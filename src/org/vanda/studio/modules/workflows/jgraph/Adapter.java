package org.vanda.studio.modules.workflows.jgraph;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public interface Adapter {
	String getName();
	mxICell dereference(ListIterator<Token> path, mxICell current);
	boolean inModel();
	void prependPath(LinkedList<Token> path);
	void update(mxGraph graph, mxICell parent, mxICell cell);
	void register(mxICell parent, mxICell cell);
	void remove(mxICell parent);
	void setSelection(Model m, List<Token> path);
}
