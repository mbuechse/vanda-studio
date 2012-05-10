package org.vanda.studio.model.hyper;

import java.util.ListIterator;

import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

public final class MutableWorkflow<F> extends DrecksWorkflow<F> implements
		HyperWorkflow<F> {

	private final MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>> addObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>> modifyObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>> removeObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Connection<F>>> connectObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Connection<F>>> disconnectObservable;

	public MutableWorkflow(Class<F> fragmentType) {
		super(fragmentType);
		addObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>>();
		modifyObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>>();
		removeObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>>();
		connectObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Connection<F>>>();
		disconnectObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Connection<F>>>();
	}

	public MutableWorkflow(MutableWorkflow<F> hyperWorkflow)
			throws CloneNotSupportedException {
		super(hyperWorkflow);
		for (Job<F> c : children.keySet())
			c.parent = this;
		addObservable = hyperWorkflow.addObservable.clone();
		modifyObservable = hyperWorkflow.modifyObservable.clone();
		removeObservable = hyperWorkflow.removeObservable.clone();
		connectObservable = hyperWorkflow.connectObservable.clone();
		disconnectObservable = hyperWorkflow.disconnectObservable.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#clone()
	 */
	@Override
	public MutableWorkflow<F> clone() throws CloneNotSupportedException {
		return new MutableWorkflow<F>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#addChild(org.vanda.studio.
	 * model.hyper.HyperJob)
	 */
	@Override
	public void addChild(Job<F> hj) {
		assert (hj.getFragmentType() == null || hj.getFragmentType() == getFragmentType());
		assert (hj.parent == null);
		if (!children.containsKey(hj)) {
			children.put(hj, new DJobInfo(hj));
			hj.parent = this;
			addObservable.notify(new Pair<MutableWorkflow<F>, Job<F>>(
					this, hj));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#addConnection(org.vanda.studio
	 * .model.hyper.Connection)
	 */
	@Override
	public void addConnection(Connection<F> cc) {
		assert (children.containsKey(cc.getSource()) && children.containsKey(cc
				.getTarget()));
		DJobInfo sji = children.get(cc.getSource());
		DJobInfo tji = children.get(cc.getTarget());
		if (tji.inputs.get(cc.getTargetPort()) != null)
			throw new RuntimeException("!!!"); // FIXME better exception
		Integer tok = sji.outputs.get(cc.getSourcePort());
		tji.inputs.set(cc.getTargetPort(), tok);
		tji.inputsBlocked++;
		connections.get(tok).snd.add(new TokenValue<F>(cc.getTarget(), cc
				.getTargetPort()));
		sji.outCount++;
		connectObservable
				.notify(new Pair<MutableWorkflow<F>, Connection<F>>(this,
						cc));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getAddObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Job<F>>> getAddObservable() {
		return addObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getConnectObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Connection<F>>> getConnectObservable() {
		return connectObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getDisconnectObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Connection<F>>> getDisconnectObservable() {
		return disconnectObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getModifyObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Job<F>>> getModifyObservable() {
		return modifyObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getRemoveObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Job<F>>> getRemoveObservable() {
		return removeObservable;
	}

	public static <F> void removeChildGeneric(Job<F> hj) {
		if (hj.parent != null)
			hj.parent.removeChild(hj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#removeChild(org.vanda.studio
	 * .model.hyper.HyperJob)
	 */
	@Override
	public void removeChild(Job<F> hj) {
		assert (hj.parent == this);
		DJobInfo ji = children.get(hj);
		if (ji != null) {
			hj.parent = null;
			for (int i = 0; i < ji.inputs.size(); i++) {
				Integer tok = ji.inputs.get(i);
				if (tok != null) {
					TokenValue<F> stv = connections.get(tok).fst;
					removeConnection(stv.hj, stv.port, hj, i, null);
				}
			}
			for (int i = 0; i < ji.outputs.size(); i++) {
				removeConnection(hj, i, null, 0, null);
				token.recycleToken(i);
			}
			address.recycleToken(ji.address);
			children.remove(hj);
			removeObservable.notify(new Pair<MutableWorkflow<F>, Job<F>>(
					this, hj));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#removeConnection(org.vanda
	 * .studio.model.hyper.Connection)
	 */
	@Override
	public void removeConnection(Connection<F> cc) {
		if (children.containsKey(cc.getSource())
				&& children.containsKey(cc.getTarget()))
			removeConnection(cc.getSource(), cc.getSourcePort(),
					cc.getTarget(), cc.getTargetPort(), cc);
	}

	@Override
	public Job<?> dereference(ListIterator<Integer> address) {
		assert (address != null && address.hasNext());
		Job<?> hj = deref.get(address.next());
		if (hj != null)
			hj = hj.dereference(address);
		return hj;
	}

	@Override
	public Integer getAddress(Job<F> child) {
		DJobInfo ji = children.get(child);
		if (ji != null)
			return ji.address;
		else
			return null;
	}

	/**
	 * If cc != null, use cc for notification.
	 * If target == null, remove all outgoing connections.
	 * Alas, this was necessary to prevent concurrent modification exceptions.
	 * 
	 * @param source
	 * @param sourcePort
	 * @param target
	 * @param targetPort
	 * @param cc
	 */
	public void removeConnection(Job<F> source, int sourcePort,
			Job<F> target, int targetPort, Connection<F> cc) {
		DJobInfo sji = children.get(source);
		ListIterator<TokenValue<F>> li = connections.get(sji.outputs
				.get(sourcePort)).snd.listIterator();
		while (li.hasNext()) {
			TokenValue<F> tv = li.next();
			if (target == null || tv.hj == target && tv.port == targetPort) {
				DJobInfo tji = children.get(tv.hj);
				assert (sji.outputs.get(sourcePort) == tji.inputs.get(tv.port));
				tji.inputs.set(tv.port, null);
				tji.inputsBlocked--;
				sji.outCount--;
				li.remove();
				if (cc != null)
					disconnectObservable
							.notify(new Pair<MutableWorkflow<F>, Connection<F>>(
									this, cc));
				else
					disconnectObservable
							.notify(new Pair<MutableWorkflow<F>, Connection<F>>(
									this, new Connection<F>(source,
											sourcePort, target, targetPort)));
			}
		}
	}

	/*
	 * public void setDimensions(HyperJob<V> hj, double[] d) { assert
	 * (children.contains(hj));
	 * 
	 * if (d[0] != hj.dimensions[0] || d[1] != hj.dimensions[1] || d[2] !=
	 * hj.dimensions[2] || d[3] != hj.dimensions[3]) { hj.setDimensions(d);
	 * modifyObservable.notify(new Pair<HyperWorkflow<F, V>, HyperJob<V>>( this,
	 * hj)); } }
	 */

}
