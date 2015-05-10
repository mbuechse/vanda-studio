package org.vanda.swing.datasources;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.DataSourceRepository;
import org.vanda.studio.app.Application;
import org.vanda.util.Factory;
import org.vanda.util.Observer;

public class DataSourceRepositoryEditor implements Observer<DataSourceMount> {

	private final DataSourceRepository dataSourceRepository;
	private final Factory<DataSource, DataSourceEditor> fr;
	private final Collection<DataSourceFactory> dsfs;
	private JPanel editor;
	private DataSourceEditor innerEditor;
	private JPanel innerEditorPanel;
	private JTextField aId;
	private JList<DataSourceMount> lDataSources;
	private JFrame f = null;

	public DataSourceRepositoryEditor(DataSourceRepository dataSourceRepository,
			Factory<DataSource, DataSourceEditor> fr, Collection<DataSourceFactory> dsfs, Application app) {
		this.dataSourceRepository = dataSourceRepository;
		this.fr = fr;
		this.dsfs = dsfs;
		f = new JFrame("Data Source Editor");
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				f = null;
			}
		});
		editor = new JPanel(new GridBagLayout());
		f.setContentPane(editor);
		f.setSize(new Dimension(500, 400));
		f.setLocationRelativeTo(app.getWindowSystem().getMainWindow());
		f.setVisible(true);
		innerEditorPanel = new JPanel(new GridLayout(1, 1));
		aId = new JTextField();
		aId.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DataSourceMount selected = lDataSources.getSelectedValue();
				String newid = aId.getText();
				if (selected != null) {
					DataSourceRepositoryEditor.this.dataSourceRepository.removeItem(selected.id);
					selected = new DataSourceMount(newid, selected.ds);
					DataSourceRepositoryEditor.this.dataSourceRepository.addItem(selected);
					lDataSources.setSelectedValue(selected, true);
				}
			}
			
		});
		lDataSources = new JList<DataSourceMount>();
		lDataSources.setCellRenderer(new DataSourceMountRenderer());
		notify(null);
		lDataSources.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				innerEditorPanel.removeAll();
				aId.setText("");
				DataSourceMount selected = lDataSources.getSelectedValue();
				if (selected != null) {
					DataSource ds = selected.ds;
					innerEditor = DataSourceRepositoryEditor.this.fr.instantiate(ds);
					innerEditorPanel.add(innerEditor.getComponent());
					aId.setText(selected.id);

				}
				editor.revalidate();
			}
		});
		JButton bNew = new JButton(new AbstractAction("new") {
			private static final long serialVersionUID = 7064196111301292429L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String id = "New DataSource";
				// MB: WHAT? "Choose a class" modal dialog? We definitely need a builder here TODO
				// Oh, and probably a view model
				Object[] dsfa = DataSourceRepositoryEditor.this.dsfs.toArray();
				DataSourceFactory type = (DataSourceFactory) JOptionPane.showInputDialog(editor.getParent(),
						"Choose a Class", "Data Source Class", JOptionPane.INFORMATION_MESSAGE, null, dsfa, dsfa[0]);
				if (type != null) {
					DataSourceMount dsm = new DataSourceMount(id, type.createDataSource());
					DataSourceRepositoryEditor.this.dataSourceRepository.addItem(dsm);
					lDataSources.setSelectedValue(dsm, true);
				}
			}
		});
		JButton bRemove = new JButton(new AbstractAction("remove") {
			private static final long serialVersionUID = 2452009339950565899L;

			@Override
			public void actionPerformed(ActionEvent e) {
				DataSourceRepositoryEditor.this.dataSourceRepository.removeItem(lDataSources.getSelectedValue().id);
			}
		});
		JButton bSave = new JButton(new AbstractAction("save") {
			private static final long serialVersionUID = -6511909543313052584L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataSourceRepositoryEditor.this.innerEditor.write();
				DataSourceRepositoryEditor.this.dataSourceRepository.store();
			}
		});
		JButton bCancel = new JButton(new AbstractAction("revert") {
			private static final long serialVersionUID = 6801109424796554027L;

			@Override
			public void actionPerformed(ActionEvent e) {
				DataSourceRepositoryEditor.this.notify(null);
			}
		});
		JLabel lId = new JLabel("ID");

		Insets i = new Insets(2, 2, 2, 2);
		int anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;

		editor.add(lDataSources, new GridBagConstraints(0, 0, 3, 2, 6, 4, anchor, GridBagConstraints.BOTH, i, 1, 1));
		editor.add(bNew, new GridBagConstraints(0, 2, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
		editor.add(bRemove, new GridBagConstraints(2, 2, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
		editor.add(lId, new GridBagConstraints(3, 0, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
		editor.add(aId, new GridBagConstraints(4, 0, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
		editor.add(innerEditorPanel, new GridBagConstraints(3, 1, 2, 1, 6, 3, anchor, GridBagConstraints.BOTH, i, 1, 1));
		editor.add(bSave, new GridBagConstraints(3, 2, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
		editor.add(bCancel, new GridBagConstraints(4, 2, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
		
		dataSourceRepository.getAddObservable().addObserver(this);
		dataSourceRepository.getRemoveObservable().addObserver(this);
	}

	public void bringToFront() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				f.toFront();
				f.repaint();
			}
		});
	}

	public void dispose() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (f != null)
					f.dispose();
			}
		});
	}

	@Override
	public void notify(DataSourceMount event) {
		DataSourceMount selected = lDataSources.getSelectedValue();
		Collection<DataSourceMount> items = dataSourceRepository.getItems();
		DataSourceMount[] itema = items.toArray(new DataSourceMount[items.size()]);
		Arrays.sort(itema, new Comparator<DataSourceMount>() {

			@Override
			public int compare(DataSourceMount o1, DataSourceMount o2) {
				return o1.id.compareTo(o2.id);
			}

		});
		lDataSources.setListData(itema);
		if (selected != null)
			// XXX what happens when selected is gone from the list?
			lDataSources.setSelectedValue(selected, false);
	}
	
	private static class DataSourceMountRenderer implements ListCellRenderer<DataSourceMount> {
		
		DefaultListCellRenderer r;
		
		public DataSourceMountRenderer() {
			r = new DefaultListCellRenderer();
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends DataSourceMount> list, DataSourceMount value,
				int index, boolean isSelected, boolean cellHasFocus) {
			return r.getListCellRendererComponent(list, value.id, index, isSelected, cellHasFocus);
		}
		
	}

}