package org.vanda.runner;

import java.io.IOException;

import org.vanda.fragment.impl.GeneratorImpl;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.Profile;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.data.Databases.CursorChange;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.Workflows.UpdatedEvent;
import org.vanda.workflows.run.BuildContext;
import org.vanda.workflows.run.Runner;

public class BuildContextImpl implements BuildContext {
	
	private static class BuildInnerContext {
		private final RunConfig rc;
		private final MutableWorkflow wf;
		private final SyntaxAnalysis synA = new SyntaxAnalysis();
		private final SemanticAnalysis semA = new SemanticAnalysis();
		
		public BuildInnerContext(MutableWorkflow wf, Database db, RunConfig rc) {
			this.rc = rc;
			this.wf = wf;
			synA.notify(new UpdatedEvent<MutableWorkflow>(wf));
			semA.notify(new CursorChange<Database>(db));
			semA.notify(synA);	
		}
		
		public Runner build(Profile prof) {
			Generator gen = new GeneratorImpl(prof, rc.getPath());
			String id;
			try {
				id = gen.generate(wf, synA, semA);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return new RunnerImpl(id);
		}
		
		public void cleanTempFiles() {
			// TODO implement
		}
		
		public void clean() {
			cleanTempFiles();
			// now clean output files
			// TODO implement
		}
	}

	private final RunConfig rc;
	private final Profile prof;
	
	public BuildContextImpl(Profile prof, String defaultPath) {
		this.prof = prof;
		rc = new RunConfig(defaultPath);
	}
	
	@Override
	public void clean(MutableWorkflow wf, Database db) {
		new BuildInnerContext(wf, db, rc).clean();
	}
	
	@Override
	public void cleanTempFiles(MutableWorkflow wf, Database db) {
		new BuildInnerContext(wf, db, rc).cleanTempFiles();
	}

	@Override
	public Runner build(MutableWorkflow wf, Database db) {
		return new BuildInnerContext(wf, db, rc).build(prof);
	}

	@Override
	public void loadSettings(String pathToWorkflow) {
		// TODO implement
	}

	@Override
	public void saveSettings(String pathToWorkflow) {
		// TODO implement
	}
	
	public RunConfig getRunConfig() {
		return rc;
	}

}