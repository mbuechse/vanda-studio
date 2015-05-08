package org.vanda.workflows.run;

import org.vanda.util.RepositoryItem;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;

public interface RunnerFactory extends RepositoryItem {

	void clean(MutableWorkflow wf, Database db);

	void cleanTempFiles(MutableWorkflow wf, Database db);
	
	// in analogy to "clean", this performs a "build"
	Runner createRunner(MutableWorkflow wf, Database db);
	
}
