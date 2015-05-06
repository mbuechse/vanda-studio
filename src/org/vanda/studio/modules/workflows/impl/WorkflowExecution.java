package org.vanda.studio.modules.workflows.impl;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.fragment.model.Generator;
import org.vanda.presentationmodel.PresentationModel;
import org.vanda.run.RunConfig;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeCheckingException;

import com.mxgraph.swing.mxGraphComponent;

/**
 * execution environment for experiments
 * 
 * more or less obsolete -- one could use the standard WorkflowEditorImpl as well, thanks to the RunNowTool
 * 
 * @author kgebhardt, buechse
 * 
 */
public class WorkflowExecution extends DefaultWorkflowEditorImpl {

	private final PresentationModel pm;

	public WorkflowExecution(Application app, Pair<MutableWorkflow, Database> phd, Generator prof, RunConfig rc,
			List<ToolFactory> toolFactories) throws TypeCheckingException {
		super(app, phd);

		pm = new PresentationModel(view, app.getToolMetaRepository().getRepository());
		// XXX this is a hack, because the underlying visualization components should not be accessed
		pm.getVisualization().setGraphImmutable();

		// setup component design
		component = (mxGraphComponent) pm.getVisualization().getGraphComponent();
		component = new MyMxGraphComponent(pm.getVisualization().getGraph());
		component.setConnectable(false);
		component.setDragEnabled(false);
		configureComponent();
		component.setName(phd.fst.getName() + "Execution");

		setupOutline();

		// add Menu-Actions
		addAction(new ResetZoomAction(), KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK), 3);
		addAction(new CloseWorkflowAction(), KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK), 2);

		// addComponent
		app.getWindowSystem().addContentWindow(null, component, null);

		for (ToolFactory tf : toolFactories)
			tools.add(tf.instantiate(this));
		syntaxAnalysis.checkWorkflow(view.getWorkflow());

		// focus window
		app.getWindowSystem().focusContentWindow(component);
		component.requestFocusInWindow();
		// init outline painting
		outline.updateScaleAndTranslate();
	}

	public JComponent getComponent() {
		return component;
	}

}