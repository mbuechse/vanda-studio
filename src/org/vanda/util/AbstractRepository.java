package org.vanda.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AbstractRepository<S, T> implements Repository<S, T> {

	protected final MultiplexObserver<T> addObservable;
	protected final MultiplexObserver<T> removeObservable;
	protected final MultiplexObserver<T> modifyObservable;
	protected Map<S, T> items;

	public AbstractRepository() {
		addObservable = new MultiplexObserver<T>();
		modifyObservable = new MultiplexObserver<T>();
		removeObservable = new MultiplexObserver<T>();
		items = new HashMap<S, T>();
	}

	@Override
	public boolean containsItem(S id) {
		return items.containsKey(id);
	}

	@Override
	public Observable<T> getAddObservable() {
		return addObservable;
	}

	@Override
	public Observable<T> getRemoveObservable() {
		return removeObservable;
	}

	@Override
	public Observable<T> getModifyObservable() {
		return modifyObservable;
	}

	@Override
	public T getItem(S id) {
		return items.get(id);
	}

	@Override
	public Collection<S> getKeys() {
		return items.keySet();
	}

	@Override
	public Collection<T> getItems() {
		return items.values();
	}

	public Observer<T> getModifyObserver() {
		return modifyObservable;
	}

	@Override
	public void refresh() {
		// if not overriden, do nothing
	}

}