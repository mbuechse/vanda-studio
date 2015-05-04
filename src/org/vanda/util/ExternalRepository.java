/**
 * 
 */
package org.vanda.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * @author buechse
 *
 */
public class ExternalRepository<T extends RepositoryItem>
		extends AbstractRepository<String, T> {

	public interface Loader<T> {
		/** scans for items and notifies caller per item */
		void load(Observer<T> o);
	}

	private final Loader<T> loader;

	public ExternalRepository(Loader<T> loader) {
		super();
		this.loader = loader;
	}

	@Override
	public void refresh() {
		if (loader != null) {
			RefreshHelper<T> r = new RefreshHelper<T>(items);
			loader.load(r);
			items = r.getNewItems();
			Util.notifyAll(addObservable, r.getAdds());
			Util.notifyAll(removeObservable, r.getRemoves());
		}
	}

	protected static class RefreshHelper<T extends RepositoryItem>
			implements Observer<T> {
		protected LinkedList<T> adds;
		protected LinkedList<T> removes;
		protected Map<String, T> items;
		protected Map<String, T> newitems;

		public RefreshHelper(Map<String, T> items) {
			adds = new LinkedList<T>();
			removes = new LinkedList<T>();
			this.items = items;
			newitems = new HashMap<String, T>();
		}

		@Override
		public void notify(T newitem) {
			System.err.println(newitem.getId());
			T item = items.remove(newitem.getId());
			if (item != null) {
				if (item.getVersion().equals(newitem.getVersion()))
					newitem = item;
				//else
				//	removes.add(item);
			}
			newitems.put(newitem.getId(), newitem);
			if (item != newitem)
				adds.add(newitem);
		}

		public Collection<T> getAdds() {
			return adds;
		}

		public Map<String, T> getNewItems() {
			return newitems;
		}

		public Collection<T> getRemoves() {
			return removes;
		}
	}
}
