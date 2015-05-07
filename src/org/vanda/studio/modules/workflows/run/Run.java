package org.vanda.studio.modules.workflows.run;

import java.util.Date;

import org.vanda.run.RunStates.*;
import org.vanda.studio.modules.workflows.run.RunStatesImpl.*;
import org.vanda.util.MultiplexObserver;

public class Run implements RunState {
	private final Date date;
	private final String id;
	private final MultiplexObserver<RunEvent> observable1 = new MultiplexObserver<RunEvent>();
	private final MultiplexObserver<RunEventId> observable;
	private RunState state;

	public Run(String id) {
		date = new Date();
		this.id = id;
		observable = new MultiplexObserver<RunEventId>();
		state = new StateInit(rt);
		state.process();
	}
	
	public String toString() {
		return state.getString(date);
	}
	
	public String getId() {
		return id;
	}
	
	public MultiplexObserver<RunEvent> getObservable() {
		return observable1;
	}
	
	public MultiplexObserver<RunEventId> getObservableId() {
		return observable;
	}

	public void doNotify(RunEventListener rsv) {
		state.doNotify(rsv);
	}

	@Override
	public void cancel() {
		state.cancel();
	}

	@Override
	public void finish() {
		state.finish();
	}

	@Override
	public void process() {
		// none of the end user's business
	}

	@Override
	public void run() {
		state.run();
	}

	@Override
	public String getString(Date date) {
		return state.getString(date);
	}
	
	private RunTransitions rt = new RunTransitions() {
		@Override
		public void doCancel() {
			state = new StateCancelled();
			state.process();
			observable1.notify(state);
		}
	
		@Override
		public void doFinish() {
			state = new StateDone();
			state.process();
			observable1.notify(state);
		}
	
		@Override
		public void doRun() {
			state = new StateRunning(observable, id, this);
			state.process();
			observable1.notify(state);
		}
	};
}
