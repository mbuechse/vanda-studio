package org.vanda.swing.datasources;

import org.vanda.datasources.DataSource;
import org.vanda.util.RepositoryItem;

public interface DataSourceFactory extends RepositoryItem {
	DataSource createDataSource();
}
