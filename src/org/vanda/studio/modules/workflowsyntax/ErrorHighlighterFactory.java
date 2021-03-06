package org.vanda.studio.modules.workflowsyntax;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class ErrorHighlighterFactory implements ToolFactory {
	/**
	 * Sets and resets syntax-error and top-sort-error-highlighting in the View.
	 * @author kgebhardt
	 *
	 */
	public class ErrorHighlighter implements Observer<SyntaxAnalysis> {

		private final View view;
		private Set<ConnectionKey> markedConnections;

		public ErrorHighlighter(View view) {
			this.view = view;
		}

		@Override
		public void notify(SyntaxAnalysis synA) {
			Collection<ConnectionKey> cycles = synA.getCyclicConnections();
			List<Pair<String, Set<ConnectionKey>>> errors = synA.getTypeErrors();
			if (errors != null) {
				HashSet<ConnectionKey> allErrors = new HashSet<ConnectionKey>();
				for (Pair<String, Set<ConnectionKey>> error : errors) {
					// TODO use new color in each iteration
					Set<ConnectionKey> eqs = error.snd;
					for (ConnectionKey eq : eqs) {
						view.getConnectionView(eq).setMarked(true);
						allErrors.add(eq);
					}
				}
				if (markedConnections != null) {
					markedConnections.removeAll(allErrors);
					for (ConnectionKey cc : markedConnections)
						view.getConnectionView(cc).setMarked(false);
				}
				markedConnections = allErrors;
			} else if (cycles != null) {
				for (ConnectionKey cc : cycles) {
					if (markedConnections == null)
						markedConnections = new HashSet<ConnectionKey>();
					if (!markedConnections.contains(cc)) {
						markedConnections.add(cc);
						view.getConnectionView(cc).setMarked(true);
					}
				}
			} else {
				// remove ErrorHighlighting
				if (markedConnections != null) {
					for (ConnectionKey cc : markedConnections)
						if (view.getConnectionView(cc) != null)
							view.getConnectionView(cc).setMarked(false);
					markedConnections = null;
				}
			}
		}
	}
	
	@Override
	public Object instantiate(WorkflowEditor wfe) {
		ErrorHighlighter eh = new ErrorHighlighter(wfe.getView());
		wfe.getContext(SyntaxAnalysis.class).getSyntaxChangedObservable().addObserver(eh);
		return eh;
	}

	@Override
	public String getId() {
		return "Error highlighter editor plugin";
	}

}
