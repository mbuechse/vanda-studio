package org.vanda.studio.modules.inspector;

import java.util.List;

import javax.swing.JComponent;

import org.vanda.util.Factory;
import org.vanda.view.View;
import org.vanda.view.Views.*;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.ElementVisitor;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public final class EditorialVisitor implements SelectionVisitor {

	private final ElementEditorFactories eefs;
	private Factory<Database, JComponent> editorFactory = null;

	public EditorialVisitor(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	public void visitWorkflow(final MutableWorkflow wf) {
		editorFactory = new Factory<Database, JComponent>() {
			@Override
			public JComponent instantiate(Database d) {
				return eefs.workflowFactories.createEditor(d, wf, wf);
			}
		};
	}

	@Override
	public void visitConnection(final MutableWorkflow wf, final ConnectionKey cc) {
		editorFactory = new Factory<Database, JComponent>() {
			@Override
			public JComponent instantiate(Database d) {
				return eefs.connectionFactories.createEditor(d, wf, cc);
			}
		};
	}

	@Override
	public void visitJob(final MutableWorkflow wf, final Job j) {
		j.visit(new ElementVisitor() {

			@Override
			public void visitLiteral(final Literal l) {
				editorFactory = new Factory<Database, JComponent>() {
					@Override
					public JComponent instantiate(Database d) {
						return eefs.literalFactories.createEditor(d, wf, l);
					}
				};
			}

			@Override
			public void visitTool(final Tool t) {
				editorFactory = new Factory<Database, JComponent>() {
					@Override
					public JComponent instantiate(Database d) {
						return eefs.toolFactories.createEditor(d, wf, t);
					}
				};
			}

		});
	}

	public Factory<Database, JComponent> getEditorFactory() {
		return editorFactory;
	}

	@Override
	public void visitVariable(final MutableWorkflow wf, final Location variable) {
		editorFactory = new Factory<Database, JComponent>() {
			@Override
			public JComponent instantiate(Database d) {
				return eefs.variableFactories.createEditor(d, wf, variable);
			}
		};
	}

	public static Factory<Database, JComponent> createAbstractFactory(ElementEditorFactories eefs, View view) {
		EditorialVisitor visitor = new EditorialVisitor(eefs);
		// Show Workflow-Preview in case of multi-selection
		List<SelectionObject> sos = view.getCurrentSelection();
		if (sos.size() > 1)
			visitor.visitWorkflow(view.getWorkflow());
		else
			for (SelectionObject so : sos)
				so.visit(visitor, view.getWorkflow());
		return visitor.getEditorFactory();
	}

}