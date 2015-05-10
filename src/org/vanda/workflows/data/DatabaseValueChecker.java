package org.vanda.workflows.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vanda.datasources.RootDataSource;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.JobVisitor;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

// TODO try to make something decent out of this
public class DatabaseValueChecker {

	public static class MissingInputsException extends Exception {

		private static final long serialVersionUID = -964570440435994657L;

	}
	
	private static class JobTraverser implements JobVisitor {
		private boolean allLitsConnected = true;
		private final List<Literal> literals;
		private final List<Job> workingSet;
		private final MutableWorkflow mwf;

		public JobTraverser(List<Literal> literals, MutableWorkflow mwf, List<Job> workingSet) {
			this.literals = literals;
			this.mwf = mwf;
			this.workingSet = workingSet;
		}

		@Override
		public void visitTool(Job j, Tool t) {
			for (Port ip : j.getInputPorts()) {
				Location l = j.bindings.get(ip);
				ConnectionKey src = mwf.getVariableSource(l);
				if (src != null)
					workingSet.add(src.target);
				else
					allLitsConnected = false;
			}
		}

		@Override
		public void visitLiteral(Job j, Literal l) {
			literals.add(l);
		}

		public boolean allLiteralsConnected() {
			return allLitsConnected;
		}
	}

	public static boolean checkDatabaseRow(MutableWorkflow mwf, RootDataSource rds,
			final HashMap<Integer, String> row, List<Literal> literals) {
		if (literals == null)
			return false;
		for (Literal l : literals) {
			String val = row.get(l.getKey());
			if (val == null)
				return false;
			if (!rds.getType(val).equals(l.getType()))
				return false;
			int i = val.indexOf(':');
			if (i == -1 || i == val.length() - 1)
				return false;
		}
		return true;
	}

	public static List<Literal> detectConnectedLiterals(final MutableWorkflow mwf) throws MissingInputsException {
		final List<Literal> literals = new ArrayList<Literal>();
		final List<Job> workingSet = new ArrayList<Job>();
		// add sink tools
		for (Job j : mwf.getChildren()) {
			j.visit(new JobVisitor() {

				@Override
				public void visitTool(Job j, Tool t) {
					if (t.getId().equals("SinkTool")) {
						workingSet.add(j);
					}
				}

				@Override
				public void visitLiteral(Job j, Literal l) {
					// do nothing
				}
			});
		}
		DatabaseValueChecker.JobTraverser jv = new JobTraverser(literals, mwf, workingSet);

		while (!workingSet.isEmpty()) {
			Job j = workingSet.remove(workingSet.size() - 1);
			j.visit(jv);
		}

		if (jv.allLiteralsConnected())
			return literals;
		else
			throw new MissingInputsException();
	}
}