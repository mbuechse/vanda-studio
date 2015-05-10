package org.vanda.util;


public final class StaticRepository<S, T> extends AbstractRepository<S, T> {
	public void put(S key, T value) {
		items.put(key, value);
	};
}