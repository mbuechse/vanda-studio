package org.vanda.studio.modules.workflows.tools.semantic;

import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.tools.semantic.AssignmentSelectionDialog.RemoveMeAsSoonAsPossible;
import org.vanda.util.Action;
import org.vanda.util.PreviewFactory;
import org.vanda.workflows.data.ExecutableWorkflowBuilder;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.serialization.Storer;

public class RunTool implements SemanticsToolFactory {
	private final Application app;
	private final RootDataSource rds;
	private final PreviewFactory executionPreviewFactory;

	private class Tool {
		/**
		 * Opens a dialog in which the setting for a RunConifg can be assigned.
		 * On execution it creates the RunConfig and opens the
		 * ExecutionPerspetive.
		 * 
		 * @author kgebhardt
		 *
		 */
		public final class RunAction implements Action, RemoveMeAsSoonAsPossible {
			private JDialog f;

			@Override
			public String getName() {
				return "Open Execution Preview...";
			}

			@Override
			public void invoke() {
				boolean validWorkflow = synA.getCyclicConnections() == null && synA.getTypeErrors() == null;
				validWorkflow &= semA.getDFA().isConnected();
				// && Types.canUnify(synA.getFragmentType(),
				// prof.getRootType());
				f = new JDialog(app.getWindowSystem().getMainWindow(), "Execute Workflow");
				AssignmentSelectionDialog rce = new AssignmentSelectionDialog(wfe.getView().getWorkflow(),
						wfe.getDatabase(), rds, RunAction.this, validWorkflow);
				f.setContentPane(rce.getComponent());
				f.setAlwaysOnTop(true);
				f.setAutoRequestFocus(true);
				f.setModal(true);
				f.pack();
				f.setLocationRelativeTo(app.getWindowSystem().getMainWindow());
				f.setVisible(true);

			}

			public void evokeExecution(List<Integer> assignmentSelection) {
				f.dispose();

				ExecutableWorkflowBuilder ewf = new ExecutableWorkflowBuilder(wfe.getView().getWorkflow(), synA);
				for (Integer i : assignmentSelection)
					ewf.addAssigment(wfe.getDatabase().getRow(i));
				String filePath = app.getProperty("outputPath");  // TODO revise
				filePath += "/" + ewf.getWorkflow().getName() + new Date().toString();
				try {
					new Storer().store(ewf.getWorkflow(), ewf.getDatabase(), filePath + ".xwf");
					// XXX ServiceLocator antipattern
					executionPreviewFactory.openEditor(filePath + ".xwf");
				} catch (Exception e) {
					// wfe.getApplication().sendMessage(new ExceptionMessage(e));
				}
			}
		}

		private WorkflowEditor wfe;
		private SemanticAnalysis semA;
		private SyntaxAnalysis synA;

		public Tool(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA) {
			this.wfe = wfe;
			this.synA = synA;
			this.semA = semA;
			wfe.addAction(new RunAction(), "system-run", KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), 6);
		}
	}

	public RunTool(Application app, RootDataSource rds, PreviewFactory executionPreviewFactory) {
		this.app = app;
		this.rds = rds;
		this.executionPreviewFactory = executionPreviewFactory;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA) {
		return new Tool(wfe, synA, semA);
	}

}
