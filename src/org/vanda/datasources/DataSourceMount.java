package org.vanda.datasources;

import org.vanda.util.HasId;

public class DataSourceMount implements HasId {
	
	public final String id;
	public final DataSource ds;
	
	public DataSourceMount(String id, DataSource ds) {
		this.id = id;
		this.ds = ds;
	}
	
	@Override
	public String getId() {
		return id;
	}
}
