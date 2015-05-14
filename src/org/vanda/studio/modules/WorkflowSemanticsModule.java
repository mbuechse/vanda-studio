package org.vanda.studio.modules;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.util.Factory;
import org.vanda.util.MetaRepository;
import org.vanda.util.StaticRepository;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class WorkflowSemanticsModule implements Module {
	
	private final MetaRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> contextFactoryMeta;

	@Override
	public String getId() {
		return "Semantic Analysis for Workflow Editors";
	}

	@Override
	public Object instantiate(Application a) {
		StaticRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> sr;
		sr = new StaticRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>>();
		sr.put(SemanticAnalysis.class, new Factory<WorkflowEditor, SemanticAnalysis>() {
			@Override
			public SemanticAnalysis instantiate(WorkflowEditor wfe) {
				Database database = wfe.getDatabase();
				SyntaxAnalysis synA = wfe.getContext(SyntaxAnalysis.class);
				SemanticAnalysis semA = new SemanticAnalysis();
				synA.getSyntaxChangedObservable().addObserver(semA);
				database.getObservable().addObserver(semA);
				semA.notify(new Databases.CursorChange<Database>(database));
				return semA;
			}

		});
		contextFactoryMeta.addRepository(sr);
		return sr;
	}

	public WorkflowSemanticsModule(
			MetaRepository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> contextFactoryMeta) {
		this.contextFactoryMeta = contextFactoryMeta;
	}

}
