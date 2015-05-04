/**
 * 
 */
package org.vanda.util;


/**
 * @author buechse
 *
 */
public class ListRepository<T extends HasId> extends AbstractRepository<String, T> {
	public ListRepository() {
		super();
	}

	public void addItem(T newitem) {
		T item = items.remove(newitem.getId());
		if (item != newitem)
			removeObservable.notify(item);
		items.put(newitem.getId(), newitem);
		if (item != newitem)
			addObservable.notify(newitem);
	}

}
