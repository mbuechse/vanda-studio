package org.vanda.studio.modules.workflows;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Factory;
import org.vanda.util.Pair;
import org.vanda.util.Repository;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.serialization.Loader;

public class WorkflowPreview implements Factory<String, Object> {
	private final Application app;
	private final ToolFactory mainComponentToolFactory;
	private final Repository<String, ToolFactory> toolFactories;
	private final Repository<String, Tool> toolRepository;

	public WorkflowPreview(Application app, ToolFactory mainComponentToolFactory,
			Repository<String, Tool> toolRepository, Repository<String, ToolFactory> toolFactories) {
		this.app = app;
		this.mainComponentToolFactory = mainComponentToolFactory;
		this.toolFactories = toolFactories;
		this.toolRepository = toolRepository;
	}

	@Override
	public Object instantiate(String filePath) {
		Pair<MutableWorkflow, Database> phd = null;
		if ("".equals(filePath)) {
			phd = new Pair<MutableWorkflow, Database>(new MutableWorkflow("Workflow"), new Database());
		} else {
			try {
				phd = new Loader(toolRepository).load(filePath);
				// rc = new org.vanda.run.serialization.Loader().load(filePath +
				// ".run");
			} catch (Exception e) {
				app.sendMessage(new ExceptionMessage(e));
			}
		}
		if (phd != null)
			return new WorkflowEditorImpl(app, mainComponentToolFactory, toolFactories.getItems(), phd);
		else
			return null;
		// let's hope the GUI will hold a reference
	}
}
