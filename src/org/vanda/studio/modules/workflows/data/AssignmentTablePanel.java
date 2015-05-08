package org.vanda.studio.modules.workflows.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.util.Factory;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.ElementVisitor;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

public class AssignmentTablePanel extends JPanel implements DatabaseListener<Database> {

	private final RootDataSource rds;
	private final Factory<DataSource, ElementSelector> fr;
	private final Database db;
	private final Observer<DatabaseEvent<Database>> dbObserver;
	private final SortedMap<Integer, Literal> literals;

	private class AssignmentTableModel extends AbstractTableModel implements WorkflowListener<MutableWorkflow> {

		private static final long serialVersionUID = -75059113029383402L;
		private final Observer<WorkflowEvent<MutableWorkflow>> workflowObserver;

		protected AbstractTableModel rowHeaderModel;

		private ElementVisitor literalAddedVisitor = new ElementVisitor() {
			@Override
			public void visitTool(Tool t) {
				// do nothing
			}

			@Override
			public void visitLiteral(Literal l) {
				literals.put((Integer) literals.size(), l);
			}
		};

		public AssignmentTableModel(MutableWorkflow w) {
			for (Job j : w.getChildren())
				j.visit(literalAddedVisitor);
			workflowObserver = new Observer<WorkflowEvent<MutableWorkflow>>() {

				@Override
				public void notify(WorkflowEvent<MutableWorkflow> event) {
					event.doNotify(AssignmentTableModel.this);
				}

			};
			w.getObservable().addObserver(workflowObserver);
		}

		@Override
		public int getColumnCount() {
			return literals.size();
		}

		@Override
		public int getRowCount() {
			return db.getSize();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			Object value = db.getRow(arg0).get(literals.get(arg1).getKey());
			if (value == null)
				return "";
			else
				return value;
		}

		@Override
		public String getColumnName(int i) {
			return literals.get(i).getName();
		}

		public int getKey(JTable table) {
			return table.getSelectedColumn();
		}

		@Override
		public void fireTableStructureChanged() {
			super.fireTableStructureChanged();
			rowHeaderModel.fireTableStructureChanged();
		}

		@Override
		public void childAdded(MutableWorkflow mwf, Job j) {
			j.visit(literalAddedVisitor);
			fireTableStructureChanged();
		}

		@Override
		public void childModified(MutableWorkflow mwf, Job j) {
			j.visit(new ElementVisitor() {
				@Override
				public void visitTool(Tool t) {
					// do nothing
				}

				@Override
				public void visitLiteral(Literal l) {
					for (Integer i : literals.keySet()) {
						if (literals.get(i) == l) {
							fireTableStructureChanged();
							break;
						}
					}
				}
			});
		}

		@Override
		public void childRemoved(final MutableWorkflow mwf, Job j) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					literals.clear();
					for (Job j : mwf.getChildren()) {
						j.visit(literalAddedVisitor);
					}
					fireTableStructureChanged();
				}
			});
		}

		@Override
		public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
		}

		@Override
		public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
		}

		@Override
		public void propertyChanged(MutableWorkflow mwf) {
		}

		@Override
		public void updated(MutableWorkflow mwf) {
		}
	}

	private class NormalSelectionListener implements ListSelectionListener {
		private final JTable table;

		public NormalSelectionListener(JTable table) {
			this.table = table;
		}

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (arg0.getLastIndex() == -1 || arg0.getValueIsAdjusting())
				return;
			if (table.getSelectedColumn() == -1 || table.getSelectedRow() == -1)
				return;
			int run = table.getSelectedRow();
			int key = table.getSelectedColumn();
			updateSelection(run, key);
		}
	}

	private static final long serialVersionUID = 4113799454513800879L;
	private static final Color TYPE_ERROR = new Color(Integer.parseInt("FF8686", 16));
	private static final Color VALUE_ERROR = new Color(Integer.parseInt("FFEC8C", 16)); 
	
	private final AssignmentTableModel atm;

	private final JTable table;
	private final JScrollPane tablePane;

	private NormalSelectionListener normalSelList;

	private MouseListener rowHeaderEditingStopMouseListener;

	private final JPanel buttonAndTabelPanel;
	private final JPanel literalEditorPanel;

	public AssignmentTablePanel(MutableWorkflow wf, Database d, RootDataSource rds, Factory<DataSource, ElementSelector> fr) {
		super(new BorderLayout());
		this.rds = rds;
		this.fr = fr;
		this.db = d;
		literals = new TreeMap<Integer, Literal>();

		buttonAndTabelPanel = new JPanel();
		GroupLayout layout = new GroupLayout(buttonAndTabelPanel);
		buttonAndTabelPanel.setLayout(layout);

		// create Table in normal mode
		atm = new AssignmentTableModel(wf);
		table = new JTable(atm);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = -6915713192476941622L;

			@Override
			public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
					int arg5) {
				JLabel comp = (JLabel) super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
				if (isErroneous(arg4, arg5)) {
					comp.setBackground(TYPE_ERROR);
					comp.setToolTipText("Literal and Datasource types do not match.");
				}
				else if (!hasAValue(arg4, arg5)) {
					comp.setBackground(VALUE_ERROR);
					comp.setToolTipText("No value is selected.");
				}
				else {
					comp.setBackground(table.getBackground());
					comp.setToolTipText(null);
				}
					
				return comp;					
			}
		});

		table.setCellSelectionEnabled(true);
		table.setTableHeader(new JTableHeader(table.getColumnModel()) {
			private static final long serialVersionUID = -3125419306047857928L;

			@Override
			public boolean getReorderingAllowed() {
				return false;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension d = super.getMinimumSize();
				d.height = JTableHeader.HEIGHT;
				return d;
			}
		});
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablePane = new JScrollPane(table);
		tablePane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -1142664914807579190L;

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = new JLabel("123").getPreferredSize().height + 4;
				return d;
			}
		});

		// create SelectionListeners
		normalSelList = new NormalSelectionListener(table);

		// add normal Listeners to the table
		table.getSelectionModel().addListSelectionListener(normalSelList);
		table.getColumnModel().getSelectionModel().addListSelectionListener(normalSelList);

		// register DatabaseObserver
		dbObserver = new Observer<DatabaseEvent<Database>>() {

			@Override
			public void notify(DatabaseEvent<Database> event) {
				event.doNotify(AssignmentTablePanel.this);
			}

		};
		db.getObservable().addObserver(dbObserver);

		// add transpose button
		JButton transposeButton = new JButton(new AbstractAction("\u2922") {
			private static final long serialVersionUID = 7260156402014653557L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO reimplement
			}

		});
		transposeButton.setToolTipText("transpose");
		transposeButton.setFont(transposeButton.getFont().deriveFont(14.0f));
		// tablePane.setCorner(JScrollPane.UPPER_LEFT_CORNER, transposeButton);

		// add run button
		JButton addButton = new JButton(new AbstractAction("add run") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				db.addRow(true);
				db.setCursor(db.getSize() - 1);
			}

		});

		// remove run button
		JButton removeButton = new JButton(new AbstractAction("delete run") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				db.delRow();
			}

		});

		// button layout
		ParallelGroup buttonVert = layout.createParallelGroup().addComponent(addButton).addComponent(removeButton);
		SequentialGroup buttonHor = layout.createSequentialGroup().addComponent(addButton)
				.addComponent(removeButton);


		// main layout
		ParallelGroup tableHor = layout.createParallelGroup().addComponent(tablePane).addGroup(buttonHor);
		SequentialGroup tableVert = layout.createSequentialGroup().addComponent(tablePane).addGroup(buttonVert);

		layout.setHorizontalGroup(tableHor);
		layout.setVerticalGroup(tableVert);
		literalEditorPanel = new JPanel(new BorderLayout());

		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonAndTabelPanel, literalEditorPanel),
				BorderLayout.CENTER);
	}

	public boolean isErroneous(int arg0, int arg1) {
		Literal lit = literals.get(arg1);
		String val = db.getRow(arg0).get(lit.getKey());
		if (val == null)
			return true;
		return !rds.getType(val).equals(lit.getType());
	}
	
	public boolean hasAValue(int arg0, int arg1) {
		Literal lit = literals.get(arg1);
		String val = db.getRow(arg0).get(lit.getKey());
		if (val == null)
			return false;
		int i = val.indexOf(':');
		if (i == -1 || i == val.length() - 1)
			return false;
		return true;
	}

	public void updateAll() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				atm.fireTableDataChanged();
				atm.fireTableStructureChanged();
				table.repaint();
				tablePane.updateUI();
				updateUI();
			}
		});
	}

	@Override
	public void cursorChange(Database d) {
		int key = ((AssignmentTableModel) table.getModel()).getKey(table);
		if (key != -1) {
			selectEntry(d.getCursor(), key);
		} else {
			updateSelection(d.getCursor(), -1);
		}

	}

	@Override
	public void dataChange(Database d, Integer key) {
		updateAll();
	}

	@Override
	public void nameChange(Database d) {
		updateAll();
	}

	private void updateSelection(int run, int key) {
		// FIXME System.out.println("run: " + run + " key: " + key);
		if (db.getCursor() != run && -1 < run && run < db.getSize()) {
			db.setCursor(run);
		}
		literalEditorPanel.removeAll();
		if (key == -1) {
			JLabel text = new JLabel("Please select a literal.");
			text.addMouseListener(rowHeaderEditingStopMouseListener);
			literalEditorPanel.add(text);
		} else if (literals.get(key) != null) {
			literalEditorPanel.add(new LiteralEditorPanel(db, literals.get(key), rds, fr));
		} else {
			JLabel text = new JLabel("There exists no such literal.");
			text.addMouseListener(rowHeaderEditingStopMouseListener);
			literalEditorPanel.add(text);
		}
		revalidate();
	}

	public void selectEntry(int row, int column) {
		table.getSelectionModel().setSelectionInterval(row, row);
		table.getColumnModel().getSelectionModel().setSelectionInterval(column, column);
	}
}