package org.vanda.util;

public final class CompositeFactory<D, E> implements Factory<D, E> {
	
	// private Map<Class<? extends D>, Factory<D, E>> registry = new HashMap<Class<? extends D>, Factory<D, E>>();
	private final Repository<Class<? extends D>, Factory<D, E>> repository;
	
	public CompositeFactory(Repository<Class<? extends D>, Factory<D, E>> repository) {
		this.repository = repository;
	}

	@Override
	public E instantiate(D d) {
		@SuppressWarnings("unchecked")
		Factory<D, E> factory = repository.getItem((Class<? extends D>) d.getClass());
		if (factory == null)
			throw new RuntimeException();
		return factory.instantiate(d);
	}
	
}
