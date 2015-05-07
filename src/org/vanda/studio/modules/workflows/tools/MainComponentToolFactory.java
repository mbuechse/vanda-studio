package org.vanda.studio.modules.workflows.tools;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.mxDropTargetListener;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.modules.workflows.model.MainComponentTool;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.HasActions;
import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.util.Util;
import org.vanda.workflows.elements.Tool;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class MainComponentToolFactory implements ToolFactory {

	public static class MainComponentToolImpl implements MainComponentTool {
		
		private final WorkflowEditor wfe;
		private final PresentationModel presentationModel;
		private Observer<WorkflowEditor> appObserver;
		private final mxGraphComponent component;
		protected mxGraphOutline outline;
		
		public MainComponentToolImpl(Repository<String, Tool> toolRepository, WorkflowEditor wfe, boolean immutable) {
			this.wfe = wfe;
			presentationModel = new PresentationModel(wfe.getView(), toolRepository);
			component = new MyMxGraphComponent(presentationModel.getVisualization().getGraph());
			new mxDropTargetListener(presentationModel, component);
			if (immutable) {
				// XXX this is a hack, because the underlying visualization components
				// should not be accessed
				presentationModel.getVisualization().setGraphImmutable();

				// setup component design
				component.setConnectable(false);
				component.setDragEnabled(false);
			}
			configureComponent();
			component.getGraphControl().addMouseListener(new EditMouseAdapter());
			component.addKeyListener(new DelKeyListener());
//			SwingUtilities.invokeLater(new Runnable() {
//				@Override
//				public void run() {
//					component.getVerticalScrollBar().setValue((int) (component.getVerticalScrollBar().getMaximum() * 0.35));
//				}
//			});
			setupOutline();
			wfe.addAction(new ResetZoomAction(), KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK), 8);
		}

		/**
		 * enables mouse wheel zooming function within graph editor window keeps the
		 * mouse cursor as zoom center
		 * 
		 * @author afischer
		 */
		protected static class MouseZoomAdapter implements MouseWheelListener {
			protected mxGraphComponent component;
	
			public MouseZoomAdapter(mxGraphComponent component) {
				this.component = component;
			}
	
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Rectangle r = component.getViewport().getViewRect();
	
				mxGraphView view = component.getGraph().getView();
				// translate view to keep mouse point as fixpoint
				double factor = e.getWheelRotation() > 0 ? 1 / 1.2 : 1.2;
				double scale = view.getScale() * factor;
				view.setScale(scale);
				Rectangle rprime = new Rectangle((int) (r.x + e.getX() * (factor - 1.0)), (int) (r.y + e.getY()
						* (factor - 1.0)), r.width, r.height);
				component.getGraphControl().scrollRectToVisible(rprime);
			}
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
	
		@SuppressWarnings("serial")
		protected static class MyMxGraphComponent extends mxGraphComponent {
	
			public MyMxGraphComponent(mxGraph graph) {
				super(graph);
				// DO NOT change this setting, otherwise selecting an inner
				// workflow's parent job is kind of hard because the inner workflow
				// is always selected
				setSwimlaneSelectionEnabled(false);
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
				return (event != null) && !event.isShiftDown() && event.isControlDown();
			}
	
		}
	
		protected void configureComponent() {
			component.setDragEnabled(false);
			component.getGraphControl().addMouseWheelListener(new MouseZoomAdapter(component));
			component.setPanning(true);
			component.setPageVisible(false);
			component.setVerticalPageCount(0);
			component.setHorizontalPageCount(0);
			component.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			component.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			appObserver = new Observer<WorkflowEditor>() {
				@Override
				public void notify(WorkflowEditor a) {
					if (a.isLargeContent())
						component.zoomTo(1.5, false);
					else
						component.zoomActual();
				}
			};
			wfe.getUIModeObservable().addObserver(appObserver);
			// initialize zoom
			appObserver.notify(wfe);
			
		}
	
		protected void setupOutline() {
			outline = new mxGraphOutline(component);
			outline.setPreferredSize(new Dimension(250, 250));
			outline.setName("Map");
			// outline.setFitPage(true);
			wfe.addToolWindow(outline, WindowSystem.NORTHEAST);
		}

		private void removeSelectedCell() {
			wfe.getView().removeSelectedCell();
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
		public JComponent getComponent() {
			return component;
		}
		
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
	
	private final boolean immutable;
	private final Repository<String, Tool> toolRepository;
	
	public MainComponentToolFactory(Repository<String, Tool> toolRepository, boolean immutable) {
		this.immutable = immutable;
		this.toolRepository = toolRepository;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new MainComponentToolImpl(toolRepository, wfe, immutable);
	}

}
