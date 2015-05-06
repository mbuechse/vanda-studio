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

public class WorkflowExecutionPreview implements PreviewFactory {
	final Application app;
	final List<ToolFactory> toolFactories;

	public WorkflowExecutionPreview(Application app, List<ToolFactory> toolFactories) {
		this.app = app;
		this.toolFactories = toolFactories;
	}

	@Override
	public JComponent createPreview(String filePath) {
		Pair<MutableWorkflow, Database> phd;
		// TODO this is not specific to an "execution editor", and it should be added to the xwf file
		// RunConfig rc;
		try {
			phd = new Loader(app.getToolMetaRepository().getRepository()).load(filePath + ".xwf");
			// rc = new org.vanda.run.serialization.Loader().load(filePath + ".run");
			WorkflowExecution wfe = new WorkflowExecution(app, phd, toolFactories);
			return wfe.getComponent();
		} catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
			return null;
		}
	}

	@Override
	public void openEditor(String value) {
		// do nothing
	}

}
