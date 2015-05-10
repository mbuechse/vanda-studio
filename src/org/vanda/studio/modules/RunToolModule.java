package org.vanda.studio.modules;

import org.vanda.datasources.DataSourceMount;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.modules.runtool.RunNowTool;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.Repository;
import org.vanda.workflows.run.BuildSystem;

public class RunToolModule implements Module {

	private final Repository<String, BuildSystem> buildSystemRepository;
	private final Repository<String, DataSourceMount> dataSourceRepository;
	private final MetaRepository<String, ToolFactory> toolFactoryMeta;

	@Override
	public String getId() {
		return "Workflow editor extensions for executing a workflow";
	}

	@Override
	public Object instantiate(Application d) {
		ListRepository<ToolFactory> lr = new ListRepository<ToolFactory>();
		lr.addItem(new RunNowTool(buildSystemRepository, dataSourceRepository));
		toolFactoryMeta.addRepository(lr);
		return lr;
	}

	public RunToolModule(Repository<String, BuildSystem> buildSystemRepository,
			Repository<String, DataSourceMount> dataSourceRepository,
			MetaRepository<String, ToolFactory> toolFactoryMeta) {
		this.dataSourceRepository = dataSourceRepository;
		this.buildSystemRepository = buildSystemRepository;
		this.toolFactoryMeta = toolFactoryMeta;
	}

}
