package org.vanda.studio.modules.workflows.data;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;

public final class Element {

	private boolean dirty = false;
	private int update;
	private String value;
	private MultiplexObserver<Element> observable;
	
	public Element(String value) {
		observable = new MultiplexObserver<Element>();
		this.value = value;
	}
	
	public void beginUpdate() {
		update++;
	}
	
	public void endUpdate() {
		update--;
		if (update == 0 && dirty) {
			observable.notify(this);
			dirty = false;
		}
	}

	public Observable<Element> getObservable() {
		return observable;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (!this.value.equals(value)) {
			beginUpdate();
			try {
				this.value = value;
				dirty = true;
			} finally {
				endUpdate();
			}
		}
	}

}
