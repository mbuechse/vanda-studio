package org.vanda.studio.modules.workflows.tools;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vanda.studio.modules.workflows.model.WorkflowDecoration;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Observer;

public class DebuggerTool implements ToolFactory {

	private static final class Tool {
		private final WorkflowEditor wfe;
		private final WorkflowDecoration m;
		private final JTextArea debugger;
		private final JScrollPane therealdebugger;

		public Tool(WorkflowEditor wfe) {
			this.wfe = wfe;
			this.m = wfe.getWorkflowDecoration(); // XXX better not cache this
			debugger = new JTextArea();
			this.m.getWorkflowCheckObservable().addObserver(
					new Observer<WorkflowDecoration>() {
						@Override
						public void notify(WorkflowDecoration event) {
							update(event);
						}
					});
			therealdebugger = new JScrollPane(debugger);
			therealdebugger.setName("Debugger");
			this.wfe.addToolWindow(therealdebugger);
		}

		public void update(WorkflowDecoration model) {
//			StringBuilder sb = new StringBuilder();
//			model.getFrozen().appendText(sb);
//			if (!model.getFrozen().isSane()) {
//				sb.append("Warning: Your workflow(s) are not executable!\n"
//						+ "The most likely reason is that some input port "
//						+ "is not connected.\n\n");
//			}
//			debugger.setText(sb.toString());
		}

	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe);
	}

}
