package org.vanda.studio.modules;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.modules.assignments.AssignmentSwitchToolFactory;
import org.vanda.studio.modules.assignments.AssignmentTableToolFactory;
import org.vanda.studio.modules.assignments.ExpandWorkflowTool;
import org.vanda.swing.data.ElementSelector;
import org.vanda.types.Type;
import org.vanda.util.CompositeFactory;
import org.vanda.util.Factory;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.Repository;

public class AssignmentsModule implements Module {

	@Override
	public String getId() {
		return "Workflow editor tools for working with assignments";
	}

	@Override
	public Object instantiate(Application app) {
		ListRepository<ToolFactory> lr = new ListRepository<ToolFactory>();
		lr.addItem(new AssignmentTableToolFactory(app, new CompositeFactory<DataSource, ElementSelector>(
				elementSelectorFactoryRepository), rootDataSource));
		lr.addItem(new AssignmentSwitchToolFactory());
		lr.addItem(new ExpandWorkflowTool(app, rootDataSource, editorFactoryRepository, execution));
		toolFactoryMeta.addRepository(lr);
		return lr;
	}

	private final Type execution;
	private final RootDataSource rootDataSource;
	private final Repository<Class<? extends DataSource>, Factory<DataSource, ElementSelector>> elementSelectorFactoryRepository;
	private final Repository<Type, Factory<String, Object>> editorFactoryRepository;
	private final MetaRepository<String, ToolFactory> toolFactoryMeta;

	public AssignmentsModule(
			Type execution,
			RootDataSource rds,
			Repository<Class<? extends DataSource>, Factory<DataSource, ElementSelector>> elementSelectorFactoryRepository,
			Repository<Type, Factory<String, Object>> editorFactoryRepository,
			MetaRepository<String, ToolFactory> toolFactoryMeta) {
		this.execution = execution;
		this.rootDataSource = rds;
		this.elementSelectorFactoryRepository = elementSelectorFactoryRepository;
		this.editorFactoryRepository = editorFactoryRepository;
		this.toolFactoryMeta = toolFactoryMeta;
	}

}
