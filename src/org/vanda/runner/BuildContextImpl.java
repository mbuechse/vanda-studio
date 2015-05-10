package org.vanda.runner;

import java.io.IOException;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.RootDataSource;
import org.vanda.fragment.impl.GeneratorImpl;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.Profile;
import org.vanda.types.Type;
import org.vanda.util.CompositeRepository;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.Repository;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.data.Databases.CursorChange;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.Workflows.UpdatedEvent;
import org.vanda.workflows.run.BuildContext;
import org.vanda.workflows.run.Runner;

public class BuildContextImpl implements BuildContext {

	private class BuildInnerContext {
		private final MutableWorkflow wf;
		private final SyntaxAnalysis synA = new SyntaxAnalysis();
		private final SemanticAnalysis semA = new SemanticAnalysis();

		public BuildInnerContext(MutableWorkflow wf, Database db) {
			this.wf = wf;
			synA.notify(new UpdatedEvent<MutableWorkflow>(wf));
			semA.notify(new CursorChange<Database>(db));
			semA.notify(synA);
		}

		public Runner build(Profile prof) {
			Generator gen = new GeneratorImpl(prof, rc.getPath(), rootDataSource);
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
	private final DataSource outputDataSource = new DataSource() {

		@Override
		public String getValue(String element) {
			return rc.getPath() + element;
		}

		@Override
		public Type getType(String element) {
			return null;
		}

		@Override
		public String createElement() {
			return "";
		}

	};
	private final RootDataSource rootDataSource;

	public BuildContextImpl(Profile prof, String defaultPath, Repository<String, DataSourceMount> dataSourceRepository) {
		this.prof = prof;
		rc = new RunConfig(defaultPath);
		MetaRepository<String, DataSourceMount> mr = new CompositeRepository<String, DataSourceMount>();
		ListRepository<DataSourceMount> lr = new ListRepository<DataSourceMount>();
		lr.addItem(new DataSourceMount("output", outputDataSource));
		mr.addRepository(lr);
		mr.addRepository(dataSourceRepository);
		rootDataSource = new RootDataSource(mr.getRepository());
	}

	@Override
	public void clean(MutableWorkflow wf, Database db) {
		new BuildInnerContext(wf, db).clean();
	}

	@Override
	public void cleanTempFiles(MutableWorkflow wf, Database db) {
		new BuildInnerContext(wf, db).cleanTempFiles();
	}

	@Override
	public Runner build(MutableWorkflow wf, Database db) {
		return new BuildInnerContext(wf, db).build(prof);
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

	@Override
	public String findFile(String value) {
		return rootDataSource.getValue(value);
	}

}