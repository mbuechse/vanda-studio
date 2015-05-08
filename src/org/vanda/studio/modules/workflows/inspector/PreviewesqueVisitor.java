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
import org.vanda.util.PreviewFactory;
import org.vanda.util.Repository;
import org.vanda.view.View;
import org.vanda.view.Views.*;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class PreviewesqueVisitor implements SelectionVisitor {

	private final SemanticAnalysis semA;
	private final SyntaxAnalysis synA;
	private AbstractPreviewFactory apf;
	
	private static final Type LOGTYPE = new CompositeType("log");

	public PreviewesqueVisitor(SemanticAnalysis semA, SyntaxAnalysis synA) {
		this.semA = semA;
		this.synA = synA;
		apf = null;
	}

	public static AbstractPreviewFactory createPreviewFactory(SemanticAnalysis semA, SyntaxAnalysis synA, View view) {
		PreviewesqueVisitor visitor = new PreviewesqueVisitor(semA, synA);
		// Show Workflow-Preview in case of multi-selection
		List<SelectionObject> sos = view.getCurrentSelection();
		if (sos.size() > 1)
			visitor.visitWorkflow(view.getWorkflow());
		else
			for (SelectionObject so : sos)
				so.visit(visitor, view.getWorkflow());
		return visitor.getPreviewFactory();
	}

	public AbstractPreviewFactory getPreviewFactory() {
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
		apf = new AbstractPreviewFactory() {
			
			@Override
			public JComponent createPreview(Repository<Type, PreviewFactory> previewFactories) {
				String log = semA.getDFA().getJobId(j) + ".log";
				return previewFactories.getItem(LOGTYPE).createPreview(log);
			}
			
			// @Override
			public JComponent createButtons(final Repository<Type, PreviewFactory> previewFactories) {
				final String log = semA.getDFA().getJobId(j) + ".log";
				JPanel pan = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				JButton bOpen = new JButton(new AbstractAction("raw log") {
					private static final long serialVersionUID = 2959913172246062587L;

					@Override
					public void actionPerformed(ActionEvent e) {
						previewFactories.getItem(LOGTYPE).openEditor(log);
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
		apf = new AbstractPreviewFactory() {
			@Override
			public JComponent createPreview(Repository<Type, PreviewFactory> previewFactories) {
				return previewFactories.getItem(type).createPreview(value);
			}

			// @Override
			public JComponent createButtons(final Repository<Type, PreviewFactory> previewFactories) {
				JPanel pan = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				JButton bOpen = new JButton(new AbstractAction("edit") {
					private static final long serialVersionUID = 2959913172246062587L;

					@Override
					public void actionPerformed(ActionEvent e) {
						previewFactories.getItem(type).openEditor(value);
					}
				});
				pan.add(bOpen, gbc);
				return pan;
			}
		};

	}

}