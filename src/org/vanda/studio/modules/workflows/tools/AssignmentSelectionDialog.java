package org.vanda.studio.modules.workflows.tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;

import org.vanda.datasources.RootDataSource;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.DatabaseValueChecker;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.MutableWorkflow;

// TODO make dialog modal, implement MVC pattern, get rid of RemoveMeAsSoonAsPossible
// TODO move correctness checks someplace else
// TODO more synergy with AssignmentTablePanel!!
// this is the effing GUI for crying out loud

/**
 * Dialogue to select assignments
 * 
 * @author kgebhardt
 * 
 */
public class AssignmentSelectionDialog {
	public interface RemoveMeAsSoonAsPossible {
		public void evokeExecution(List<Integer> assignmentSelection);
	}

	private JPanel pan;
	private List<Integer> assignmentSelection;
	private List<JCheckBox> assignmentCheckboxes;
	private Map<Integer, JSpinner> priorityMap;

	public JComponent getComponent() {
		return pan;
	}

	public AssignmentSelectionDialog(final MutableWorkflow mwf, Database db, RootDataSource rds,
			final RemoveMeAsSoonAsPossible r, boolean validWorkflow) {
		// Panel and basic Layout
		pan = new JPanel();
		GroupLayout layout = new GroupLayout(pan);
		pan.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		// AssignmentSelectionTable
		JPanel tablePane = new JPanel();
		GroupLayout tableLayout = new GroupLayout(tablePane);
		tablePane.setLayout(tableLayout);
		assignmentSelection = new ArrayList<Integer>();
		priorityMap = new HashMap<Integer, JSpinner>();
		assignmentCheckboxes = new ArrayList<JCheckBox>();

		ParallelGroup leftColumn = tableLayout.createParallelGroup();
		ParallelGroup rightColumn = tableLayout.createParallelGroup();
		SequentialGroup tableRows = tableLayout.createSequentialGroup();

		// Table Head
		JLabel headerLeft = new JLabel("Assignment");
		JLabel headerRight = new JLabel("Priority");
		leftColumn.addComponent(headerLeft);
		rightColumn.addComponent(headerRight);
		tableRows.addGroup(tableLayout.createParallelGroup().addComponent(headerLeft).addComponent(headerRight));

		// Table Content
		List<Literal> connectedLiterals = null;
		boolean inputsComplete = validWorkflow;
		try {
			connectedLiterals = DatabaseValueChecker.detectConnectedLiterals(mwf);
		} catch (DatabaseValueChecker.MissingInputsException e) {
			inputsComplete = false;
		}

		// TODO remember previous selections and priorities
		for (int i = 0; i < db.getSize(); ++i) {
			final Integer a_i = new Integer(i);
			String name = db.getName(i);
			JCheckBox assignment = new JCheckBox(new AbstractAction(name != null ? name : "") {
				private static final long serialVersionUID = 1827258959703699422L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (assignmentSelection.contains(a_i))
						assignmentSelection.remove(a_i);
					else
						assignmentSelection.add(a_i);
				}
			});
			assignmentSelection.add(a_i);
			assignment.setSelected(assignmentSelection.contains(a_i));
			assignmentCheckboxes.add(assignment);
			boolean selectable = inputsComplete
					&& DatabaseValueChecker.checkDatabaseRow(mwf, rds, db.getRow(i), connectedLiterals);
			JSpinner priority = new JSpinner(new SpinnerNumberModel(i, 0, 1000, 1));

			if (!selectable) {
				assignment.setEnabled(false);
				priority.setEnabled(false);
			}

			priority.setMaximumSize(new Dimension(20, JSpinner.HEIGHT));
			priorityMap.put((Integer) i, priority);
			ParallelGroup row = tableLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(assignment).addComponent(priority);
			leftColumn.addComponent(assignment);
			rightColumn.addComponent(priority);
			tableRows.addGroup(row);
		}
		SequentialGroup tableColumns = tableLayout.createSequentialGroup().addGroup(leftColumn)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, 50)
				.addGroup(rightColumn);

		tableLayout.setHorizontalGroup(tableColumns);
		tableLayout.setVerticalGroup(tableRows);

		tablePane.revalidate();
		tablePane.repaint();
		JScrollPane tableScrollPane = new JScrollPane(tablePane);

		// Select All / None Buttons
		JButton selectAllButton = new JButton(new AbstractAction("select all") {
			private static final long serialVersionUID = -7778511164140696020L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (JCheckBox acb : assignmentCheckboxes) {
					if (!acb.isSelected() && acb.isEnabled()) {
						acb.setSelected(true);
						acb.getAction().actionPerformed(null);
					}
				}

			}
		});

		JButton selectNoneButton = new JButton(new AbstractAction("clear selection") {
			private static final long serialVersionUID = 1004649182223613515L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (JCheckBox acb : assignmentCheckboxes) {
					if (acb.isSelected()) {
						acb.setSelected(false);
						acb.getAction().actionPerformed(null);
					}
				}

			}

		});
		SequentialGroup buttonsHori = layout.createSequentialGroup().addComponent(selectAllButton)
				.addComponent(selectNoneButton);
		ParallelGroup buttonsVert = layout.createParallelGroup().addComponent(selectAllButton)
				.addComponent(selectNoneButton);

		// ExecutionSystem Selection
		// TODO read out available Systems from somewhere
		JLabel exLabel = new JLabel("Execution System");
		JComboBox<String> exSystem = new JComboBox<String>();
		exLabel.setEnabled(false);
		exSystem.setMaximumSize(new Dimension(Short.MAX_VALUE, JComboBox.HEIGHT));
		exSystem.addItem("Shell Compiler");
		exSystem.setEnabled(false);
		JButton exButton = new JButton(new AbstractAction("Open Execution Preview") {
			private static final long serialVersionUID = 3626621817499179974L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Collections.sort(assignmentSelection); // TODO sort by prio
				r.evokeExecution(assignmentSelection);
			}
		});

		SequentialGroup executionSystemHorizontal = layout.createSequentialGroup().addComponent(exLabel)
				.addComponent(exSystem).addComponent(exButton);
		ParallelGroup executionSystemVertical = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(exLabel).addComponent(exSystem).addComponent(exButton);

		// Setup entire layout
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(tableScrollPane).addGroup(buttonsHori)
				.addGroup(executionSystemHorizontal));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(tableScrollPane).addGroup(buttonsVert)
				.addGroup(executionSystemVertical));

	}
}
