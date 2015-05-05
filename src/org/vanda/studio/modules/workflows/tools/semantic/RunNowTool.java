package org.vanda.studio.modules.workflows.tools.semantic;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.vanda.fragment.model.Generator;
import org.vanda.run.RunStates;
import org.vanda.run.RunStates.RunEvent;
import org.vanda.run.RunStates.RunEventId;
import org.vanda.run.RunStates.RunEventListener;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.run.Run;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;
import org.vanda.view.JobView;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
import org.vanda.workflows.data.DataflowAnalysis;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class RunNowTool implements SemanticsToolFactory {

	public static class ClearWorkflowDirectoryAction implements Action {
		private WorkflowEditor wfe;

		public ClearWorkflowDirectoryAction(WorkflowEditor wfe) {
			this.wfe = wfe;
		}

		@Override
		public String getName() {
			return "Clear Workflow directory";
		}

		@Override
		public void invoke() {
			File d = new File(wfe.getApplication().getProperty("outputPath"));
			if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(wfe.getApplication().getWindowSystem()
					.getMainWindow(), "Do you want to empty " + d.getAbsolutePath() + "?", "Empty working directory",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[] {
							"Empty directory!", "No, leave it." }, "default")) {
				if (!emptyDirectory(d))
					JOptionPane.showMessageDialog(wfe.getApplication().getWindowSystem().getMainWindow(),
							"Deletion failed.");
			}
		}

		static public boolean emptyDirectory(File dir) {
			boolean success = true;
			if (dir == null)
				return false;
			if (dir.exists()) {
				for (File f : dir.listFiles()) {
					if (f.isDirectory()) {
						success = success & emptyDirectory(f);
						success = success & f.delete();
					} else {
						success = success & f.delete();
					}
				}
			}
			return success;
		}

	}

	public static Map<String, Job> createIdMap(MutableWorkflow wf, DataflowAnalysis dfa) {
		Map<String, Job> result = new HashMap<String, Job>();
		for (Job j : wf.getChildren()) {
			result.put(dfa.getJobId(j), j);
		}
		return result;
	}

	private class Tool implements Observer<RunEvent>, RunEventListener {

		private final RunEvent READY = new RunStates.RunStateReady();

		private class RunEventObserver implements Observer<RunEventId> {

			private final Map<String, Job> idMap;
			public final String currentRow;
			private final Map<Job, RunEvent> jobStates;

			public RunEventObserver(String currentRow) {
				this.currentRow = currentRow;
				idMap = createIdMap(view.getWorkflow(), semA.getDFA());
				jobStates = new HashMap<Job, RunEvent>();
			}

			@Override
			public void notify(RunEventId event) {
				Job j = idMap.get(event.getId());
				// j can be null because not every id is a job id (e.g., the
				// workflow itself)
				if (j != null) {
					jobStates.put(j, event.getEvent());
					view.getJobView(j).notify(event.getEvent());
				}
			}
		}

		private final class DbListener implements Observer<DatabaseEvent<Database>>, DatabaseListener<Database> {

			@Override
			public void cursorChange(Database d) {
				if (reo != null) {
					String row = d.get(0);
					boolean isCurrent = row.equals(reo.currentRow);
					for (Map.Entry<Job, RunEvent> jobState : reo.jobStates.entrySet()) {
						JobView jobView = view.getJobView(jobState.getKey());
						if (isCurrent)
							jobView.notify(jobState.getValue());
						else
							jobView.notify(READY);
					}
				} else {
					// get information from the filesystem?
				}
			}

			@Override
			public void dataChange(Database d, Integer key) {
			}

			@Override
			public void nameChange(Database d) {
			}

			@Override
			public void notify(DatabaseEvent<Database> event) {
				event.doNotify(this);
			}

		}

		private final class CancelAction implements Action {
			@Override
			public String getName() {
				return "Cancel";
			}

			@Override
			public void invoke() {
				run.cancel();
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
					for (Job j : synA.getSorted())
						view.getJobView(j).notify(READY);
					reo = new RunEventObserver(wfe.getDatabase().get(0));
					run = new Run(app, id);
					run.getObservableId().addObserver(reo);
					run.getObservable().addObserver(Tool.this);
					run.run();
					wfe.enableAction(cancelAction);
					wfe.disableAction(this);
				}
			}
		}

		private final Application app;
		private final WorkflowEditor wfe;
		private final View view;
		private final SemanticAnalysis semA;
		private final SyntaxAnalysis synA;
		private Run run;
		private RunEventObserver reo;
		private final RunAction runAction;
		private final CancelAction cancelAction;
		private final DbListener dbListener;

		public Tool(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA) {
			app = wfe.getApplication();
			this.wfe = wfe;
			view = wfe.getView();
			this.synA = synA;
			this.semA = semA;
			cancelAction = new CancelAction();
			runAction = new RunAction();
			dbListener = new DbListener();
			wfe.getDatabase().getObservable().addObserver(dbListener);
			wfe.addAction(runAction, "player-time", KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK), 0);
			wfe.addAction(cancelAction, "process-stop", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), 1);
			wfe.addAction(new ClearWorkflowDirectoryAction(wfe), "run-build-clean",
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), 3);
			wfe.disableAction(cancelAction);
		}

		private String generate() {
			try {
				return prof.generate(wfe.getView().getWorkflow(), synA, semA);
			} catch (IOException e) {
				app.sendMessage(new ExceptionMessage(e));
			}
			return null;
		}

		@Override
		public void cancelled() {
			wfe.disableAction(cancelAction);
			wfe.enableAction(runAction);
			reo = null;
		}

		@Override
		public void done() {
			wfe.disableAction(cancelAction);
			wfe.enableAction(runAction);
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
			wfe.enableAction(cancelAction);
		}

		@Override
		public void notify(RunEvent event) {
			event.doNotify(this);
		}
	}

	private final Generator prof;

	public RunNowTool(Generator prof) {
		this.prof = prof;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, View view) {
		return new Tool(wfe, synA, semA);
	}

}
