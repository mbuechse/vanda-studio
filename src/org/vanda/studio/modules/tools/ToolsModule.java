package org.vanda.studio.modules.tools;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.ExternalRepository;
import org.vanda.util.MetaRepository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.toolinterfaces.ToolLoader;

public class ToolsModule implements Module {

	@Override
	public String getName() {
		return "Tool interfaces module for Vanda Studio";
	}
	
	private final MetaRepository<String, Tool> toolMeta;
	
	public ToolsModule(MetaRepository<String, Tool> toolMeta) {
		this.toolMeta = toolMeta;
	}

	@Override
	public Object createInstance(Application a) {
		ExternalRepository<Tool> er = new ExternalRepository<Tool>(
				new ToolLoader(a.getProperty("toolsPath")));
		er.refresh();
		toolMeta.addRepository(er);
		return er;
	}

}
