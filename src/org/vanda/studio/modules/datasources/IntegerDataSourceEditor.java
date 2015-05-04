package org.vanda.studio.modules.datasources;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.IntegerDataSource;
import org.vanda.util.FactoryRegistry;

public class IntegerDataSourceEditor implements DataSourceEditor {

	private final IntegerDataSource integerDataSource;

	IntegerDataSourceEditor(IntegerDataSource integerDataSource) {
		this.integerDataSource = integerDataSource;
	}

	@Override
	public JComponent getComponent() {
		return new JLabel("IntegerDataSource");
	}

	@Override
	public DataSource getDataSource() {
		return this.integerDataSource;
	}

	@Override
	public void write() {
		// Do nothing.
	}
	
	public static class Factory implements FactoryRegistry.Factory<DataSource, DataSourceEditor> {
		@Override
		public DataSourceEditor instantiate(DataSource d) {
			return new IntegerDataSourceEditor((IntegerDataSource) d);
		}
	}

}