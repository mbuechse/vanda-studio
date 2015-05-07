package org.vanda.datasources;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.datasources.DataSourceEditor;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.Repository;

 
public class RootDataSource implements DataSource {

	private Repository<String, DataSourceMount> mtab;

	public RootDataSource(Repository<String, DataSourceMount> mtab) {
		super();
		this.mtab = mtab;
	}
	
	public Repository<String, DataSourceMount> getMountTable() {
		return mtab;
	}

	@Override
	public Type getType(String element) {
		String[] p = element.split(":");
		if (p.length < 2)
			return Types.undefined;
		DataSource ds = mtab.getItem(p[0]).ds;
		return ds != null ? ds.getType(p[1]) : Types.undefined;
	}

	@Override
	public String getValue(String element) {
		String[] p = element.split(":");
		DataSource ds = mtab.getItem(p[0]).ds;
		return ds != null ? ds.getValue(p[1]) : "";
	}

	public DataSourceEditor createEditor(Application app) {
		return null;
	}
	
	@Override
	public String createElement() {
		return ":"; // never needed, hopefully  XXX
	}

}
