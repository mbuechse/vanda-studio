/**
 * 
 */
package org.vanda.studio.core;

import java.util.ArrayList;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.Observer;

public class ModuleManager {

	protected Application application;

	protected ArrayList<Module> modules;
	
	protected ArrayList<Object> instances;
	
	private Observer<Application> shutdownObserver;

	public ModuleManager(Application application) {
		this.application = application;
		modules = new ArrayList<Module>();
		instances = new ArrayList<Object>();
		shutdownObserver = new Observer<Application>() {
			@Override
			public void notify(Application a) {
				finalizeModules();
			}
		};
		application.getShutdownObservable().addObserver(shutdownObserver);
	}
	
	public void loadModule(Module m) {
		modules.add(m);
	}

	public void initModules() {
		instances.clear();
		instances.ensureCapacity(modules.size());
		
		for (Module m : modules)
			instances.add(m.createInstance(application));
	}

	public void finalizeModules() {
		instances.clear();
	}
}
