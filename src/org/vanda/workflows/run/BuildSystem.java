package org.vanda.workflows.run;

import org.vanda.util.RepositoryItem;

public interface BuildSystem extends RepositoryItem {
	BuildContext createBuildContext();
}
