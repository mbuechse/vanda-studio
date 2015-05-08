package org.vanda.workflows.run;

import org.vanda.util.RepositoryItem;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;

public interface RunnerFactory extends RepositoryItem {

	Runner createRunner(MutableWorkflow wf, Database db);
	
}
