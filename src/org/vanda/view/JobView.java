package org.vanda.view;

import org.vanda.run.RunStates.RunEvent;
import org.vanda.run.RunStates.RunStateReady;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.view.Views.*;
import org.vanda.workflows.hyper.Job;

public class JobView extends AbstractView<Job> implements Observer<RunEvent> {

	private final MultiplexObserver<RunEvent> runEventObserver = new MultiplexObserver<RunEvent>();
	// RunEvent doubles as state (user can call doNotify any time)
	private RunEvent runEvent = new RunStateReady();
	private int progress = 0;

	public JobView(ViewListener<AbstractView<?>> listener) {
		super(listener);
	}

	public int getRunProgress() {
		return progress;
	}

	public Observable<RunEvent> getRunEventObservable() {
		return runEventObserver;
	}

	@Override
	public SelectionObject createSelectionObject(Job t) {
		return new JobSelectionObject(t);
	}

	public RunEvent getRunState() {
		return runEvent;
	}

	@Override
	public void notify(RunEvent event) {
		runEvent = event;
		runEventObserver.notify(event);
	}
}
