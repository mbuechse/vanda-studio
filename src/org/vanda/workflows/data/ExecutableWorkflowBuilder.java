package org.vanda.workflows.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.ElementAdapter;
import org.vanda.workflows.hyper.ElementReturnVisitor;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;

/**
 * @author kgebhardt, buechse
 * 
 */
public class ExecutableWorkflowBuilder {
	
	final MutableWorkflow workflow;
	final SyntaxAnalysis synA;
	final double[] dims;
	final MutableWorkflow ewf;
	final Database edb;
	double dx = 0;
	double dy = 0;
	
	public ExecutableWorkflowBuilder(MutableWorkflow wf, SyntaxAnalysis synA) {
		workflow = wf;
		dims = workflowDimension(workflow);
		this.synA = synA;
		ewf = new MutableWorkflow(workflow.getName());
		edb = new Database();
		edb.put(new Integer(0), "");  // special row id that does not influence job id 
	}

	private static double[] workflowDimension(MutableWorkflow mwf) {
		double xmin = 0, xmax = 0, ymin = 0, ymax = 0;
		Iterator<Job> ji = mwf.getChildren().iterator();
		if (ji.hasNext()) {
			Job j = ji.next();
			xmin = j.getX();
			xmax = j.getX() + j.getWidth();
			ymin = j.getY();
			ymax = j.getY() + j.getHeight();
		}
		while (ji.hasNext()) {
			Job j = ji.next();
			if (xmin > j.getX())
				xmin = j.getX();
			if (xmax < j.getX() + j.getWidth())
				xmax = j.getX() + j.getWidth();
			if (ymin > j.getY())
				ymin = j.getY();
			if (ymax < j.getY() + j.getHeight())
				ymax = j.getY() + j.getHeight();
		}
		return new double[] { xmax - xmin, ymax - ymin };

	}

	private void createJobInstance(DataflowAnalysis dfa, Map<Location, Location> translation, Job j,
			ElementAdapter element) {
		Job ej = new Job(element, dfa.getJobId(j));
		ej.setDimensions(new double[] { j.getX() + dx, j.getY() + dy, j.getWidth(), j.getHeight() });
		ewf.addChild(ej);

		// Build Variable-Translation Table
		for (Port op : j.getOutputPorts()) {
			translation.put(j.bindings.get(op), ej.bindings.get(op));
		}

		// Establish Connections
		for (Port ip : j.getInputPorts()) {
			ewf.addConnection(new ConnectionKey(ej, ip), translation.get(j.bindings.get(ip)));
		}

	}

	public void addAssigment(final Map<Integer, String> assignment) {
		// to translate between original Location and new copies
		final Map<Location, Location> translation = new HashMap<Location, Location>();
		final DataflowAnalysis dfa = new DataflowAnalysis();
		dfa.init(assignment, synA.getSorted());

		for (final Job j : synA.getSorted()) {
			ElementAdapter element = null;
			element = j.visitReturn(new ElementReturnVisitor<ElementAdapter>() {
				@Override
				public ElementAdapter visitLiteral(Literal lit) {
					Literal newlit = new Literal(lit.getType(), lit.getName(), null);
					edb.put(newlit.getKey(), assignment.get(lit.getKey()));
					return new LiteralAdapter(newlit);
				}

				@Override
				public ElementAdapter visitTool(Tool t) {
					return j.getElement();
				}
			});
			createJobInstance(dfa, translation, j, element);
		}
		
		dy += dims[1];
	}
	
	public MutableWorkflow getWorkflow() {
		return ewf;
	}
	
	public Database getDatabase() {
		return edb;
	}
}
