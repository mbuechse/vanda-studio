/**
 * 
 */
package org.vanda.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author buechse
 * 
 */
public final class CompositeRepository<S, T> implements Repository<S, T>, MetaRepository<S, T> {

	protected HashSet<Repository<S, ? extends T>> repositories;

	protected MultiplexObserver<Repository<S, ? extends T>> addRepositoryObservable;
	private Observer<Repository<S, ? extends T>> addRepositoryMultiplexObserver;
	protected MultiplexObserver<Repository<S, ? extends T>> removeRepositoryObservable;
	private Observer<Repository<S, ? extends T>> removeRepositoryMultiplexObserver;

	protected MultiplexObserver<T> addObservable;
	protected MultiplexObserver<T> modifyObservable;
	protected MultiplexObserver<T> removeObservable;

	public CompositeRepository() {
		repositories = new HashSet<Repository<S, ? extends T>>();
		addRepositoryObservable = new MultiplexObserver<Repository<S, ? extends T>>();
		removeRepositoryObservable = new MultiplexObserver<Repository<S, ? extends T>>();
		addObservable = new MultiplexObserver<T>();
		modifyObservable = new MultiplexObserver<T>();
		removeObservable = new MultiplexObserver<T>();

		addRepositoryMultiplexObserver = new Observer<Repository<S, ? extends T>>() {

			@Override
			public void notify(Repository<S, ? extends T> r) {

				// "forward" child events
				r.getAddObservable().addObserver(addObservable);
				r.getRemoveObservable().addObserver(removeObservable);
				r.getModifyObservable().addObserver(modifyObservable);

				// pretend all items of r have been added
				Util.notifyAll(addObservable, r.getItems());

			}

		};
		addRepositoryObservable.addObserver(addRepositoryMultiplexObserver);

		removeRepositoryMultiplexObserver = new Observer<Repository<S, ? extends T>>() {

			@Override
			public void notify(Repository<S, ? extends T> r) {

				// stop forwarding
				r.getAddObservable().removeObserver(addObservable);
				r.getRemoveObservable().removeObserver(removeObservable);
				r.getModifyObservable().removeObserver(modifyObservable);

				// pretend all items of r have been removed
				Util.notifyAll(removeObservable, r.getItems());

			}

		};
		removeRepositoryObservable.addObserver(removeRepositoryMultiplexObserver);
	}

	@Override
	public Observable<Repository<S, ? extends T>> getRepositoryAddObservable() {
		return addRepositoryObservable;
	}

	@Override
	public Observable<Repository<S, ? extends T>> getRepositoryRemoveObservable() {
		return removeRepositoryObservable;
	}

	@Override
	public <T1 extends T> void addRepository(Repository<S, T1> r) {
		// fail-fast behavior
		if (r == null)
			throw new IllegalArgumentException("repository must not be null");
		if (!repositories.add(r))
			throw new UnsupportedOperationException("cannot add repository twice");

		addRepositoryObservable.notify(r);
	}

	@Override
	public T getItem(S id) {
		for (Repository<S, ? extends T> r : repositories) {
			T item = r.getItem(id);
			if (item != null)
				return item;
		}
		return null;
	}

	@Override
	public boolean containsItem(S id) {
		for (Repository<S, ? extends T> r : repositories) {
			if (r.containsItem(id))
				return true;
		}
		return false;
	}

	public Collection<S> getKeys() {
		Set<S> result = new HashSet<S>();
		for (Repository<S, ? extends T> r : repositories)
			result.addAll(r.getKeys());
		return result;
	}

	public Collection<T> getItems() {
		ArrayList<T> result = new ArrayList<T>();
		for (Repository<S, ? extends T> r : repositories)
			result.addAll(r.getItems());
		return result;
	}

	public void refresh() {
		for (Repository<S, ? extends T> r : repositories)
			r.refresh();
	}

	@Override
	public <T1 extends T> void removeRepository(Repository<S, T1> r) {
		// fail-fast behavior
		if (r == null)
			throw new IllegalArgumentException("repository must not be null");
		if (!repositories.remove(r))
			throw new UnsupportedOperationException("cannot add repository twice");

		removeRepositoryObservable.notify(r);

	}

	public Observable<T> getAddObservable() {
		return addObservable;
	}

	public Observable<T> getRemoveObservable() {
		return removeObservable;
	}

	public Observable<T> getModifyObservable() {
		return modifyObservable;
	}

	@Override
	public Repository<S, T> getRepository() {
		return this;
	}

}
