package org.vanda.studio.modules.workflows.impl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.LayoutSelector;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

/**
 * Parent class for WorkflowEditor and WorkflowExecution that contains shared functionality
 * @author kgebhardt
 *
 */
public class DefaultWorkflowEditorImpl implements WorkflowEditor, WorkflowListener<MutableWorkflow> {
	protected final Application app;
	protected mxGraphComponent component;
	protected final Database database;
	protected mxGraphOutline outline;
	protected final View view;
	protected final SyntaxAnalysis syntaxAnalysis;
	protected final Collection<Object> tools;

	private Observer<Application> appObserver;

	public DefaultWorkflowEditorImpl(Application app, Pair<MutableWorkflow, Database> phd) {
		this.app = app;
		view = new View(phd.fst);
		database = phd.snd;
		syntaxAnalysis = new SyntaxAnalysis();
		view.getWorkflow().getObservable().addObserver(syntaxAnalysis);
		tools = new ArrayList<Object>();
	}
	
	@Override
	public void addAction(Action a, KeyStroke keyStroke) {
		addAction(a, keyStroke, 0);
	}
	
	@Override
	public void addAction(Action a, KeyStroke keyStroke, int pos) {
		app.getWindowSystem().addAction(component, a, keyStroke, pos);
	}

	@Override
	public void addToolWindow(JComponent c, LayoutSelector layout) {
		app.getWindowSystem().addToolWindow(component, null, c, layout);
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void enableAction(Action a) {
		app.getWindowSystem().enableAction(component, a);
	}
	
	@Override
	public void disableAction(Action a) {
		app.getWindowSystem().disableAction(component, a);
	}
	
	@Override
	public void focusToolWindow(JComponent c) {
		app.getWindowSystem().focusToolWindow(c);
	}

	@Override
	public Application getApplication() {
		return app;
	}

	@Override
	public Database getDatabase() {
		return database;
	}

	@Override
	public View getView() {
		return view;
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		if (mwf == view.getWorkflow()) {
			component.setName(mwf.getName());
			app.getWindowSystem().addContentWindow(null, component, null);
		}
	}

	@Override
	public void removeToolWindow(JComponent c) {
		app.getWindowSystem().removeToolWindow(component, c);
	}

	@Override
	public void updated(MutableWorkflow mwf) {
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

	public void close() {
		// remove tab
		app.getWindowSystem().removeContentWindow(component);
	}

	/**
	 * enables mouse wheel zooming function within graph editor window keeps the
	 * mouse cursor as zoom center
	 * 
	 * @author afischer
	 */
	protected static class MouseZoomAdapter implements MouseWheelListener {
		protected Application app;
		protected mxGraphComponent component;

		public MouseZoomAdapter(Application app, mxGraphComponent component) {
			this.app = app;
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
		component.getGraphControl().addMouseWheelListener(new MouseZoomAdapter(app, component));
		component.setPanning(true);
		component.setPageVisible(false);
		component.setVerticalPageCount(0);
		component.setHorizontalPageCount(0);
		component.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		component.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		appObserver = new Observer<Application>() {
			@Override
			public void notify(Application a) {
				if (a.getUIMode().isLargeContent())
					component.zoomTo(1.5, false);
				else
					component.zoomActual();
			}
		};
		app.getUIModeObservable().addObserver(appObserver);
		// initialize zoom
		appObserver.notify(app);
		
	}

	protected void setupOutline() {
		outline = new mxGraphOutline(component);
		outline.setPreferredSize(new Dimension(250, 250));
		outline.setName("Map");
		// outline.setFitPage(true);
		// addToolWindow(outline, WindowSystem.SOUTHEAST);
		addToolWindow(outline, WindowSystem.NORTHEAST);
	}

	@Override
	public void addAction(Action a, String imageName, KeyStroke keyStroke, int pos) {
		app.getWindowSystem().addAction(component, a, imageName, keyStroke, pos);
	}

	@Override
	public SyntaxAnalysis getSyntaxAnalysis() {
		return syntaxAnalysis;
	}

}
