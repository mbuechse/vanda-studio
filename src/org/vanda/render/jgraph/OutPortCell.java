package org.vanda.render.jgraph;

import com.mxgraph.model.mxICell;

public class OutPortCell extends Cell {
	public OutPortCell(Graph graph) 
	{
		super(JGraphRendering.outPortRenderer, graph);
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		// do nothing
	}

	@Override
	public void onRemove() {
		// do nothing
	}

	@Override
	public void onResize(Graph graph) {
		// do nothing
	}

	@Override
	public void setSelection(boolean selected) {
		// do nothing
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.OUTPORT;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public boolean isValidConnectionSource() {
		return true;
	}

	@Override
	public boolean isValidConnectionTarget() {
		return false;
	}

	@Override
	public boolean isValidDropTarget() {
		return false;
	}
}
