package org.vanda.workflows.run;

import org.vanda.util.MultiplexObserver;
import org.vanda.workflows.run.RunStates.RunEvent;
import org.vanda.workflows.run.RunStates.RunEventId;
import org.vanda.workflows.run.RunStates.RunState;

public interface Runner extends RunState {
	public MultiplexObserver<RunEvent> getObservable();
	public MultiplexObserver<RunEventId> getObservableId();
}
