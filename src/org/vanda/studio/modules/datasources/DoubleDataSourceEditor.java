package org.vanda.studio.modules.datasources;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DoubleDataSource;
import org.vanda.util.FactoryRegistry;

public class DoubleDataSourceEditor implements DataSourceEditor {

	private final DoubleDataSource doubleDataSource;

	DoubleDataSourceEditor(DoubleDataSource doubleDataSource) {
		this.doubleDataSource = doubleDataSource;
	}

	@Override
	public JComponent getComponent() {
		return new JLabel("DoubleDataSource");
	}

	@Override
	public DataSource getDataSource() {
		return this.doubleDataSource;
	}

	@Override
	public void write() {
		// Do nothing.
	}
	
	public static class Factory implements FactoryRegistry.Factory<DataSource, DataSourceEditor> {
		@Override
		public DataSourceEditor instantiate(DataSource d) {
			return new DoubleDataSourceEditor((DoubleDataSource) d);
		}
	}

}