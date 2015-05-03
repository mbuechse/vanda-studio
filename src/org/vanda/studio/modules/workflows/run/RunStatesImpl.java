package org.vanda.studio.modules.workflows.run;

import java.util.Date;

import org.vanda.run.RunStates.*;

public class RunStatesImpl {
	
	public static interface RunTransitions {
		void doCancel();

		void doFinish();

		void doRun();
	}

	public static class StateInit implements RunState {
		
		private final RunTransitions rt;
		
		public StateInit(RunTransitions rt) {
			this.rt = rt;
		}

		@Override
		public void run() {
			rt.doRun();
		}
		
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.ready();
		}

		@Override
		public String getString(Date date) {
			return "[Initial] " + date.toString();
		}

		@Override
		public void cancel() {
			
		}

		@Override
		public void finish() {
			
		}

		@Override
		public void process() {
			
		}
	}

	public static class StateCancelled implements RunState {
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.cancelled();
		}

		@Override
		public String getString(Date date) {
			return "[Cancelled] " + date.toString();
		}

		@Override
		public void cancel() {
			
		}

		@Override
		public void finish() {
			
		}

		@Override
		public void process() {
			
		}

		@Override
		public void run() {
			
		}
	}

	public static class StateDone implements RunState {
		@Override
		public void doNotify(RunEventListener rsv) {
			rsv.done();
		}

		@Override
		public String getString(Date date) {
			return "[Done] " + date.toString();
		}

		@Override
		public void cancel() {
			
		}

		@Override
		public void finish() {
			
		}

		@Override
		public void process() {
			
		}

		@Override
		public void run() {
			
		}
	}

	
	


}
