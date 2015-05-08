/**
 * 
 */
package org.vanda.studio.core;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.RCChecker;

public final class Launcher implements Runnable {

	private Launcher() {
		// I'm a private launcher, a launcher for money ...
	}

	@Override
	public void run() {
		ApplicationImpl app = new ApplicationImpl();
		Module[] ms = {
				new org.vanda.studio.modules.messages.MessageModule(),
				new org.vanda.studio.modules.tools.ToolsModule(app.getToolMetaRepository()),
				new org.vanda.studio.modules.run.RunModule(app.getRunnerFactoryMetaRepository()),
				new org.vanda.studio.modules.previews.PreviewsModule(app.getPreviewFactoryMetaRepository()),
				new org.vanda.studio.modules.workflows.WorkflowModule(app.getToolMetaRepository().getRepository(), app
						.getRunnerFactoryMetaRepository().getRepository(), app.getRootDataSource(),
						app.getPreviewFactoryMetaRepository()),
				new org.vanda.studio.modules.datasources.DataSourceModule(app.getDataSourceMetaRepository()) };

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
		RCChecker.readRC();  // TODO oh boy
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
