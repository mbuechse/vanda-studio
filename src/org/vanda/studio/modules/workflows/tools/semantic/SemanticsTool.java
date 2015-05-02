package org.vanda.studio.modules.workflows.tools.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.view.View;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class SemanticsTool implements ToolFactory {

	private final static class Tool {

		private final SemanticAnalysis semA;
		private final SyntaxAnalysis synA;
		private final View view;
		private final Collection<Object> tools;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {

			view = wfe.getView();
			// model = new Model(view, wfe.getDatabase());
			synA = wfe.getSyntaxAnalysis();
			semA = wfe.getSemanticAnalysis();
			tools = new ArrayList<Object>();
			for (SemanticsToolFactory stf : stfs)
				// stf.instantiate(wfe, model, view);
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
