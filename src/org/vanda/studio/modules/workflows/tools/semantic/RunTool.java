package org.vanda.studio.modules.workflows.tools.semantic;

import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.vanda.fragment.model.Generator;
import org.vanda.run.RunConfig;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.run.RunConfigEditor;
import org.vanda.studio.modules.workflows.run.RunConfigEditor.Runner;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.view.View;
import org.vanda.workflows.data.ExecutableWorkflowBuilder;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.serialization.Storer;

public class RunTool implements SemanticsToolFactory {
	public static final Type EXECUTION = new CompositeType("Execution");
	
	private class Tool {
		/**
		 * Opens a dialog in which the setting for a RunConifg can be assigned. 
		 * On execution it creates the RunConfig and opens the ExecutionPerspetive.
		 * @author kgebhardt
		 *
		 */
		public final class RunAction implements Action, Runner {
			private JDialog f;

			@Override
			public String getName() {
				return "Open Execution Preview...";
			}

			@Override
			public void invoke() {
				boolean validWorkflow = synA.getCyclicConnections() == null && synA.getTypeErrors() == null;
				validWorkflow &= semA.getDFA().isConnected()
						&& Types.canUnify(synA.getFragmentType(), prof.getRootType());
				f = new JDialog(wfe.getApplication().getWindowSystem().getMainWindow(), "Execute Workflow");
				RunConfigEditor rce = new RunConfigEditor(wfe.getView().getWorkflow(), wfe.getDatabase(),
						app.getRootDataSource(), app.getProperty("outputPath"), RunAction.this, validWorkflow);
				f.setContentPane(rce.getComponent());
				f.setAlwaysOnTop(true);
				f.setAutoRequestFocus(true);
				f.setModal(true);
				f.pack();
				f.setLocationRelativeTo(app.getWindowSystem().getMainWindow());
				f.setVisible(true);

			}

			public void evokeExecution(List<Integer> assignmentSelection, String filePath) {
				f.dispose();
				
				// TODO: probably obsolete, remove after testing
				// String id = generate();
				// if (id != null) {
				// serialize Workflow + Database
				ExecutableWorkflowBuilder ewf = new ExecutableWorkflowBuilder(wfe.getView().getWorkflow(), synA);
				for (Integer i : assignmentSelection)
					ewf.addAssigment(wfe.getDatabase().getRow(i));
				filePath += "/" + ewf.getWorkflow().getName() + new Date().toString();
				RunConfig rc = new RunConfig(filePath, new HashMap<String, Integer>());
				try {
					new Storer().store(ewf.getWorkflow(), ewf.getDatabase(), filePath + ".xwf");
					new org.vanda.run.serialization.Storer().store(rc, filePath + ".run");
					// XXX ServiceLocator antipattern
					app.getPreviewFactory(EXECUTION).openEditor(filePath + ".xwf");
				} catch (Exception e) {
					wfe.getApplication().sendMessage(new ExceptionMessage(e));
				}
			}
		}

		private Application app;
		private WorkflowEditor wfe;
		private SemanticAnalysis semA;
		private SyntaxAnalysis synA;

		public Tool(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA) {
			this.wfe = wfe;
			this.synA = synA;
			this.semA = semA;
			app = wfe.getApplication();
			wfe.addAction(new RunAction(), "system-run", KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), 4);
		}
	}

	private final Generator prof;

	public RunTool(Generator prof) {
		this.prof = prof;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, View view) {
		return new Tool(wfe, synA, semA);
	}

}
