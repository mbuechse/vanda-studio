/**
 * 
 */
package org.vanda.studio.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.vanda.studio.app.Module;

/**
 * @author buechse, rmueller
 * 
 */
public class ModuleLoader {
	//private static final Logger log = Logger.getLogger(ModuleLoader.class);

	// helper class
	private ModuleLoader() {}

	public static void loadJars(String directory, List<Module> modules) {
		File dir = new File(directory);
		File[] files;

		if (dir.isFile()) {
			files = new File[1];
			files[0] = dir;
		}
		else {
			files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File _, String name) {
					return (name.endsWith(".jar"));
				}
			});
		}
		
		if (files == null) {
			// log...
		}
		else {
			// construct class path of our class loader
			ArrayList<URL> urls = new ArrayList<URL>();
			for (File f : files) {
				try {
					urls.add(f.toURI().toURL());
				}
				catch (MalformedURLException e) {
				}
			}
			URLClassLoader clazzLoader = new URLClassLoader(urls.toArray(new URL[0]));
			try {
				for (File f : files) {
					List<String> classNames = getClassNames(f.getAbsolutePath());
					for (String cn : classNames) {
						try {
							Class<?> clazz = clazzLoader.loadClass(cn);
							if (clazz != null && Module.class.isAssignableFrom(clazz)) {
								modules.add((Module)clazz.newInstance());
							}
						}
						catch (Exception e) {
							//log.error("Error on instantiating module connector '"
							//		+ clazz.getName() + "'; skipping module...", e);
						}
					}
				}
			} finally {
				try {
					clazzLoader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	protected static List<String> getClassNames(String jarName) {
		ArrayList<String> classes = new ArrayList<String>();
		
		try {
			FileInputStream fis = new FileInputStream(jarName);
			try {
				JarInputStream jarFile = new JarInputStream(fis);
				try {
					JarEntry jarEntry = jarFile.getNextJarEntry();
					while (jarEntry != null) {
						if (jarEntry.getName().endsWith(".class")) {
							String cn = jarEntry.getName().replaceAll("/", "\\.");
							// remove ".class"
							classes.add(cn.substring(0, cn.length() - 6));
						}
						jarEntry = jarFile.getNextJarEntry();
					}
				} catch (Exception e) {
					//log.error("error during processing of jar '" + jarName
					//		+ "'; skipping...", e);
				} finally {
					jarFile.close();
				}
			} catch (IOException e) {
				// ignore
				e.printStackTrace();
			} finally {
				fis.close();
			}
		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {
			// ignore
		}
		return classes;
	}
}
