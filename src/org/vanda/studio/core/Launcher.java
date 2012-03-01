/**
 * 
 */
package org.vanda.studio.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;

public final class Launcher implements Runnable {

	private Launcher() {
		// utility class
	}
	
	@Override
	public void run() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex) {
		}
		
		Application app = new ApplicationImpl();
		Module[] ms = {
			new org.vanda.studio.modules.algorithms.AlgorithmsModule(),
			new org.vanda.studio.modules.dictionaries.DictionaryModule(),
			new org.vanda.studio.modules.wrtgs.WrtgModule(),
			new org.vanda.studio.modules.terms.TermModule()
		};

		ModuleManager moduleManager = new ModuleManager(app);
		moduleManager.loadModules();
		for (Module m : ms)
			moduleManager.loadModule(m);
		moduleManager.initModules();
		
		app.getGlobalRepository().refresh();
		System.out.println(app.getGlobalRepository().getItems());
	}

	/**
	 * @param args
	 *            Command line Arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Launcher());
	}

}