package org.vanda.datasources.serialization;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class Storer {
	
	private final List<DataSourceType<?>> types;
	
	public Storer(List<DataSourceType<?>> types) {
		this.types = types;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void store(Collection<DataSourceMount> w, String filename) throws Exception {
		Writer writer = new FileWriter(new File(filename));
		final PrettyPrintWriter ppw = new PrettyPrintWriter(writer);
		ppw.startNode("root");
		for (DataSourceMount j : w) {
			ppw.startNode("mount");
			ppw.addAttribute("path", j.id);
			DataSource ds = j.ds;
			for (DataSourceType<? extends DataSource> dst : types) {
				// XXX use hash map
				if (ds.getClass().equals(dst.getDataSourceClass())) {
					((DataSourceType) dst).store(ppw, ds);
					break;
				}
			}
			ppw.endNode(); // mount
		}
		ppw.endNode(); // root
	}

}
