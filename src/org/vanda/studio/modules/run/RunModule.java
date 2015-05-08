package org.vanda.studio.modules.run;

import java.util.List;

import org.vanda.fragment.bash.RootLinker;
import org.vanda.fragment.bash.ShellCompiler;
import org.vanda.fragment.bash.ShellTool;
import org.vanda.fragment.bash.ToolLoader;
import org.vanda.fragment.impl.ProfileImpl;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentLinker;
import org.vanda.fragment.model.Profile;
import org.vanda.runner.BuildContextImpl;
import org.vanda.runner.BuildSystemImpl;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.modules.run.ProfileManager.ProfileOpener;
import org.vanda.util.Action;
import org.vanda.util.ExternalRepository;
import org.vanda.util.HasActions;
import org.vanda.util.ListRepository;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.run.BuildContext;
import org.vanda.workflows.run.BuildSystem;
import org.vanda.workflows.run.Runner;

public class RunModule implements Module {

	public static final String TOOL_PATH_KEY = "profileToolPath";
	public static final String TOOL_PATH_DEFAULT = System.getProperty("user.home") + "/.vanda/functions/";

	@Override
	public String getName() {
		return "Run module for Vanda Studio";
	}

	private static class RunModuleInstance {

		private ProfileManager manager = null;
		
		private static class BuildContextGuiImpl implements BuildContext, HasActions {
			
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
		
		private static class BuildSystemGuiImpl implements BuildSystem {
			
			private final BuildSystemImpl delegate;

			@Override
			public String getCategory() {
				return delegate.getCategory();
			}

			@Override
			public String getContact() {
				return delegate.getContact();
			}

			@Override
			public String getDescription() {
				return delegate.getDescription();
			}

			@Override
			public String getName() {
				return delegate.getName();
			}

			@Override
			public String getVersion() {
				return delegate.getVersion();
			}

			@Override
			public String getId() {
				return delegate.getId();
			}

			@Override
			public BuildContext createBuildContext() {
				return new BuildContextGuiImpl(delegate.createBuildContext());
			}
			
			public BuildSystemGuiImpl(BuildSystemImpl delegate) {
				this.delegate = delegate;
			}
			
		}

		public RunModuleInstance(Application app) {
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
			ListRepository<BuildSystem> repository = new ListRepository<BuildSystem>();
			// TODO the runconfig must be workflow-specific
			repository.addItem(new BuildSystemGuiImpl(new BuildSystemImpl(profile, app.getProperty("outputPath"))));
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
