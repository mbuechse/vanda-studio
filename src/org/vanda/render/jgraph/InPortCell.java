package org.vanda.render.jgraph;

import com.mxgraph.model.mxICell;

public class InPortCell extends Cell {

	public InPortCell(Graph graph) {
		super(JGraphRendering.inPortRenderer, graph);
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
		return LayoutManager.INPORT;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public boolean isValidConnectionSource() {
		return false;
	}

	@Override
	public boolean isValidConnectionTarget() {
		return true;
	}

	@Override
	public boolean isValidDropTarget() {
		return false;
	}
}
