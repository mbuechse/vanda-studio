package org.vanda.studio.modules;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.studio.modules.workflowsyntax.ErrorHighlighterFactory;
import org.vanda.util.Factory;
import org.vanda.util.MetaRepository;
import org.vanda.util.StaticRepository;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class WorkflowSyntaxModule implements Module {

	@Override
	public String getId() {
		return "Syntax Analysis for Workflow Editors";
	}

	@Override
	public Object instantiate(Application a) {
		return null;
	}

	private final StaticRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> sr;
	private final StaticRepository<String, ToolFactory> sr2;

	public WorkflowSyntaxModule(
			MetaRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> contextFactoryMeta,
			MetaRepository<String, ToolFactory> toolFactoryMeta) {
		sr = new StaticRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>>();
		sr.put(SyntaxAnalysis.class, new Factory<WorkflowEditor, SyntaxAnalysis>() {
			@Override
			public SyntaxAnalysis instantiate(WorkflowEditor d) {
				SyntaxAnalysis synA = new SyntaxAnalysis();
				d.getView().getWorkflow().getObservable().addObserver(synA);
				return synA;
			}

		});
		contextFactoryMeta.addRepository(sr);

		sr2 = new StaticRepository<String, ToolFactory>();
		ToolFactory tf = new ErrorHighlighterFactory();
		sr2.put(tf.getId(), tf);
		toolFactoryMeta.addRepository(sr2);
	}

}
