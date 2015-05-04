/**
 * 
 */
package org.vanda.util;

import java.util.Collection;


/**
 * @author buechse
 * 
 */
public interface Repository<S, T> {
	Observable<T> getAddObservable();

	Observable<T> getRemoveObservable();
	
	Observable<T> getModifyObservable();

	T getItem(S id);

	boolean containsItem(S id);
	
	Collection<S> getKeys();

	Collection<T> getItems();
	
	void refresh();
}
