/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.VObject;
import org.vanda.studio.model.VObjectInstance;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleVObjectInstance;
import org.vanda.studio.modules.common.VObjectFactory;
import org.vanda.studio.util.Observer;

/**
 * some parts by hjholtz
 * @author buechse
 * 
 */
public class VDictionaryFactory implements VObjectFactory<VDictionary> {
	
	@Override
	public VDictionary createInstance(
		ModuleInstance<VDictionary> mod,
		File f)
	{
		return new VDictionaryImpl(mod, f);
	}
	
	protected static class VDictionaryImpl implements VDictionary {
		
		protected static final String[] inports = { };
		protected static final String[] outports = { "dictionary" };
		
		ModuleInstance<VDictionary> mod;
		File file;
		String author;
		String category;
		String date;
		String description;
		String id;
		String name;
		
		public VDictionaryImpl(ModuleInstance<VDictionary> mod, File file) {
			this.mod = mod;
			this.file = file;
			author = "unknown";
			category = "Dictionaries";
			date = "file.getDate..."; // FIXME
			description = "Dictionary";
			id = file.getAbsolutePath();
			name = file.getName();
			name = name.substring(0,name.length()-4);
			//System.out.println(file.getPath());
		}
		
		@Override
		public void appendActions(List<Action> as) {
			as.add(
				new Action() {
					@Override
					public String getName() {
						return "View";
					}
					
					@Override
					public void invoke() {
						// open Torsten's viewer
						mod.openEditor(VDictionaryImpl.this);
					}
				});
		}

		@Override
		public VObjectInstance createInstance() {
			return new SimpleVObjectInstance();
		}

		@Override
		public String getAuthor() {
			return author;
		}
		
		@Override
		public String getCategory() {
			return category;
		}
		
		@Override
		public String getDate() {
			return date;
		}
	
		@Override
		public String getDescription() {
			return description;
		}
	
		@Override
		public String getId() {
			return id;
		}
	
		@Override
		public String[] getInputPorts() {
			return inports;
		}
	
		@Override
		public String getName() {
			return name;
		}
	
		@Override
		public String[] getOutputPorts() {
			return outports;
		}
		
		@Override
		public Dictionary load() throws IOException {
			return new Dictionary(file.getAbsolutePath(), '\t');
		}

		@Override
		public void selectRenderer(RendererSelection rs) {
			rs.selectGrammarRenderer();
		}
		
	}

}