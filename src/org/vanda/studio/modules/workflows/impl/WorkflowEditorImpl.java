package org.vanda.studio.modules.workflows.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.mxDropTargetListener;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.util.Action;
import org.vanda.util.HasActions;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.util.Util;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;

import com.mxgraph.swing.util.mxGraphTransferable;


public class WorkflowEditorImpl extends DefaultWorkflowEditorImpl {

	protected final PresentationModel presentationModel;
	protected final ErrorHighlighter synUp;
	private final Observer<WorkflowEvent<MutableWorkflow>> mwfObserver;

	public WorkflowEditorImpl(Application app, List<ToolFactory> toolFactories, Pair<MutableWorkflow, Database> phd) {
		super(app, phd);
		presentationModel = new PresentationModel(view, app.getToolMetaRepository().getRepository());

		mwfObserver = new Observer<WorkflowEvent<MutableWorkflow>>() {
			@Override
			public void notify(WorkflowEvent<MutableWorkflow> event) {
				event.doNotify(WorkflowEditorImpl.this);
			}
		};
		view.getWorkflow().getObservable().addObserver(mwfObserver);

		synUp = new ErrorHighlighter(app, syntaxAnalysis, view);
		syntaxAnalysis.getSyntaxChangedObservable().addObserver(synUp);

		component = new MyMxGraphComponent(presentationModel.getVisualization().getGraph());
		new mxDropTargetListener(presentationModel, component);
		configureComponent();
		component.getGraphControl().addMouseListener(new EditMouseAdapter());
		component.addKeyListener(new DelKeyListener());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				component.getVerticalScrollBar().setValue((int) (component.getVerticalScrollBar().getMaximum() * 0.35));
			}
		});
		component.setName(view.getWorkflow().getName());
		app.getWindowSystem().addContentWindow(null, component, null);

		addAction(new ResetZoomAction(), KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK), 8);
		addAction(new CloseWorkflowAction(), KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK), 1);

		setupOutline();

		for (ToolFactory tf : toolFactories)
			tools.add(tf.instantiate(this));

		syntaxAnalysis.checkWorkflow(view.getWorkflow());
		// send some initial event ("updated" will be sent)
		view.getWorkflow().beginUpdate();
		view.getWorkflow().endUpdate();

		// focus window
		app.getWindowSystem().focusContentWindow(component);
		component.requestFocusInWindow();
		// init outline painting
		outline.updateScaleAndTranslate();
	}

	static {
		try {
			mxGraphTransferable.dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
					+ "; class=com.mxgraph.swing.util.mxGraphTransferable");
		} catch (ClassNotFoundException cnfe) {
			// do nothing
			System.err.println("Problem!");
		}
	}

	/**
	 * Handles KeyEvents such as removing cells when focused and pressing DEL
	 * 
	 * @author afischer
	 * 
	 */
	protected class DelKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {

			// check if KeyEvent occurred on graph component,
			// e.getSource().equals(component) &&
			// only handle DELETE-key
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				removeSelectedCell();
			}

		}
	}

	private void removeSelectedCell() {
		view.removeSelectedCell();
	}

	/**
	 * Handles mouse actions: opens cell-specific views/editors on double-click,
	 * opens context menu on mouse right-click
	 * 
	 * @author buechse, afischer
	 * 
	 */
	protected class EditMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == 1 && e.getClickCount() == 2) {
				// double click using left mouse button
				Object cell = component.getCellAt(e.getX(), e.getY());
				Object value = component.getGraph().getModel().getValue(cell);

				if (value instanceof HasActions) {
					Action def = Util.getDefaultAction((HasActions) value);
					if (def != null)
						def.invoke();
				}
			} else if (e.getButton() == 3) {
				// show context menu when right clicking a node or an edge
				Object cell = component.getCellAt(e.getX(), e.getY());
				final Object value = component.getGraph().getModel().getValue(cell);

				if (value != null)
					((Cell) value).rightMouseClick(e);
			}
		}
	}

}
