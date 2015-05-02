package org.vanda.workflows.hyper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.workflows.hyper.TopSorter.TopSortException;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

/**
 * Performs type checking and topological sorting of a Workflow, and stores the results 
 * @author kgebhardt
 *
 */
public class SyntaxAnalysis  implements Observer<WorkflowEvent<MutableWorkflow>>, WorkflowListener<MutableWorkflow> {
	private Map<Object, Type> types = Collections.emptyMap();
	private Type fragmentType = null;
	private MultiplexObserver<SyntaxAnalysis> syntaxChangedObservable;
	protected Collection<ConnectionKey> cycles = null;
	protected List<Pair<String, Set<ConnectionKey>>> typeErrors = null;
	protected Job[] sorted = null;

	public SyntaxAnalysis() {
		syntaxChangedObservable = new MultiplexObserver<SyntaxAnalysis>();
	}

	public void typeCheck(MutableWorkflow hwf) throws TypeCheckingException {
		types = Collections.emptyMap();
		fragmentType = null;
		TypeChecker tc = new TypeChecker();
		hwf.typeCheck(tc);
		tc.check();
		types = tc.getTypes();
		fragmentType = tc.getFragmentType();
	}

	public void checkWorkflow(MutableWorkflow hwf) {
		sorted = null;
		cycles = null;
		typeErrors = null;
		try {
			typeCheck(hwf);
			sorted = hwf.getSorted();
		} catch (TypeCheckingException e) {
			typeErrors = e.errors;
		} catch (TopSortException e) {
			cycles = e.getCyclicConnections();
		}
		syntaxChangedObservable.notify(this);
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
	
	public List<Pair<String, Set<ConnectionKey>>> getTypeErrors() {
		return typeErrors;
	}
	
	public Collection<ConnectionKey> getCyclicConnections() {
		return cycles;
	}

	public MultiplexObserver<SyntaxAnalysis> getSyntaxChangedObservable() {
		return syntaxChangedObservable;
	}
	

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
		checkWorkflow(mwf);
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
		checkWorkflow(mwf);
	}

	@Override
	public void notify(WorkflowEvent<MutableWorkflow> event) {
		event.doNotify(this);
	}
}
