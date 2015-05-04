package org.vanda.util;

import java.util.HashMap;
import java.util.Map;

// this class is a quick hack
// TODO improve
public final class CompositeFactory<D, E> implements Factory<D, E> {
	
	private Map<Class<? extends D>, Factory<D, E>> registry = new HashMap<Class<? extends D>, Factory<D, E>>();
	
	public void put(Class<? extends D> clazz, Factory<D, E> f) {
		registry.put(clazz, f);
	}

	@Override
	public E instantiate(D d) {
		Factory<D, E> factory = registry.get(d.getClass());
		if (factory == null)
			throw new RuntimeException();
		return factory.instantiate(d);
	}
	
	
	
}
