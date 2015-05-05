package org.vanda.studio.modules.workflows.tools.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class SemanticsTool implements ToolFactory {

	private final static class Tool {

		private final Database database;
		private final SemanticAnalysis semA;
		private final SyntaxAnalysis synA;
		private final View view;
		private final Collection<Object> tools;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {

			view = wfe.getView();
			database = wfe.getDatabase();
			synA = wfe.getSyntaxAnalysis();
			semA = new SemanticAnalysis();
			synA.getSyntaxChangedObservable().addObserver(semA);
			database.getObservable().addObserver(semA);
			
			semA.notify(new Databases.CursorChange<Database>(database));
			tools = new ArrayList<Object>();
			for (SemanticsToolFactory stf : stfs)
				tools.add(stf.instantiate(wfe, synA, semA, view));

		}

	}

	private final LinkedList<SemanticsToolFactory> repository;

	public SemanticsTool() {
		repository = new LinkedList<SemanticsToolFactory>();
	}

	public SemanticsTool(List<SemanticsToolFactory> srep) {
		repository = new LinkedList<SemanticsToolFactory>(srep);
	}

	public LinkedList<SemanticsToolFactory> getSemanticsToolFactoryMetaRepository() {
		return repository;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe, repository);
	}

}
