package org.vanda.studio.modules.workflows.impl;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.execution.model.RunStates.*;
import org.vanda.fragment.model.Generator;
import org.vanda.presentationmodel.execution.PresentationModel;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.run.Run;
import org.vanda.studio.modules.workflows.run.RunConfig;
import org.vanda.studio.modules.workflows.tools.semantic.InspectorTool;
import org.vanda.studio.modules.workflows.tools.semantic.SemanticsTool;
import org.vanda.studio.modules.workflows.tools.semantic.SemanticsToolFactory;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.TypeCheckingException;

import com.mxgraph.swing.mxGraphComponent;

/**
 * execution environment for experiments
 * 
 * @author kgebhardt, buechse
 * 
 */
public class WorkflowExecution extends DefaultWorkflowEditorImpl implements Observer<RunEvent>, RunEventListener {

	public static Map<String, Job> createIdMap(MutableWorkflow wf) {
		Map<String, Job> result = new HashMap<String, Job>();
		for (Job j : wf.getChildren()) {
			if (j.getId() != null)
				result.put(j.getId(), j);
		}
		return result;
	}
	
	public static class RunEventObserver implements Observer<RunEventId> {
		
		private final Map<String, Job> idMap;
		private final View v;
		
		public RunEventObserver(View v) {
			this.v = v;
			idMap = createIdMap(v.getWorkflow());
		}

		@Override
		public void notify(RunEventId event) {
			Job j = idMap.get(event.getId());
			if (j == null) {
				// for (Job j1 : idMap.values())
				// 	v.getJobView(j1).notify(event.getEvent());
			} else
				v.getJobView(j).notify(event.getEvent());
		}
		
	}

	private final class CancelAction implements Action {
		@Override
		public String getName() {
			return "Cancel";
		}

		@Override
		public void invoke() {
			run.cancelled();
		}

		public void disable() {
			app.getWindowSystem().disableAction(component, this);
		}

		public void enable() {
			app.getWindowSystem().enableAction(component, this);
		}
	}

	private final class RunAction implements Action {

		@Override
		public String getName() {
			return "Run";
		}

		@Override
		public void invoke() {
			// compile	
			String id = generate();
			// run after successful compilation
			if (id != null) {
				reo = new RunEventObserver(pm.getView());
				run = new Run(app, reo, id);
				run.getObservable().addObserver(WorkflowExecution.this);
				run.running();
				cancel.enable();
				app.getWindowSystem().disableAction(component, this);
			} 
		}
	}

	private final Generator prof;
	private final PresentationModel pm;
	private Run run;
	private RunEventObserver reo;
	private final CancelAction cancel;
	@SuppressWarnings("unused")
	private final Object semanticsToolInstance;


	public WorkflowExecution(Application app, Pair<MutableWorkflow, Database> phd, Generator prof, RunConfig rc)
			throws TypeCheckingException {
		super(app, phd);
		this.prof = prof;

		synA = new SyntaxAnalysis();
		semA = new SemanticAnalysis();
		synA.getSyntaxChangedObservable().addObserver(semA);
		database.getObservable().addObserver(semA);
		synA.checkWorkflow(phd.fst);
		semA.notify(new Databases.CursorChange<Database>(database));

		pm = new PresentationModel(view);
		cancel = new CancelAction();

		// setup component design
		component = (mxGraphComponent) pm.getVisualization().getGraphComponent();
		component = new MyMxGraphComponent(pm.getVisualization().getGraph());
		component.setConnectable(false);
		component.setDragEnabled(false);
		configureComponent();
		component.setName(phd.fst.getName() + "Execution");

		setupOutline();

		// add inspector
		ElementEditorFactories eefs = new ElementEditorFactories();
		LinkedList<SemanticsToolFactory> srep = new LinkedList<SemanticsToolFactory>();
		srep.add(new InspectorTool(eefs));
		SemanticsTool semanticsTool = new SemanticsTool(srep);
		semanticsToolInstance = semanticsTool.instantiate(this);

		// add Menu-Actions
		addAction(new RunAction(), "player-time", KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK), 0);
		addAction(cancel, "process-stop", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), 1);
		cancel.disable();
		addAction(new ResetZoomAction(), KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK), 3);
		addAction(new CloseWorkflowAction(), KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK), 2);

		// addComponent
		app.getWindowSystem().addContentWindow(null, component, null);

		// focus window
		app.getWindowSystem().focusContentWindow(component);
		component.requestFocusInWindow();
		// init outline painting
		outline.updateScaleAndTranslate();

	}

	private String generate() {
		try {
			return prof.generate(view.getWorkflow(), synA, semA);
		} catch (IOException e) {
			app.sendMessage(new ExceptionMessage(e));
		}
		return null;
	}

	public JComponent getComponent() {
		return component;
	}

	@Override
	public void cancelled() {
		cancel.disable();
		reo = null;
	}

	@Override
	public void done() {
		cancel.disable();
		reo = null;
	}

	@Override
	public void progress(int progress) {
		// do nothing 
	}

	@Override
	public void ready() {
		// do nothing
	}

	@Override
	public void running() {
		cancel.enable();
	}

	@Override
	public void notify(RunEvent event) {
		event.doNotify(this);
	}

}