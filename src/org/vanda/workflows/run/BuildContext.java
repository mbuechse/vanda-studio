package org.vanda.workflows.run;

import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;

public interface BuildContext {

	void clean(MutableWorkflow wf, Database db);

	void cleanTempFiles(MutableWorkflow wf, Database db);
	
	Runner build(MutableWorkflow wf, Database db);
	
	// settings may hold several configurations
	void loadSettings(String pathToWorkflow);
	
	void saveSettings(String pathToWorkflow);
	
	// TODO offer settings dialog
	// buildContext has the necessary knowledge
	// but it should be gui-agnostic!

}
