package org.vanda.workflows.run;

import org.vanda.datasources.DataSourceMount;
import org.vanda.util.Repository;
import org.vanda.util.RepositoryItem;

public interface BuildSystem extends RepositoryItem {
	BuildContext createBuildContext(Repository<String, DataSourceMount> dataSourceRepository);
}
