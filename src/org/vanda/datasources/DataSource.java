package org.vanda.datasources;

import org.vanda.types.Type;

public interface DataSource {

	String getValue(String element);
	Type getType(String element);
	String createElement();

}
