package org.vanda.studio.modules.run;

import java.io.IOException;

import org.vanda.fragment.bash.RootLinker;
import org.vanda.fragment.bash.ShellCompiler;
import org.vanda.fragment.bash.ShellTool;
import org.vanda.fragment.bash.ToolLoader;
import org.vanda.fragment.impl.GeneratorImpl;
import org.vanda.fragment.impl.ProfileImpl;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentLinker;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.Profile;
import org.vanda.runner.RunnerImpl;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.modules.run.ProfileManager.ProfileOpener;
import org.vanda.util.Action;
import org.vanda.util.ExternalRepository;
import org.vanda.util.ListRepository;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.CursorChange;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.Workflows.UpdatedEvent;
import org.vanda.workflows.run.Runner;
import org.vanda.workflows.run.RunnerFactory;

public class RunModule implements Module {

	public static final String TOOL_PATH_KEY = "profileToolPath";
	public static final String TOOL_PATH_DEFAULT = System.getProperty("user.home") + "/.vanda/functions/";

	@Override
	public String getName() {
		return "Run module for Vanda Studio";
	}
	
	private static class RunnerFactoryImpl implements RunnerFactory {
		
		private final Profile prof;

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

		@Override
		public Runner createRunner(MutableWorkflow wf, Database db) {
			Generator gen = new GeneratorImpl(prof);
			SyntaxAnalysis synA = new SyntaxAnalysis();
			SemanticAnalysis semA = new SemanticAnalysis();
			synA.notify(new UpdatedEvent<MutableWorkflow>(wf));
			semA.notify(new CursorChange<Database>(db));
			semA.notify(synA);
			String id;
			try {
				id = gen.generate(wf, synA, semA);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return new RunnerImpl(id);
		}
		
		public RunnerFactoryImpl(Profile prof) {
			this.prof = prof;
		}
		
	}
	
	private static class RunModuleInstance {
		
		private final Application app;
		private ProfileManager manager = null;

		public RunModuleInstance(Application app) {
			this.app = app;
			ListRepository<FragmentCompiler> compilers = new ListRepository<FragmentCompiler>();
			compilers.addItem(new ShellCompiler());
			ListRepository<FragmentLinker> linkers = new ListRepository<FragmentLinker>();
			linkers.addItem(new RootLinker());
			ExternalRepository<ShellTool> er;
			String path = app.getProperty(TOOL_PATH_KEY);
			if (path == null) {
				path = TOOL_PATH_DEFAULT;
				app.setProperty(TOOL_PATH_KEY, TOOL_PATH_DEFAULT);
			}
			er = new ExternalRepository<ShellTool>(new ToolLoader(path));
			er.refresh();
			
			Profile profile = new ProfileImpl();
			profile.getFragmentCompilerMetaRepository().addRepository(compilers);
			profile.getFragmentLinkerMetaRepository().addRepository(linkers);
			profile.getFragmentToolMetaRepository().addRepository(er);
			ListRepository<RunnerFactory> repository = new ListRepository<RunnerFactory>();
			repository.addItem(new RunnerFactoryImpl(profile));
			app.getRunnerFactoryMetaRepository().addRepository(repository);
			
		}

		public final class OpenManagerAction implements Action, ProfileOpener {
			@Override
			public String getName() {
				return "Manage Fragment Profiles...";
			}

			@Override
			public void invoke() {
				if (manager == null) {
					// manager = new ProfileManager(app, repository, this);
				}
				manager.focus();
			}

			@Override
			public void closeManager() {
				manager = null;
			}
		}

	}

	@Override
	public Object createInstance(Application app) {
		return new RunModuleInstance(app);
	}

}
