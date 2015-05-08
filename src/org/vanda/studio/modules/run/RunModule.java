package org.vanda.studio.modules.run;

import org.vanda.fragment.bash.RootLinker;
import org.vanda.fragment.bash.ShellCompiler;
import org.vanda.fragment.bash.ShellTool;
import org.vanda.fragment.bash.ToolLoader;
import org.vanda.fragment.impl.ProfileImpl;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentLinker;
import org.vanda.fragment.model.Profile;
import org.vanda.runner.BuildSystemImpl;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.modules.run.ProfileManager.ProfileOpener;
import org.vanda.util.Action;
import org.vanda.util.ExternalRepository;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;
import org.vanda.workflows.run.BuildSystem;

public class RunModule implements Module {

	public static final String TOOL_PATH_KEY = "profileToolPath";
	public static final String TOOL_PATH_DEFAULT = System.getProperty("user.home") + "/.vanda/functions/";
	
	private final MetaRepository<String, BuildSystem> buildMeta;
	
	public RunModule(MetaRepository<String, BuildSystem> buildMeta) {
		this.buildMeta = buildMeta;
	}

	@Override
	public String getName() {
		return "Run module for Vanda Studio";
	}

	private class RunModuleInstance {

		private ProfileManager manager = null;
		
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
			buildMeta.addRepository(repository);

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
