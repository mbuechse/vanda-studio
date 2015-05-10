package org.vanda.studio.modules;

import javax.swing.JComponent;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.modules.inspector.ElementEditorFactories;
import org.vanda.studio.modules.inspector.InspectorToolFactory;
import org.vanda.studio.modules.inspector.LiteralEditor;
import org.vanda.swing.data.ElementSelector;
import org.vanda.types.Type;
import org.vanda.util.CompositeFactory;
import org.vanda.util.Factory;
import org.vanda.util.MetaRepository;
import org.vanda.util.Repository;
import org.vanda.util.StaticRepository;
import org.vanda.workflows.run.BuildSystem;

public class WorkflowInspectorModule implements Module {

	private final ElementEditorFactories eefs;

	public WorkflowInspectorModule(
			RootDataSource rootDataSource,
			Repository<Class<? extends DataSource>, Factory<DataSource, ElementSelector>> elementSelectorFactoryRepository,
			MetaRepository<String, ToolFactory> toolFactoryMeta, Repository<Type, Factory<String, JComponent>> previewFactories,
			Repository<String, BuildSystem> buildSystemRepository,
			Repository<String, DataSourceMount> dataSourceRepository) {

		eefs = new ElementEditorFactories();
		eefs.workflowFactories.add(new org.vanda.studio.modules.inspector.WorkflowElementEditor());
		eefs.literalFactories.add(new LiteralEditor(rootDataSource, new CompositeFactory<DataSource, ElementSelector>(
				elementSelectorFactoryRepository)));

		StaticRepository<String, ToolFactory> sr = new StaticRepository<String, ToolFactory>();
		ToolFactory tf = new InspectorToolFactory(eefs, previewFactories, buildSystemRepository, dataSourceRepository);
		sr.put(tf.getId(), tf);
		toolFactoryMeta.addRepository(sr);
	}

	@Override
	public String getId() {
		return "Inspector for Workflows";
	}

	@Override
	public Object instantiate(Application d) {
		// TODO Auto-generated method stub
		return null;
	}

}
