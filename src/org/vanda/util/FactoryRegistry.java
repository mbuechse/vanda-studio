package org.vanda.util;

import java.util.HashMap;
import java.util.Map;

// this class is a quick hack
// TODO improve
public class FactoryRegistry<D, E> {
	
	public interface Factory<D, E> {
		E instantiate(D d);
	}

	public Map<Class<? extends D>, Factory<D, E>> registry = new HashMap<Class<? extends D>, Factory<D, E>>();
	
}
