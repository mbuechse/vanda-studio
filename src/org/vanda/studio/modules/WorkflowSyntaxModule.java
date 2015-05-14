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

	private final MetaRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> contextFactoryMeta;
	private final MetaRepository<String, ToolFactory> toolFactoryMeta;

	@Override
	public String getId() {
		return "Syntax Analysis for Workflow Editors";
	}

	@Override
	public Object instantiate(Application a) {
		StaticRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> sr;
		StaticRepository<String, ToolFactory> sr2;
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
		return sr;
	}

	public WorkflowSyntaxModule(
			MetaRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> contextFactoryMeta,
			MetaRepository<String, ToolFactory> toolFactoryMeta) {
		this.contextFactoryMeta = contextFactoryMeta;
		this.toolFactoryMeta = toolFactoryMeta;
	}

}
