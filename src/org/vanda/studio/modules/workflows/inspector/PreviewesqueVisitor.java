package org.vanda.studio.modules.workflows.inspector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.Factory;
import org.vanda.util.Repository;
import org.vanda.view.View;
import org.vanda.view.Views.*;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.run.BuildContext;

public class PreviewesqueVisitor implements SelectionVisitor {

	private final SemanticAnalysis semA;
	private final SyntaxAnalysis synA;
	private final BuildContext bc;
	private Factory<Repository<Type, Factory<String, JComponent>>, JComponent> apf;

	private static final Type LOGTYPE = new CompositeType("log");

	public PreviewesqueVisitor(SemanticAnalysis semA, SyntaxAnalysis synA, BuildContext bc) {
		this.semA = semA;
		this.synA = synA;
		this.bc = bc;
		apf = null;
	}

	public static Factory<Repository<Type, Factory<String, JComponent>>, JComponent> createPreviewFactory(
			SemanticAnalysis semA, SyntaxAnalysis synA, View view, BuildContext bc) {
		PreviewesqueVisitor visitor = new PreviewesqueVisitor(semA, synA, bc);
		// Show Workflow-Preview in case of multi-selection
		List<SelectionObject> sos = view.getCurrentSelection();
		if (sos.size() > 1)
			visitor.visitWorkflow(view.getWorkflow());
		else
			for (SelectionObject so : sos)
				so.visit(visitor, view.getWorkflow());
		return visitor.getPreviewFactory();
	}

	public Factory<Repository<Type, Factory<String, JComponent>>, JComponent> getPreviewFactory() {
		return apf;
	}

	@Override
	public void visitWorkflow(MutableWorkflow wf) {
	}

	@Override
	public void visitConnection(MutableWorkflow wf, ConnectionKey cc) {
		visitVariable(wf, MutableWorkflow.getConnectionValue(cc));
	}

	@Override
	public void visitJob(MutableWorkflow wf, final Job j) {
		if (j.getId() == null)
			return;
		apf = new Factory<Repository<Type, Factory<String, JComponent>>, JComponent>() {

			@Override
			public JComponent instantiate(Repository<Type, Factory<String, JComponent>> previewFactories) {
				String log = bc.findFile("output:" + semA.getDFA().getJobId(j) + ".log");
				return previewFactories.getItem(LOGTYPE).instantiate(log);
			}

			// @Override TODO replace by HasActions something
			@SuppressWarnings("unused")
			public JComponent createButtons(final Repository<Type, Factory<String, Object>> editorFactories) {
				final String log = bc.findFile("output:" + semA.getDFA().getJobId(j) + ".log");
				JPanel pan = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				JButton bOpen = new JButton(new AbstractAction("raw log") {
					private static final long serialVersionUID = 2959913172246062587L;

					@Override
					public void actionPerformed(ActionEvent e) {
						editorFactories.getItem(LOGTYPE).instantiate(log);
					}
				});
				pan.add(bOpen, gbc);
				return pan;
			}
		};
	}

	@Override
	public void visitVariable(MutableWorkflow wf, Location variable) {
		// XXX no support for nested workflows because wf is ignored
		final Type type = synA.getType(variable);
		final String value = semA.getDFA().getValue(variable);
		apf = new Factory<Repository<Type, Factory<String, JComponent>>, JComponent>() {
			@Override
			public JComponent instantiate(Repository<Type, Factory<String, JComponent>> previewFactories) {
				Factory<String, JComponent> pvf = previewFactories.getItem(type);
				if (pvf == null)
					pvf = previewFactories.getItem(null);
				return pvf.instantiate(bc.findFile(value));
			}

			// @Override TODO replace by HasActions something
			@SuppressWarnings("unused")
			public JComponent createButtons(final Repository<Type, Factory<String, Object>> editorFactories) {
				JPanel pan = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				JButton bOpen = new JButton(new AbstractAction("edit") {
					private static final long serialVersionUID = 2959913172246062587L;

					@Override
					public void actionPerformed(ActionEvent e) {
						editorFactories.getItem(type).instantiate(bc.findFile(value));
					}
				});
				pan.add(bOpen, gbc);
				return pan;
			}
		};

	}

}