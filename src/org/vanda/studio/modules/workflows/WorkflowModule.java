package org.vanda.studio.modules.workflows;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DirectoryDataSource;
import org.vanda.datasources.DoubleDataSource;
import org.vanda.datasources.IntegerDataSource;
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
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.modules.workflows.data.DirectorySelector;
import org.vanda.studio.modules.workflows.data.DoubleSelector;
import org.vanda.studio.modules.workflows.data.ElementSelector;
import org.vanda.studio.modules.workflows.data.IntegerSelector;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.inspector.LiteralEditor;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.tools.AssignmentSwitchToolFactory;
import org.vanda.studio.modules.workflows.tools.AssignmentTableToolFactory;
import org.vanda.studio.modules.workflows.tools.ErrorHighlighterFactory;
import org.vanda.studio.modules.workflows.tools.MainComponentToolFactory;
import org.vanda.studio.modules.workflows.tools.PaletteTool;
import org.vanda.studio.modules.workflows.tools.SaveTool;
import org.vanda.studio.modules.workflows.tools.WorkflowToPDFToolFactory;
import org.vanda.studio.modules.workflows.tools.semantic.InspectorTool;
import org.vanda.studio.modules.workflows.tools.semantic.ProfileManager;
import org.vanda.studio.modules.workflows.tools.semantic.RunNowTool;
import org.vanda.studio.modules.workflows.tools.semantic.RunTool;
import org.vanda.studio.modules.workflows.tools.semantic.SemanticsTool;
import org.vanda.studio.modules.workflows.tools.semantic.SemanticsToolFactory;
import org.vanda.studio.modules.workflows.tools.semantic.ProfileManager.ProfileOpener;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.Action;
import org.vanda.util.ExternalRepository;
import org.vanda.util.CompositeFactory;
import org.vanda.util.ListRepository;

public class WorkflowModule implements Module {

	public static final Type WORKFLOW = new CompositeType("Workflow");

	@Override
	public Object createInstance(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}

	protected static final class WorkflowModuleInstance {

		private final Application app;
		private final ElementEditorFactories eefs;
		private final ListRepository<Profile> repository;
		private final Profile profile;
		private ProfileManager manager;

		public static final String TOOL_PATH_KEY = "profileToolPath";
		public static final String TOOL_PATH_DEFAULT = System.getProperty("user.home") + "/.vanda/functions/";

		public WorkflowModuleInstance(Application a) {
			app = a;
			profile = new ProfileImpl();
			ListRepository<FragmentCompiler> compilers = new ListRepository<FragmentCompiler>();
			compilers.addItem(new ShellCompiler());
			profile.getFragmentCompilerMetaRepository().addRepository(compilers);
			ListRepository<FragmentLinker> linkers = new ListRepository<FragmentLinker>();
			linkers.addItem(new RootLinker());
			profile.getFragmentLinkerMetaRepository().addRepository(linkers);
			repository = new ListRepository<Profile>();
			repository.addItem(profile);
			manager = null;

			ExternalRepository<ShellTool> er;
			String path = app.getProperty(TOOL_PATH_KEY);
			if (path == null) {
				path = TOOL_PATH_DEFAULT;
				app.setProperty(TOOL_PATH_KEY, TOOL_PATH_DEFAULT);
			}
			er = new ExternalRepository<ShellTool>(new ToolLoader(path));
			profile.getFragmentToolMetaRepository().addRepository(er);
			er.refresh();

			CompositeFactory<DataSource, ElementSelector> fr = new CompositeFactory<DataSource, ElementSelector>();
			fr.put(DoubleDataSource.class, new DoubleSelector.Fäctory());
			fr.put(IntegerDataSource.class, new IntegerSelector.Fäctory());
			fr.put(DirectoryDataSource.class, new DirectorySelector.Fäctory());

			eefs = new ElementEditorFactories();
			eefs.workflowFactories.add(new org.vanda.studio.modules.workflows.inspector.WorkflowEditor());
			eefs.literalFactories.add(new LiteralEditor(app, fr));

			ToolFactory pdftool = new WorkflowToPDFToolFactory(app.getToolMetaRepository().getRepository());
			Generator gen = new GeneratorImpl(app, profile);
			LinkedList<SemanticsToolFactory> srep = new LinkedList<SemanticsToolFactory>();
			srep = new LinkedList<SemanticsToolFactory>();
			srep.add(new InspectorTool(eefs));
			srep.add(new RunNowTool(gen));

			LinkedList<ToolFactory> toolFactories;
			toolFactories = new LinkedList<ToolFactory>();
			toolFactories.add(pdftool);
			toolFactories.add(new SemanticsTool(srep));
			app.registerPreviewFactory(RunTool.EXECUTION, new WorkflowExecutionPreview(app,
					new MainComponentToolFactory(app.getToolMetaRepository().getRepository(),
					/* immutable= */true), toolFactories));

			srep = new LinkedList<SemanticsToolFactory>(srep);
			srep.add(new RunTool(gen));

			toolFactories = new LinkedList<ToolFactory>();
			toolFactories.add(new ErrorHighlighterFactory());
			toolFactories.add(new PaletteTool(app.getToolMetaRepository().getRepository()));
			toolFactories.add(new SaveTool());
			toolFactories.add(pdftool);
			toolFactories.add(new SemanticsTool(srep));
			toolFactories.add(new AssignmentTableToolFactory(eefs, app.getRootDataSource()));
			toolFactories.add(new AssignmentSwitchToolFactory());
			app.registerPreviewFactory(WORKFLOW, new WorkflowPreview(app, new MainComponentToolFactory(app
					.getToolMetaRepository().getRepository(), false), toolFactories));

			// app.getWindowSystem().addAction(null, new OpenManagerAction(),
			// null, 100);
			app.getWindowSystem().addAction(null, new NewWorkflowAction(), "document-new",
					KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK), 0);
			app.getWindowSystem().addAction(null, new OpenWorkflowAction(), "document-open",
					KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK), 1);
		}

		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Workflow";
			}

			@Override
			public void invoke() {
				app.getPreviewFactory(WORKFLOW).openEditor("");
			}
		}

		protected class OpenWorkflowAction implements Action {
			@Override
			public String getName() {
				return "Open Workflow...";
			}

			@Override
			public void invoke() {
				// create a new file opening dialog
				JFileChooser chooser = new JFileChooser("");
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("Workflow XML (*.xwf)", "xwf"));
				String lastDir = app.getProperty("lastDir");
				if (lastDir != null)
					chooser.setCurrentDirectory(new File(lastDir));

				// center dialog over main window
				int result = chooser.showOpenDialog(app.getWindowSystem().getMainWindow());

				// once file choice is approved, load the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					app.setProperty("lastDir", chosenFile.getParentFile().getAbsolutePath());
					String filePath = chosenFile.getPath();
					app.getPreviewFactory(WORKFLOW).openEditor(filePath);
				}
			}
		}

		public final class OpenManagerAction implements Action, ProfileOpener {
			@Override
			public String getName() {
				return "Manage Fragment Profiles...";
			}

			@Override
			public void invoke() {
				if (manager == null) {
					manager = new ProfileManager(app, repository, this);
				}
				manager.focus();
			}

			@Override
			public void closeManager() {
				manager = null;
			}
		}

	}
}
