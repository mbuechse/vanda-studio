package org.vanda.studio.modules.assignments;

import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.KeyStroke;

import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.types.Type;
import org.vanda.util.Action;
import org.vanda.util.Factory;
import org.vanda.util.Repository;
import org.vanda.workflows.data.ExecutableWorkflowBuilder;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.serialization.Storer;

public class ExpandWorkflowTool implements ToolFactory {
	private final Application app;
	private final Repository<Type, Factory<String, Object>> editorFactoryRepository;
	private final Type execution;

	private class Tool {
		/**
		 * Opens a dialog in which the setting for a RunConifg can be assigned.
		 * On execution it creates the RunConfig and opens the
		 * ExecutionPerspetive.
		 * 
		 * @author kgebhardt
		 *
		 */
		public final class RunAction implements Action {

			@Override
			public String getName() {
				return "Expand Workflow...";
			}

			@Override
			public void invoke() {
				Set<Integer> assignmentSelection = new HashSet<Integer>();
				AssignmentSelectionDialog rce = new AssignmentSelectionDialog(app.getWindowSystem().getMainWindow(),
						wfe.getView().getWorkflow(), wfe.getDatabase(), assignmentSelection);
				rce.setLocationRelativeTo(app.getWindowSystem().getMainWindow());
				rce.setAlwaysOnTop(true);
				rce.setModal(true);
				rce.setVisible(true);
				if (rce.doApply) {
					ExecutableWorkflowBuilder ewf = new ExecutableWorkflowBuilder(wfe.getView().getWorkflow(), synA);
					for (Integer i : assignmentSelection)
						ewf.addAssigment(wfe.getDatabase().getRow(i));
					String filePath = app.getProperty("outputPath"); // TODO
																		// revise
					filePath += "/" + ewf.getWorkflow().getName() + new Date().toString();
					try {
						new Storer().store(ewf.getWorkflow(), ewf.getDatabase(), filePath + ".xwf");
						editorFactoryRepository.getItem(execution).instantiate(filePath + ".xwf");
					} catch (Exception e) {
						// wfe.getApplication().sendMessage(new
						// ExceptionMessage(e));
					}
				}
			}
		}

		private WorkflowEditor wfe;
		private SyntaxAnalysis synA;

		public Tool(WorkflowEditor wfe, SyntaxAnalysis synA) {
			this.wfe = wfe;
			this.synA = synA;
			wfe.addAction(new RunAction(), "system-run", KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), 9);
		}
	}

	public ExpandWorkflowTool(Application app, RootDataSource rds,
			Repository<Type, Factory<String, Object>> editorFactoryRepository, Type execution) {
		this.app = app;
		this.editorFactoryRepository = editorFactoryRepository;
		this.execution = execution;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe, wfe.getContext(SyntaxAnalysis.class));
	}

	@Override
	public String getId() {
		return "Workflow expansion editor plugin";
	}

}
