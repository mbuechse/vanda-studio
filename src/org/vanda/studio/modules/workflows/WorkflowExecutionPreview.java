package org.vanda.studio.modules.workflows;

import java.util.List;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.modules.workflows.impl.WorkflowExecution;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.serialization.Loader;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class WorkflowExecutionPreview implements PreviewFactory {
	private final Application app;
	private final List<ToolFactory> toolFactories;

	public WorkflowExecutionPreview(Application app, List<ToolFactory> toolFactories) {
		this.app = app;
		this.toolFactories = toolFactories;
	}

	@Override
	public JComponent createPreview(String filePath) {
		throw new NotImplementedException();
	}

	@Override
	public void openEditor(String filePath) {
		Pair<MutableWorkflow, Database> phd;
		// TODO this is not specific to an "execution editor", and it should be
		// added to the xwf file
		// RunConfig rc;
		try {
			phd = new Loader(app.getToolMetaRepository().getRepository()).load(filePath);
			// rc = new org.vanda.run.serialization.Loader().load(filePath +
			// ".run");
			new WorkflowExecution(app, toolFactories, phd);
			// let's hope the GUI will hold a reference
		} catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
		}
	}
}
