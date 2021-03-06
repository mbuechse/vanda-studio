package org.vanda.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import javax.swing.SwingWorker;

import org.vanda.runner.RunStatesImpl.RunTransitions;
import org.vanda.util.Observer;
import org.vanda.util.Util;
import org.vanda.workflows.run.RunStates.*;

public class StateRunning implements RunState {

	private final Observer<RunEventId> obs;
	private final String id;
	private final RunTransitions rt;

	private Worker w;

	public StateRunning(Observer<RunEventId> obs, String id, RunTransitions rt) {
		this.obs = obs;
		this.id = id;
		this.rt = rt;
	}

	@Override
	public void cancel() {
		if (!w.isDone())
			w.cancel(true);
	}

	@Override
	public void process() {
		w = new Worker();
		w.execute();
	}
	
	@Override
	public void doNotify(RunEventListener rsv) {
		rsv.running();
	}
	
	@Override
	public String getString(Date date) {
		return "[Running] " + date.toString();
	}

	private class Worker extends SwingWorker<String, RunEventId> implements Observer<RunEventId> {

		private Process process;
		private StreamGobbler esg;
		private int retval = -1;
		private final LineParser[] parsers = { new ProgressParser(), new RunningParser(), new DoneParser(),
				new CancelledParser() };

		@Override
		protected String doInBackground() {
			try {
				// TODO use path from runconfig
				process = Runtime.getRuntime().exec(id, null, null);
			} catch (Exception e) {
				// TODO maybe trigger cancel event or something, but get rid of this coupling
				// app.sendMessage(new ExceptionMessage(e));
			}

			// ignore stderr, parse stdout
			InputStream stdin = process.getInputStream();
			InputStream stderr = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stdin);
			BufferedReader br = new BufferedReader(isr);
			LineParser lp = new OrParser(parsers);
			esg = new StreamGobbler(stderr);
			esg.start();
			try {
				String line;
				while ((line = br.readLine()) != null)
					lp.parseLine(line, this);
				retval = process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void done() {
			if (process != null) {
				process.destroy();
				process = null;
			}
			if (retval != 0) {
				obs.notify(new RunEventId(new RunStateCancelled(), id));
				rt.doCancel();
			} else
				obs.notify(new RunEventId(new RunStateDone(), id));
			rt.doFinish();
		}

		@Override
		public void notify(RunEventId event) {
			publish(event);
		}

		@Override
		protected void process(List<RunEventId> res) {
			Util.notifyAll(obs, res);
		}

	}

	private interface LineParser {
		public void parseLine(String line, Observer<RunEventId> mo);
	}

	private static class CancelledParser implements LineParser {
		@Override
		public void parseLine(String line, Observer<RunEventId> mo) {
			if (line.startsWith("Cancelled: ")) {
				String newstring = line.replaceFirst("Cancelled: ", "");
				mo.notify(new RunEventId(new RunStateCancelled(), newstring));
			}
		}
	}

	private static class DoneParser implements LineParser {
		@Override
		public void parseLine(String line, Observer<RunEventId> mo) {
			if (line.startsWith("Done: ")) {
				String newstring = line.replaceFirst("Done: ", "");
				mo.notify(new RunEventId(new RunStateDone(), newstring));
			}
		}
	}

	private static class OrParser implements LineParser {
		private final LineParser[] parsers;

		public OrParser(LineParser... pa) {
			parsers = pa.clone();
		}

		@Override
		public void parseLine(String line, Observer<RunEventId> mo) {
			for (LineParser p : parsers) {
				p.parseLine(line, mo);
			}
		}
	}

	private static class ProgressParser implements LineParser {
		@Override
		public void parseLine(String line, Observer<RunEventId> mo) {
			if (line.startsWith("Progress: ")) {
				String[] field = line.replaceFirst("Progress: ", "").trim().split("@");
				try {
					String id = field[0];
					int progress = Integer.parseInt(field[1].trim());
					mo.notify(new RunEventId(new RunStateProgress(progress), id));
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
	}

	private static class RunningParser implements LineParser {
		@Override
		public void parseLine(String line, Observer<RunEventId> mo) {
			if (line.startsWith("Running: ")) {
				String newstring = line.replace("Running: ", "");
				mo.notify(new RunEventId(new RunStateRunning(), newstring));
			}
		}
	}

	@Override
	public void finish() {
		// no can do
	}

	@Override
	public void run() {
		// already running
	}

}
