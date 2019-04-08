package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.Cells.MarkChangedEvent;
import org.vanda.render.jgraph.Cells.RemoveCellEvent;
import org.vanda.render.jgraph.Cells.SetSelectionEvent;
import org.vanda.util.Observer;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;

public class ConnectionCell extends Cell {
	boolean handdrawn;

	public ConnectionCell() {
		super(null, null);
		handdrawn = true;
	}

	public ConnectionCell(final Graph graph, OutPortCell source, InPortCell target) {
		// r = null, to fit connection creation scheme
		super(null, graph);
		handdrawn = false;

		// create mxCell and add it to Graph
		mxICell sourceVis = source.getVisualization();
		mxICell targetVis = target.getVisualization();

		if (sourceVis != null && targetVis != null) {
			graph.beginUpdate();
			try {
				visualization = (mxCell) graph.getGraph().createEdge(
						graph.getGraph().getDefaultParent(), null, this,
						sourceVis, targetVis, null);

				graph.getGraph().addEdge(visualization,
						graph.getGraph().getDefaultParent(), sourceVis,
						targetVis, null);
			} finally {
				graph.endUpdate();
			}
		} else
			assert (false);

	}

	public OutPortCell getSourceCell() {
		return (OutPortCell) visualization.getSource().getValue();
	}

	@Override
	public void highlight(boolean highlight) {
		// XXX erroneous is what we want here, but naming is suboptimal
		if (JGraphRendering.erroneousStyle.updateStyle(visualization, highlight))
			getObservable().notify(new MarkChangedEvent<Cell>(this));

	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		// no observers (-> no ConnectionAdapter) -> hand-drawn edge
		if (handdrawn) {
			mxIGraphModel model = graph.getGraph().getModel();
			Object source = model.getTerminal(cell, true);
			Object target = model.getTerminal(cell, false);

			// ignore "unfinished" edges
			if (source != null && target != null) {
				visualization = (mxCell) cell;

				InPortCell tval = (InPortCell) model.getValue(target);
				JobCell tparval = (JobCell) model.getValue(model
						.getParent(target));

				// register graph for cell changes
				graphObserver = new Observer<CellEvent<Cell>>() {

					@Override
					public void notify(CellEvent<Cell> event) {
						event.doNotify(graph.getCellChangeListener());
					}

				};
				getObservable().addObserver(graphObserver);

				// Create ConnectionAdapter
				((WorkflowCell) ((mxICell) graph.getGraph().getDefaultParent())
						.getValue()).getDataInterface().createConnection(this,
						tparval, tval);

			}
		}
	}

	@Override
	public void onRemove() {
		getObservable().notify(new RemoveCellEvent<Cell>(this));
	}

	@Override
	public void onResize(Graph graph) {
		// do nothing
	}

	@Override
	public void setSelection(boolean selected) {
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.CONNECTION;
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

	@Override
	public boolean isValidConnectionSource() {
		return false;
	}

	@Override
	public boolean isValidConnectionTarget() {
		return false;
	}

	@Override
	public boolean isValidDropTarget() {
		return false;
	}

	@Override
	public String getLabel() {
		return ""; //this.toString();
	}
}
