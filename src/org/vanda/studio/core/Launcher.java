/**
 * 
 */
package org.vanda.studio.core;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.types.Type;
import org.vanda.util.CompositeRepository;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.MetaRepository;
import org.vanda.util.PreviewFactory;
import org.vanda.util.RCChecker;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.run.BuildSystem;

// dependency injection class (application builder, so to speak)
public final class Launcher implements Runnable {

	private final MetaRepository<Type, PreviewFactory> previewFactoryMeta;
	private final MetaRepository<String, DataSourceMount> dataSourceMeta;
	private final MetaRepository<String, BuildSystem> buildSystemMeta;
	private final MetaRepository<String, Tool> toolMeta;
	private final MetaRepository<String, ToolFactory> toolFactoryMeta;
	
	private final RootDataSource rootDataSource;
	private final ApplicationImpl app = new ApplicationImpl();

	private Launcher() {
		// I'm a private launcher, a launcher for money ...
		buildSystemMeta = new CompositeRepository<String, BuildSystem>();
		previewFactoryMeta = new CompositeRepository<Type, PreviewFactory>();
		dataSourceMeta = new CompositeRepository<String, DataSourceMount>();
		toolMeta = new CompositeRepository<String, Tool>();
		toolFactoryMeta = new CompositeRepository<String, ToolFactory>();
		rootDataSource = new RootDataSource(dataSourceMeta.getRepository());
	}

	@Override
	public void run() {
		Module[] ms = {
				// new org.vanda.studio.modules.messages.MessageModule(),
				new org.vanda.studio.modules.tools.ToolsModule(toolMeta),
				new org.vanda.studio.modules.run.RunModule(buildSystemMeta),
				new org.vanda.studio.modules.previews.PreviewsModule(previewFactoryMeta),
				new org.vanda.studio.modules.workflows.WorkflowModule(toolMeta.getRepository(),
						buildSystemMeta.getRepository(), rootDataSource, previewFactoryMeta,
						toolFactoryMeta),
				new org.vanda.studio.modules.datasources.DataSourceModule(dataSourceMeta) };

		ModuleManager moduleManager = new ModuleManager(app);
		for (Module m : ms)
			moduleManager.loadModule(m);
		moduleManager.initModules();
		app.setModuleManager(moduleManager);

		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(app));
		// throw new NullPointerException("brain is null");
	}

	/**
	 * @param args
	 *            Command line Arguments
	 */
	public static void main(String[] args) {
		RCChecker.readRC(); // TODO oh boy
		SwingUtilities.invokeLater(new Launcher());
	}

	public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
		private final Application app;

		public ExceptionHandler(Application app) {
			this.app = app;
		}

		public void uncaughtException(final Thread t, final Throwable e) {
			if (SwingUtilities.isEventDispatchThread()) {
				app.sendMessage(new ExceptionMessage(e));
			} else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							app.sendMessage(new ExceptionMessage(e));
						}
					});
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				} catch (InvocationTargetException ite) {
					// not much more we can do here except log the exception
					ite.getCause().printStackTrace();
				}
			}
		}

	}
}
