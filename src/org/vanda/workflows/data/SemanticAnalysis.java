package org.vanda.workflows.data;

import java.util.Map;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.SyntaxAnalysis;

/**
 * Holds and updates the DataflowAnalysis of a Workflow.
 * @author kgebhardt
 *
 */
public class SemanticAnalysis implements Observer<Object>, DatabaseListener<Database> {
	protected DataflowAnalysis dfa;
	private Map<Integer, String> assignment = null;
	private Job[] sorted = null;
	private MultiplexObserver<SemanticAnalysis> observable;

	public SemanticAnalysis() {
		this.observable = new MultiplexObserver<SemanticAnalysis>();
	}

	public void updateDFA() {
		dfa = new DataflowAnalysis();
		dfa.init(assignment, sorted);
		observable.notify(this);
	}

	/**
	 * 
	 * @return DataFlowAnalysis of current assignment
	 */
	public DataflowAnalysis getDFA() {
		return dfa;
	}


	public Observable<SemanticAnalysis> getObservable() {
		return observable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void notify(Object event) {
		if (event instanceof DatabaseEvent) {
			((DatabaseEvent<Database>) event).doNotify(this);
		} else if (event instanceof SyntaxAnalysis) {
			sorted = ((SyntaxAnalysis) event).getSorted();
			updateDFA();
		}
	}

	@Override
	public void cursorChange(Database d) {
		assignment = d.getRow(d.getCursor());
		updateDFA();
	}

	@Override
	public void dataChange(Database d, Object key) {
		assignment = d.getRow(d.getCursor());  // probably unnecessary
		updateDFA();
	}

	@Override
	public void nameChange(Database d) {
		// do nothing
	}
	
}
