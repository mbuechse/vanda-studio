package org.vanda.studio.modules.workflows;

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.DirectoryDataSource;
import org.vanda.datasources.DoubleDataSource;
import org.vanda.datasources.IntegerDataSource;
import org.vanda.datasources.RootDataSource;
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
import org.vanda.studio.modules.workflows.tools.RunTool;
import org.vanda.studio.modules.workflows.tools.SaveTool;
import org.vanda.studio.modules.workflows.tools.WorkflowToPDFToolFactory;
import org.vanda.studio.modules.workflows.tools.semantic.InspectorTool;
import org.vanda.studio.modules.workflows.tools.semantic.RunNowTool;
import org.vanda.studio.modules.workflows.tools.semantic.SemanticsTool;
import org.vanda.studio.modules.workflows.tools.semantic.SemanticsToolFactory;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.AbstractRepository;
import org.vanda.util.Action;
import org.vanda.util.CompositeFactory;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.PreviewFactory;
import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.run.BuildSystem;

public class WorkflowModule implements Module {

	public static final Type WORKFLOW = new CompositeType("Workflow");
	public static final Type EXECUTION = new CompositeType("Execution");

	private final RootDataSource rootDataSource;
	private final MetaRepository<Type, PreviewFactory> previewFactoryMeta;
	private final Repository<String, BuildSystem> buildSystemRepository;
	private final Repository<String, DataSourceMount> dataSourceRepository;
	private final Repository<String, Tool> toolRepository;
	private final MetaRepository<String, ToolFactory> toolFactoryMeta;

	public WorkflowModule(Repository<String, Tool> toolRepository,
			Repository<String, DataSourceMount> dataSourceRepository,
			Repository<String, BuildSystem> buildSystemRepository, RootDataSource rootDataSource,
			MetaRepository<Type, PreviewFactory> previewFactoryMeta, MetaRepository<String, ToolFactory> toolFactoryMeta) {
		this.toolRepository = toolRepository;
		this.rootDataSource = rootDataSource;
		this.dataSourceRepository = dataSourceRepository;
		this.buildSystemRepository = buildSystemRepository;
		this.previewFactoryMeta = previewFactoryMeta;
		this.toolFactoryMeta = toolFactoryMeta;
	}

	@Override
	public Object createInstance(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}

	private static final class StaticRepository extends AbstractRepository<Type, PreviewFactory> {
		public void put(Type key, PreviewFactory value) {
			items.put(key, value);
		};
	}

	protected final class WorkflowModuleInstance {

		private final Application app;
		private final ElementEditorFactories eefs;
		private final StaticRepository sr;

		public WorkflowModuleInstance(Application a) {
			app = a;
			sr = new StaticRepository();
			WorkflowPreview executionPreviewFactory = new WorkflowPreview(app, new MainComponentToolFactory(app,
					toolRepository, false), toolRepository, toolFactoryMeta.getRepository());

			CompositeFactory<DataSource, ElementSelector> fr = new CompositeFactory<DataSource, ElementSelector>();
			fr.put(DoubleDataSource.class, new DoubleSelector.Fäctory());
			fr.put(IntegerDataSource.class, new IntegerSelector.Fäctory());
			fr.put(DirectoryDataSource.class, new DirectorySelector.Fäctory());

			eefs = new ElementEditorFactories();
			eefs.workflowFactories.add(new org.vanda.studio.modules.workflows.inspector.WorkflowEditor());
			eefs.literalFactories.add(new LiteralEditor(app, rootDataSource, fr));

			ListRepository<SemanticsToolFactory> srep = new ListRepository<SemanticsToolFactory>();
			srep.addItem(new InspectorTool(eefs, previewFactoryMeta.getRepository()));
			srep.addItem(new RunNowTool());

			ListRepository<ToolFactory> lr = new ListRepository<ToolFactory>();
			lr.addItem(new ErrorHighlighterFactory());
			lr.addItem(new PaletteTool(toolRepository));
			lr.addItem(new SaveTool());
			lr.addItem(new WorkflowToPDFToolFactory(toolRepository));
			lr.addItem(new SemanticsTool(srep, buildSystemRepository, dataSourceRepository));
			lr.addItem(new AssignmentTableToolFactory(app, fr, rootDataSource));
			lr.addItem(new AssignmentSwitchToolFactory());
			lr.addItem(new RunTool(app, rootDataSource, executionPreviewFactory));
			toolFactoryMeta.addRepository(lr);
			sr.put(EXECUTION, executionPreviewFactory);
			sr.put(WORKFLOW, executionPreviewFactory);
			previewFactoryMeta.addRepository(sr);

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
				previewFactoryMeta.getRepository().getItem(WORKFLOW).openEditor("");
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
					previewFactoryMeta.getRepository().getItem(WORKFLOW).openEditor(filePath);
				}
			}
		}

	}
}
