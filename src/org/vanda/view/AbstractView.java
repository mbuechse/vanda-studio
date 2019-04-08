package org.vanda.view;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.view.Views.*;

/**
 * stores selection, highlighting and run information
 * 
 * @author kgebhardt
 * 
 */
public abstract class AbstractView<T> {

	protected boolean marked;

	protected final MultiplexObserver<ViewEvent<AbstractView<?>>> observable = new MultiplexObserver<ViewEvent<AbstractView<?>>>();
	
	protected final ViewListener<AbstractView<?>> listener;

	protected boolean selected;
	
	protected AbstractView(ViewListener<AbstractView<?>> listener) {
		this.listener = listener;
	}
	
	public abstract SelectionObject createSelectionObject(T t);

	public Observable<ViewEvent<AbstractView<?>>> getObservable() {
		return observable;
	}

	public boolean isMarked() {
		return marked;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setMarked(boolean marked) {
		if (this.marked != marked) {
			this.marked = marked;
			observable.notify(new MarkChangedEvent<AbstractView<?>>(this));
			listener.markChanged(this);
		}
	}

	public void setSelected(boolean selected) {
		if (this.selected != selected) {
			this.selected = selected;
			observable.notify(new SelectionChangedEvent<AbstractView<?>>(this));
			listener.selectionChanged(this);
		}
	}
}