package org.vanda.studio.modules.workflows;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.LayoutSelector;
import org.vanda.studio.modules.workflows.model.MainComponentTool;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

/**
 * Parent class for WorkflowEditor and WorkflowExecution that contains shared
 * functionality
 * 
 * @author kgebhardt
 *
 */
public class WorkflowEditorImpl implements WorkflowEditor, WorkflowListener<MutableWorkflow> {
	protected final Application app;
	protected final Database database;
	protected final View view;
	private final Observer<WorkflowEvent<MutableWorkflow>> mwfObserver;
	protected final SyntaxAnalysis syntaxAnalysis;
	protected final Collection<Object> tools;
	protected final MainComponentTool mainComponentTool;
	protected final JComponent component;
	protected final MultiplexObserver<WorkflowEditor> shutdownObservable;
	protected final MultiplexObserver<WorkflowEditor> uiModeObservable;

	public WorkflowEditorImpl(Application app, ToolFactory mainComponentFactory, List<ToolFactory> toolFactories,
			Pair<MutableWorkflow, Database> phd) {
		this.app = app;
		view = new View(phd.fst);
		database = phd.snd;
		shutdownObservable = new MultiplexObserver<WorkflowEditor>();
		uiModeObservable = new MultiplexObserver<WorkflowEditor>();
		app.getShutdownObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application event) {
				shutdownObservable.notify(WorkflowEditorImpl.this);
			}
		});
		app.getUIModeObservable().addObserver(new Observer<Application>() {
			@Override
			public void notify(Application event) {
				uiModeObservable.notify(WorkflowEditorImpl.this);
			}
		});
		syntaxAnalysis = new SyntaxAnalysis();
		view.getWorkflow().getObservable().addObserver(syntaxAnalysis);
		tools = new ArrayList<Object>();
		component = new JPanel(new BorderLayout());
		component.setName(view.getWorkflow().getName());
		app.getWindowSystem().addContentWindow(null, component, null);

		// focus window a FIRST TIME so that it gets its layout before we add the tools
		app.getWindowSystem().focusContentWindow(component);
		component.requestFocusInWindow();
		
		mainComponentTool = (MainComponentTool) mainComponentFactory.instantiate(this);
		component.add(mainComponentTool.getComponent(), BorderLayout.CENTER);

		mwfObserver = new Observer<WorkflowEvent<MutableWorkflow>>() {
			@Override
			public void notify(WorkflowEvent<MutableWorkflow> event) {
				event.doNotify(WorkflowEditorImpl.this);
			}
		};
		view.getWorkflow().getObservable().addObserver(mwfObserver);


		addAction(new CloseWorkflowAction(), KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK), 1);

		for (ToolFactory tf : toolFactories)
			tools.add(tf.instantiate(this));

		syntaxAnalysis.checkWorkflow(view.getWorkflow());
		// send some initial event ("updated" will be sent)
		view.getWorkflow().beginUpdate();
		view.getWorkflow().endUpdate();

		// FIXME focus window a SECOND TIME because otherwise the buttons are not being displayed
		app.getWindowSystem().focusContentWindow(component);
		component.requestFocusInWindow();
		// init outline painting
		// outline.updateScaleAndTranslate();
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

	public void close() {
		// remove tab
		app.getWindowSystem().removeContentWindow(component);
	}

	@Override
	public void addAction(Action a, String imageName, KeyStroke keyStroke, int pos) {
		app.getWindowSystem().addAction(component, a, imageName, keyStroke, pos);
	}

	@Override
	public SyntaxAnalysis getSyntaxAnalysis() {
		return syntaxAnalysis;
	}

	@Override
	public String getProperty(String key) {
		return app.getProperty(getClass().getName() + "." + key);
	}

	@Override
	public void setProperty(String key, String value) {
		app.setProperty(getClass().getName() + "." + key, value);
	}

	@Override
	public Observable<WorkflowEditor> getShutdownObservable() {
		return shutdownObservable;
	}

	@Override
	public Observable<WorkflowEditor> getUIModeObservable() {
		return uiModeObservable;
	}

	@Override
	public boolean isLargeContent() {
		return app.getUIMode().isLargeContent();
	}

}
