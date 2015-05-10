package org.vanda.studio.modules.workflows.tools;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;

import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ElementVisitor;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

// TODO implement MVC pattern
// TODO more synergy with AssignmentTablePanel!!
// this is the effing GUI for crying out loud

/**
 * Dialog to select assignments
 * 
 * @author kgebhardt
 * 
 */
public class AssignmentSelectionDialog extends JDialog {
	private static final long serialVersionUID = 26881773449612849L;

	public interface RemoveMeAsSoonAsPossible {
		public void evokeExecution(List<Integer> assignmentSelection);
	}

	private JPanel pan;
	private Set<Integer> assignmentSelection;
	private List<JCheckBox> assignmentCheckboxes;
	private Map<Integer, JSpinner> priorityMap;
	public boolean doApply = false;

	public AssignmentSelectionDialog(Window owner, final MutableWorkflow mwf, Database db, Set<Integer> assignmentSelection) {
		super(owner, "Expand Workflow");
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
		this.assignmentSelection = assignmentSelection;
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
		final List<Literal> connectedLiterals = new LinkedList<Literal>();
		for (Job j : mwf.getChildren())
			j.visit(new ElementVisitor() {
				@Override
				public void visitLiteral(Literal l) {
					connectedLiterals.add(l);
				}

				@Override
				public void visitTool(Tool t) {
				}
				
			});

		// TODO remember previous selections and priorities
		for (int i = 0; i < db.getSize(); ++i) {
			final Integer a_i = new Integer(i);
			String name = db.getName(i);
			final JCheckBox assignment = new JCheckBox(new AbstractAction(name != null ? name : "") {
				private static final long serialVersionUID = 1827258959703699422L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (!((JCheckBox) arg0.getSource()).isSelected())
						AssignmentSelectionDialog.this.assignmentSelection.remove(a_i);
					else
						AssignmentSelectionDialog.this.assignmentSelection.add(a_i);
				}
			});
			assignmentSelection.add(a_i);
			assignment.setSelected(assignmentSelection.contains(a_i));
			assignmentCheckboxes.add(assignment);
			JSpinner priority = new JSpinner(new SpinnerNumberModel(i, 0, 1000, 1));

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
		JButton exButton = new JButton(new AbstractAction("Expand Workflow") {
			private static final long serialVersionUID = 3626621817499179974L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				doApply = true;
				AssignmentSelectionDialog.this.dispose();
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

		setContentPane(pan);
		setAutoRequestFocus(true);
		pack();
	}
}
