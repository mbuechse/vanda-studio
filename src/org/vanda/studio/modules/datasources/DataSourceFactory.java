package org.vanda.studio.modules.datasources;

import org.vanda.datasources.DataSource;
import org.vanda.util.RepositoryItem;

public interface DataSourceFactory extends RepositoryItem {
	DataSource createDataSource();
}
