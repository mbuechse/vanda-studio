package org.vanda.swing.datasources;

import javax.swing.JComponent;

import org.vanda.datasources.DataSource;

public interface DataSourceEditor {
	
	public JComponent getComponent();
	
	public DataSource getDataSource();
	
	public void write();
}
