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
import org.vanda.workflows.run.Runner;
import org.vanda.workflows.run.RunnerFactory;

public class RunnerFactoryImpl implements RunnerFactory {
	
	private final Profile prof;
	private RunConfig rc = null;

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContact() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class BuildContext {
		private final RunConfig rc;
		private final MutableWorkflow wf;
		private final SyntaxAnalysis synA = new SyntaxAnalysis();
		private final SemanticAnalysis semA = new SemanticAnalysis();
		
		public BuildContext(MutableWorkflow wf, Database db, RunConfig rc) {
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
	
	@Override
	public void clean(MutableWorkflow wf, Database db) {
		new BuildContext(wf, db, rc).clean();
	}
	
	@Override
	public void cleanTempFiles(MutableWorkflow wf, Database db) {
		new BuildContext(wf, db, rc).cleanTempFiles();
	}

	@Override
	public Runner createRunner(MutableWorkflow wf, Database db) {
		return new BuildContext(wf, db, rc).build(prof);
	}
	
	public RunnerFactoryImpl(Profile prof, RunConfig rc) {
		this.prof = prof;
		this.rc = rc;
	}
	
}