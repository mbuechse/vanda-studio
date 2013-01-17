package org.vanda.studio.modules.workflows.impl;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.jgraph.ConnectionAdapter;
import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;
import org.vanda.studio.modules.workflows.jgraph.JobAdapter;
import org.vanda.studio.modules.workflows.jgraph.WorkflowAdapter;
import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.model.Model.SingleObjectSelection;
import org.vanda.studio.modules.workflows.model.Model.WorkflowSelection;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.HasActions;
import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.util.Util;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowChildEvent;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowChildListener;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowEvent;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowListener;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class WorkflowEditorImpl implements WorkflowEditor, WorkflowListener,
		WorkflowChildListener {

	protected final Application app;
	protected final Model model;
	protected final MyMxGraphComponent component;
	protected final mxGraphOutline outline;
	protected final DrecksAdapter renderer;
	protected JComponent palette;
	protected final JSplitPane mainpane;

	public WorkflowEditorImpl(Application a,
			Repository<ToolFactory> toolFactories, MutableWorkflow hwf) {
		app = a;
		model = new Model(hwf);
		renderer = new DrecksAdapter(model);
		// palette = new Palette(app, sm);
		// palette.update();

		component = new MyMxGraphComponent(renderer.getGraph());
		// component.setDragEnabled(false);
		component.getGraphControl().addMouseListener(new EditMouseAdapter());
		component.getGraphControl().addMouseWheelListener(
				new MouseZoomAdapter(app, component));
		component.addKeyListener(new DelKeyListener());
		component.setPanning(true);
		component.getPageFormat().setOrientation(PageFormat.LANDSCAPE);
		component.setPageVisible(true);
		component
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		component
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		component.zoomActual();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				component.getVerticalScrollBar()
						.setValue(
								(int) (component.getVerticalScrollBar()
										.getMaximum() * 0.35));

			}

		});
		// component.getGraphControl().scrollRectToVisible(new
		// Rectangle(133,1000,0,0));
		// component.setPanning(true); // too complic: must press SHIFT+CONTROL
		// (component.getGraph().getDefaultParent());
		outline = new mxGraphOutline(component);
		mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component,
				outline);
		mainpane.setOneTouchExpandable(true);
		mainpane.setResizeWeight(0.9);
		mainpane.setDividerSize(6);
		mainpane.setBorder(null);
		mainpane.setName(model.getRoot().getName());
		mainpane.setDividerLocation(0.7);

		app.getUIModeObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application a) {
				if (a.getUIMode().isLargeContent())
					component.zoomTo(1.5, false);
				else
					component.zoomActual();
			}
		});
		app.getWindowSystem().addAction(mainpane, new CheckWorkflowAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		app.getWindowSystem().addContentWindow(null, mainpane, null);
		app.getWindowSystem().focusContentWindow(mainpane);
		mainpane.requestFocusInWindow();

		for (ToolFactory tf : toolFactories.getItems())
			tf.instantiate(this);
		app.getWindowSystem().addAction(mainpane, new ResetZoomAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK));
		app.getWindowSystem().addAction(mainpane, new CloseWorkflowAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));

		model.getChildObservable().addObserver(
				new Observer<WorkflowChildEvent>() {

					@Override
					public void notify(WorkflowChildEvent event) {
						event.doNotify(WorkflowEditorImpl.this);
					}

				});

		model.getWorkflowObservable().addObserver(
				new Observer<WorkflowEvent>() {
					@Override
					public void notify(WorkflowEvent event) {
						event.doNotify(WorkflowEditorImpl.this);
					}
				});
		recheck(); // XXX experimental
	}

	static {
		try {
			mxGraphTransferable.dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ "; class=com.mxgraph.swing.util.mxGraphTransferable");
		} catch (ClassNotFoundException cnfe) {
			// do nothing
			System.out.println("Problem!");
		}
	}

	public void close() {
		// remove tab
		app.getWindowSystem().removeContentWindow(mainpane);
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
		WorkflowSelection ws = model.getSelection();
		if (ws instanceof SingleObjectSelection)
			((SingleObjectSelection) ws).remove();
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
				final Object value = component.getGraph().getModel()
						.getValue(cell);

				PopupMenu menu = null;

				// create connection specific context menu
				if (value instanceof ConnectionAdapter) {
					menu = new PopupMenu(((ConnectionAdapter) value).toString());
					// menu = new PopupMenu(cell.toString());

					@SuppressWarnings("serial")
					JMenuItem item = new JMenuItem("Remove Connection") {
						@Override
						public void fireActionPerformed(ActionEvent e) {
							removeSelectedCell();
						}
					};

					item.setAccelerator(KeyStroke.getKeyStroke(
							KeyEvent.VK_DELETE, 0));
					menu.add(item);
				}

				// create node specific context menu
				if (value instanceof JobAdapter) {
					menu = new PopupMenu(((JobAdapter) value).getName());
					@SuppressWarnings("serial")
					JMenuItem item = new JMenuItem("Remove Job") {
						@Override
						public void fireActionPerformed(ActionEvent e) {
							removeSelectedCell();
						}
					};
					item.setAccelerator(KeyStroke.getKeyStroke(
							KeyEvent.VK_DELETE, 0));
					menu.add(item);
				}

				if (menu != null) {
					if (value instanceof HasActions) {
						HasActions ha = (HasActions) value;
						LinkedList<Action> as = new LinkedList<Action>();
						ha.appendActions(as);
						for (final Action a : as) {
							@SuppressWarnings("serial")
							JMenuItem item = new JMenuItem(a.getName()) {
								@Override
								public void fireActionPerformed(ActionEvent e) {
									a.invoke();
								}
							};
							menu.add(item);
						}
					}
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	/**
	 * enables mouse wheel zooming function within graph editor window keeps the
	 * mouse cursor as zoom center
	 * 
	 * @author afischer
	 */
	protected static class MouseZoomAdapter implements MouseWheelListener {
		protected Application app;
		protected MyMxGraphComponent component;

		public MouseZoomAdapter(Application app, MyMxGraphComponent component) {
			this.app = app;
			this.component = component;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			Rectangle r = component.getViewport().getViewRect();

			mxGraphView view = component.getGraph().getView();
			// translate view to keep mouse point as fixpoint
			double factor = e.getWheelRotation() > 0 ? 1 / 1.2 : 1.2;
			double scale = (double) ((int) (view.getScale() * 100 * factor)) / 100;
			view.setScale(scale);
			Rectangle rprime = new Rectangle((int) (r.x + e.getX()
					* (factor - 1.0)), (int) (r.y + e.getY() * (factor - 1.0)),
					r.width, r.height);
			component.getGraphControl().scrollRectToVisible(rprime);
		}
	}

	/**
	 * a context popup menu that displays a components title
	 * 
	 * @author afischer
	 * 
	 */
	@SuppressWarnings("serial")
	protected static class PopupMenu extends JPopupMenu {

		public PopupMenu(String title) {
			add(new JLabel("<html><b>" + title + "</b></html>"));
			addSeparator();
		}
	}

	protected class CheckWorkflowAction implements Action {

		@Override
		public String getName() {
			return "Check Workflow";
		}

		@Override
		public void invoke() {
			recheck();
		}
	}

	protected class CloseWorkflowAction implements Action {

		@Override
		public String getName() {
			return "Close Workflow";
		}

		@Override
		public void invoke() {
			close();
		}
	}

	protected class ResetZoomAction implements Action {

		@Override
		public String getName() {
			return "Reset Zoom";
		}

		@Override
		public void invoke() {
			component.zoomActual();
		}
	}

	@Override
	public void addToolWindow(JComponent c) {
		app.getWindowSystem().addToolWindow(mainpane, null, c);
	}

	@Override
	public void focusToolWindow(JComponent c) {
		app.getWindowSystem().focusToolWindow(c);
	}

	@Override
	public void removeToolWindow(JComponent c) {
		app.getWindowSystem().removeToolWindow(mainpane, c);
	}

	@Override
	public void addAction(Action a, KeyStroke keyStroke) {
		app.getWindowSystem().addAction(mainpane, a, keyStroke);
	}

	@Override
	public Application getApplication() {
		return app;
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		if (mwf == model.getRoot()) {
			mainpane.setName(mwf.getName());
			app.getWindowSystem().addContentWindow(null, mainpane, null);
		}
	}

	private void recheck() {
		try {
			model.checkWorkflow();
		} catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
		}
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
		recheck();
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
		recheck();
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
		recheck();
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
		recheck();
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
		recheck();
	}

	@SuppressWarnings("serial")
	private static class MyMxGraphComponent extends mxGraphComponent {

		protected mxICell currentCollapsibleCell;
		protected List<mxICell> collapsedCells;

		public MyMxGraphComponent(mxGraph graph) {
			super(graph);
			this.graphHandler = new MyMxGraphHandler(this);
			// DO NOT change this setting, otherwise selecting an inner
			// workflow's parent job is kind of hard because the inner workflow
			// is always selected
			setSwimlaneSelectionEnabled(false);
			collapsedCells = new ArrayList<mxICell>();
		}

		@Override
		public boolean hitFoldingIcon(Object cell, int x, int y) {
			if (cell != null) {
				mxIGraphModel model = graph.getModel();

				// Draws the collapse/expand icons
				boolean isEdge = model.isEdge(cell);

				if (foldingEnabled && (model.isVertex(cell) || isEdge)) {
					mxCellState state = graph.getView().getState(cell);

					if (state != null
							&& graph.getModel().getValue(cell) instanceof WorkflowAdapter) {
						state = graph.getView().getState(
								graph.getModel().getParent(cell));

						ImageIcon icon = getFoldingIcon(state);

						if (icon != null) {
							if (getFoldingIconBounds(state, icon)
									.contains(x, y)) {
								currentCollapsibleCell = (mxICell) graph
										.getModel().getParent(cell);
							} else
								currentCollapsibleCell = null;
						}
					}
				}
			}

			return super.hitFoldingIcon(cell, x, y);
		}

		@Override
		/**
		 * Note: This is not used during drag and drop operations due to limitations
		 * of the underlying API. To enable this for move operations set dragEnabled
		 * to false.
		 *
		 * @param event
		 * @return Returns true if the given event is a panning event.
		 */
		public boolean isPanningEvent(MouseEvent event) {
			return (event != null) && !event.isShiftDown()
					&& event.isControlDown();
		}

	}

	private static class MyMxGraphHandler extends mxGraphHandler {

		private MyMxGraphComponent component;

		public MyMxGraphHandler(MyMxGraphComponent component) {
			super(component);
			this.component = component;
		}

		@Override
		protected Cursor getCursor(MouseEvent e) {
			Cursor cursor = super.getCursor(e);

			Object cell = graphComponent.getCellAt(e.getX(), e.getY(), false);
			mxIGraphModel m = graphComponent.getGraph().getModel();

			// if mouse is over an inner workflow's title bar
			if (cell != null && m.getValue(cell) instanceof WorkflowAdapter) {

				// check if the fold button of its parent job was hit and adjust
				// cursor image
				if ((component.currentCollapsibleCell != null && component.currentCollapsibleCell
						.equals(m.getParent(cell)))) {
					cursor = FOLD_CURSOR;
				}
			}

			return cursor;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);

			Object cell = graphComponent.getCellAt(e.getX(), e.getY(), false);

			// if an inner workflow cell was clicked
			if (cell != null
					&& graphComponent.getGraph().getModel().getValue(cell) instanceof WorkflowAdapter) {

				// if mouse is currently over the fold button of the parent job,
				// collapse or expand parent
				if (component.currentCollapsibleCell != null
						&& ((mxICell) cell).getParent().equals(
								component.currentCollapsibleCell)) {

					// if current collapsible cell is already in collapsed
					// state,
					// it is an element of the collapsedCells list
					boolean collapsed = component.collapsedCells
							.contains(component.currentCollapsibleCell);

					// FIXME? for some reason, getGraph().foldCells(...)
					// does not update the isCollapsed state of the changed cell
					// which is why there is a collapsedCells list that keeps
					// track
					// of all currently collapsed cells

					// collapse/expand depending on current state
					graphComponent.getGraph().foldCells(!collapsed, false,
							new Object[] { component.currentCollapsibleCell });

					// remove expanded cell from collapsedCells list
					if (collapsed) {
						component.collapsedCells
								.remove(component.currentCollapsibleCell);
					} else {
						component.collapsedCells
								.add(component.currentCollapsibleCell);
					}
				}

			}
		}
	}

	@Override
	public void setPalette(JComponent c) {
		if (palette != c) {
			if (palette != null)
				removeToolWindow(palette);
			palette = c;
			if (palette != null)
				addToolWindow(palette);
			// mainpane.setRightComponent(c);
		}
	}

	@Override
	public Model getModel() {
		return model;
	}
}