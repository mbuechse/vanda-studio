package org.vanda.studio.modules.datasources;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.util.FactoryRegistry;

public class DataSourceRepositoryEditor {

	private final DataSourceRepository dataSourceRepository;
	private final FactoryRegistry<DataSource, DataSourceEditor> fr;
	private final Collection<DataSourceFactory> dsfs;
	private JPanel editor;
	private DataSourceEditor innerEditor;
	private JPanel innerEditorPanel;
	private JTextField aId;
	private JList lDataSources;
	private JFrame f = null;

	public DataSourceRepositoryEditor(DataSourceRepository dataSourceRepository,
			FactoryRegistry<DataSource, DataSourceEditor> fr, Collection<DataSourceFactory> dsfs, Application app) {
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
		lDataSources = new JList();
		notifyMe();
		lDataSources.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				innerEditorPanel.removeAll();
				aId.setText("");
				if (lDataSources.getSelectedIndex() > -1) {
					DataSource ds = DataSourceRepositoryEditor.this.dataSourceRepository.getItem((String) lDataSources
							.getSelectedValue()).ds;
					innerEditor = DataSourceRepositoryEditor.this.fr.registry.get(ds.getClass()).instantiate(ds);
					innerEditorPanel.add(innerEditor.getComponent());
					aId.setText((String) lDataSources.getSelectedValue());

				}
				editor.revalidate();
			}
		});
		JButton bNew = new JButton(new AbstractAction("new") {
			private static final long serialVersionUID = 7064196111301292429L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// MB: TODO I don't think so! Don't mount the data source yet!
				// one might "mount on save"
				String id = "New DataSource";
				// MB: WHAT? "Choose a class" modal dialog? We definitely need a
				// builder here
				// Oh, and probably a view model
				DataSourceFactory[] dsfa = (DataSourceFactory[]) DataSourceRepositoryEditor.this.dsfs.toArray();
				DataSourceFactory type = (DataSourceFactory) JOptionPane.showInputDialog(editor.getParent(),
						"Choose a Class", "Data Source Class", JOptionPane.INFORMATION_MESSAGE, null, dsfa, dsfa[0]);
				if (type != null) {
					DataSourceRepositoryEditor.this.dataSourceRepository.addItem(new DataSourceMount(id, type.createDataSource()));
					notifyMe(id);
				}
			}
		});
		JButton bRemove = new JButton(new AbstractAction("remove") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2452009339950565899L;

			@Override
			public void actionPerformed(ActionEvent e) {
				DataSourceRepositoryEditor.this.dataSourceRepository.removeItem((String) lDataSources
						.getSelectedValue());
				notifyMe(); // TODO convert to observer pattern
			}
		});
		JButton bSave = new JButton(new AbstractAction("save") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6511909543313052584L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataSourceRepositoryEditor.this.innerEditor.write();
				DataSourceRepositoryEditor.this.dataSourceRepository.store();
			}
		});
		JButton bCancel = new JButton(new AbstractAction("revert") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6801109424796554027L;

			@Override
			public void actionPerformed(ActionEvent e) {
				notifyMe();
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
	}

	private void notifyMe() {
		notifyMe(lDataSources.getSelectedValue());
	}

	private void notifyMe(Object id) {
		// MB: TODO use an effing list model! use MVC!
		Object[] a = dataSourceRepository.getKeys().toArray();
		Arrays.sort(a);
		lDataSources.setListData(a);
		int idx = -1;
		for (int i = 0; i < a.length; ++i) {
			if (a[i].equals(id)) {
				idx = i;
				break;
			}
		}
		lDataSources.setSelectedIndex(idx);
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

}