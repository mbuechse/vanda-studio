package org.vanda.datasources;

import org.vanda.types.Type;

public class DirectoryDataSource implements DataSource {

	public String path;
	public String filter;
	public Type type;

	public DirectoryDataSource(Type type, String path, String filter) {
		this.type = type;
		this.filter = filter;
		this.path = path;
	}

	@Override
	public String getValue(String element) {
		return path + "/" + element;
	}

	@Override
	public Type getType(String element) {
		return type;
	}

	@Override
	public String createElement() {
		return "";
	}

}
