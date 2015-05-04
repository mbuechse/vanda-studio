package org.vanda.datasources;

import org.vanda.types.CompositeType;
import org.vanda.types.Type;

public class IntegerDataSource implements DataSource {

	private static final Type TYPE = new CompositeType("Integer");

	public IntegerDataSource() {
	}

	@Override
	public String getValue(String element) {
		return element;
	}

	@Override
	public Type getType(String element) {
		return TYPE;
	}

	@Override
	public String createElement() {
		return "0";
	}
}
