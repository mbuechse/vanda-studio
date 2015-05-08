package org.vanda.studio.modules.run;

import java.util.List;

import org.vanda.runner.BuildContextImpl;
import org.vanda.util.Action;
import org.vanda.util.HasActions;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.run.BuildContext;
import org.vanda.workflows.run.Runner;

public class BuildContextGuiImpl implements BuildContext, HasActions {
	
	private final BuildContextImpl delegate;
	private final Action editAction = new Action() {

		@Override
		public String getName() {
			return "Edit run configuration";
		}

		@Override
		public void invoke() {
			new RunConfigEditor(delegate.getRunConfig());
		}
		
	};

	@Override
	public void clean(MutableWorkflow wf, Database db) {
		delegate.clean(wf, db);
	}

	@Override
	public void cleanTempFiles(MutableWorkflow wf, Database db) {
		delegate.cleanTempFiles(wf, db);
	}

	@Override
	public Runner build(MutableWorkflow wf, Database db) {
		return delegate.build(wf, db);
	}

	@Override
	public void loadSettings(String pathToWorkflow) {
		delegate.loadSettings(pathToWorkflow);
	}

	@Override
	public void saveSettings(String pathToWorkflow) {
		delegate.saveSettings(pathToWorkflow);
	}

	@Override
	public void appendActions(List<Action> as) {
		as.add(editAction);
	}
	
	public BuildContextGuiImpl(BuildContextImpl delegate) {
		this.delegate = delegate;
	}
	
}