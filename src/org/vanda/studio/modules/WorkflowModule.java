package org.vanda.studio.modules;

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.studio.modules.workflows.WorkflowPreview;
import org.vanda.studio.modules.workflows.tools.MainComponentToolFactory;
import org.vanda.studio.modules.workflows.tools.PaletteTool;
import org.vanda.studio.modules.workflows.tools.SaveTool;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.util.Action;
import org.vanda.util.Factory;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.Repository;
import org.vanda.util.StaticRepository;
import org.vanda.workflows.elements.Tool;

public class WorkflowModule implements Module {

	public static final Type WORKFLOW = new CompositeType("Workflow");
	public static final Type EXECUTION = new CompositeType("Execution");

	private final MetaRepository<Type, Factory<String, Object>> editorFactoryMeta;
	private final Repository<String, Tool> toolRepository;
	private final MetaRepository<String, ToolFactory> toolFactoryMeta;
	private final Repository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> contextFactoryRepository;

	public WorkflowModule(Repository<String, Tool> toolRepository, RootDataSource rootDataSource,
			Repository<Class<? extends Object>, Factory<WorkflowEditor, ? extends Object>> contextFactoryRepository,
			MetaRepository<Type, Factory<String, JComponent>> previewFactoryMeta,
			MetaRepository<Type, Factory<String, Object>> editorFactoryMeta,
			MetaRepository<String, ToolFactory> toolFactoryMeta) {
		this.toolRepository = toolRepository;
		this.contextFactoryRepository = contextFactoryRepository;
		this.editorFactoryMeta = editorFactoryMeta;
		this.toolFactoryMeta = toolFactoryMeta;
	}

	@Override
	public Object instantiate(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getId() {
		return "Workflows"; // Module for Vanda Studio";
	}

	protected final class WorkflowModuleInstance {

		private final Application app;
		private final StaticRepository<Type, Factory<String, Object>> sr;

		public WorkflowModuleInstance(Application a) {
			app = a;
			sr = new StaticRepository<Type, Factory<String, Object>>();
			WorkflowPreview executionPreviewFactory = new WorkflowPreview(app, contextFactoryRepository,
					new MainComponentToolFactory(app, toolRepository, false), toolRepository,
					toolFactoryMeta.getRepository());

			ListRepository<ToolFactory> lr = new ListRepository<ToolFactory>();
			lr.addItem(new PaletteTool(toolRepository));
			lr.addItem(new SaveTool());
			toolFactoryMeta.addRepository(lr);
			sr.put(EXECUTION, executionPreviewFactory);
			sr.put(WORKFLOW, executionPreviewFactory);
			editorFactoryMeta.addRepository(sr);

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
				editorFactoryMeta.getRepository().getItem(WORKFLOW).instantiate("");
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
					editorFactoryMeta.getRepository().getItem(WORKFLOW).instantiate(filePath);
				}
			}
		}

	}
}
