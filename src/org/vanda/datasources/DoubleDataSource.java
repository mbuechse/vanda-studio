package org.vanda.datasources;

import org.vanda.types.CompositeType;
import org.vanda.types.Type;

public class DoubleDataSource implements DataSource {

	private static final Type TYPE = new CompositeType("Double");

	public DoubleDataSource() {
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
		return "0.0";
	}
}
