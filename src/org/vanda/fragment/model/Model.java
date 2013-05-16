package org.vanda.fragment.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.studio.modules.workflows.model.WorkflowDecoration;
import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeChecker;
import org.vanda.workflows.hyper.TypeCheckingException;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

public class Model implements Observer<WorkflowEvent<MutableWorkflow>>, WorkflowListener<MutableWorkflow> {
	
	// private final org.vanda.studio.model.Model model;
	private final MutableWorkflow hwf;
	private final Database db;
	protected Job[] sorted = null;
	private Map<Object, Type> types = Collections.emptyMap();
	private Type fragmentType = null;
	private DataflowAnalysis dfa;
	private MultiplexObserver<DataflowAnalysis> dfaChangedObservable;
	

	// FIXME package dependency: WorkflowDecoration is in a Vanda Studio module
	public Model(WorkflowDecoration deco, Database db) {
		hwf = deco.getRoot();
		this.db = db;
		// this.model = model;
		dfaChangedObservable = new MultiplexObserver<DataflowAnalysis>();
		deco.getWorkflowCheckObservable().addObserver(new Observer<WorkflowDecoration>() {

			@Override
			public void notify(org.vanda.studio.modules.workflows.model.WorkflowDecoration event) {
				dfa = new DataflowAnalysis(event.getRoot(), event.getSorted(), event.getFragmentType());
				dfa.init();
				dfaChangedObservable.notify(dfa);
			}
		};
	}
	
	public void typeCheck() throws TypeCheckingException {
		TypeChecker tc = new TypeChecker();
		hwf.typeCheck(tc);
		tc.check();
		types = tc.getTypes();
		fragmentType = tc.getFragmentType();
		
	}

	public void checkWorkflow() {
		// markedElements.clear();
		try {
			sorted = null;
			typeCheck();
			try {
				sorted = hwf.getSorted();
			} catch (Exception e) {
				// FIXME send message that there are cycles
>>>>>>> vanilla
			}
			dfa = new DataflowAnalysis(hwf, db, sorted, fragmentType);
			dfa.init();
			dfaChangedObservable.notify(dfa);
		} catch (TypeCheckingException e) {
			List<Pair<String, Set<ConnectionKey>>> errors = e.getErrors();
			for (Pair<String, Set<ConnectionKey>> error : errors) {
				// TODO use new color in each iteration
				Set<ConnectionKey> eqs = error.snd;
				// for (ConnectionKey eq : eqs)
				// 	markedElements.add(new ConnectionSelection(hwf, eq));
			}

		}
		// markedElementsObservable.notify(this);
		// weg workflowCheckObservable.notify(this);
	}
	
	public Job[] getSorted() {
		return sorted;
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Type getType(Object variable) {
		return types.get(variable);
	}

	public DataflowAnalysis getDataflowAnalysis() {
		return dfa;
	}
	
	public Observable<DataflowAnalysis> getDfaChangedObservable() {
		return dfaChangedObservable;
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
		checkWorkflow(); // FIXME
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
	}

	@Override
	public void updated(MutableWorkflow mwf) {
		checkWorkflow();
	}

	@Override
	public void notify(WorkflowEvent<MutableWorkflow> event) {
		event.doNotify(this);
	}

}
