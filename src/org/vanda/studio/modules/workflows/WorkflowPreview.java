package org.vanda.studio.modules.workflows;

import java.util.List;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.serialization.Loader;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WorkflowPreview implements PreviewFactory {
	private final Application app;
	private final ToolFactory mainComponentToolFactory;
	private final List<ToolFactory> toolFactories;

	public WorkflowPreview(Application app, ToolFactory mainComponentToolFactory, List<ToolFactory> toolFactories) {
		this.app = app;
		this.mainComponentToolFactory = mainComponentToolFactory;
		this.toolFactories = toolFactories;
	}

	@Override
	public JComponent createPreview(String filePath) {
		throw new NotImplementedException();
	}

	@Override
	public void openEditor(String filePath) {
		Pair<MutableWorkflow, Database> phd = null;
		if ("".equals(filePath)) {
			phd = new Pair<MutableWorkflow, Database>(new MutableWorkflow("Workflow"), new Database());
		} else {
			try {
				phd = new Loader(app.getToolMetaRepository().getRepository()).load(filePath);
				// rc = new org.vanda.run.serialization.Loader().load(filePath +
				// ".run");
			} catch (Exception e) {
				app.sendMessage(new ExceptionMessage(e));
			}
		}
		if (phd != null)
			new WorkflowEditorImpl(app, mainComponentToolFactory, toolFactories, phd);
		// let's hope the GUI will hold a reference
	}
}
