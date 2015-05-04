package org.vanda.studio.modules.datasources;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DirectoryDataSource;
import org.vanda.datasources.DoubleDataSource;
import org.vanda.studio.app.Application;
import org.vanda.types.Type;
import org.vanda.util.FactoryRegistry;

public class DirectoryDataSourceEditor implements DataSourceEditor {

	private final DirectoryDataSource directoryDataSource;
	private JPanel pan;
	private JTextField tFolder;
	private JTextField tFilter;
	private JComboBox cType;

	public DirectoryDataSourceEditor(DirectoryDataSource directoryDataSource, Application app) {
		this.directoryDataSource = directoryDataSource;
		pan = new JPanel(new GridBagLayout());
		JLabel lFolder = new JLabel("Folder", JLabel.TRAILING);
		tFolder = new JTextField(this.directoryDataSource.path);
		JButton bFolder = new JButton(new AbstractAction("..."){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(new File(tFolder.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            tFolder.setText(file.getAbsolutePath());
		        }
			}
		});
		JLabel lFilter = new JLabel("Filter", JLabel.TRAILING);
		tFilter = new JTextField(this.directoryDataSource.filter);
		JLabel lType = new JLabel("Type", JLabel.TRAILING);
		cType = new JComboBox(app.getTypes().toArray());
		// handle creation of new datasources (type == null)
		if (this.directoryDataSource.type != null) 
			cType.setSelectedItem(this.directoryDataSource.type);
		else if (app.getTypes().size() > 0)
			cType.setSelectedIndex(0);
		pan.add(tFolder);
		pan.add(tFilter);
		
		pan.add(cType);

		GridBagConstraints gbc;
		gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.ABOVE_BASELINE_LEADING,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);
		
		pan.add(lFolder, gbc);
		
		gbc.gridy = 1;
		pan.add(lFilter, gbc);
		
		gbc.gridy = 2;
		pan.add(lType, gbc);
		
		gbc.gridy = 0;
		gbc.gridx = 1;
		gbc.weightx = 1;
		pan.add(tFolder, gbc);
		
		gbc.gridx = 2;
		gbc.weightx = 0;
		pan.add(bFolder, gbc);
		
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.gridx = 1;
		gbc.gridy = 1;
		pan.add(tFilter, gbc);
		
		gbc.gridy = 2;
		pan.add(cType, gbc);
	}

	@Override
	public JComponent getComponent() {
		return pan;
	}

	@Override
	public DataSource getDataSource() {
		return this.directoryDataSource;
	}

	@Override
	public void write() {
		this.directoryDataSource.path = tFolder.getText();
		this.directoryDataSource.filter = tFilter.getText();
		this.directoryDataSource.type = (Type) cType.getSelectedItem();
	}
	
	public static class Factory implements FactoryRegistry.Factory<DataSource, DataSourceEditor> {
		
		private final Application app;
		
		public Factory(Application app) {
			this.app = app;
		}
		
		@Override
		public DataSourceEditor instantiate(DataSource d) {
			return new DirectoryDataSourceEditor((DirectoryDataSource) d, app);
		}
	}

}