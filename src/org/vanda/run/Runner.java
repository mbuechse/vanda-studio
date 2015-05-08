package org.vanda.run;

import org.vanda.run.RunStates.RunEvent;
import org.vanda.run.RunStates.RunEventId;
import org.vanda.run.RunStates.RunState;
import org.vanda.util.MultiplexObserver;

public interface Runner extends RunState {
	public MultiplexObserver<RunEvent> getObservable();
	public MultiplexObserver<RunEventId> getObservableId();
}
