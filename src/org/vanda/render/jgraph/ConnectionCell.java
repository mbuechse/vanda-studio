package org.vanda.render.jgraph;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class ConnectionCell extends Cell {
	private ConnectionKey connectionKey;
	private ConnectionViewListener connectionViewListener;
	private class ConnectionViewListener implements AbstractView.ViewListener<AbstractView> {
	
		@Override
		public void selectionChanged(AbstractView v) {
			getObservable().notify(new SelectionChangedEvent<Cell>(ConnectionCell.this)); 
		}

		@Override
		public void markChanged(AbstractView v) {
			if (v.isHighlighted())
				visualization.setStyle("highlightededge");
			else
				visualization.setStyle("defaultEdge");
			getObservable().notify(new MarkChangedEvent<Cell>(ConnectionCell.this));
		}

		@Override
		public void highlightingChanged(AbstractView v) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public ConnectionCell() {	
		connectionKey = null;
		this.observable = new CellObservable();
	}
	
	public ConnectionCell(ConnectionKey connectionKey, final Graph graph, PortCell source, PortCell target) {
		this.connectionKey = connectionKey;
		this.connectionViewListener = new ConnectionViewListener();
		this.observable = new CellObservable();
		
		// Register at ConnectionView
		graph.getView().getConnectionView(connectionKey).getObservable().addObserver(new Observer<ViewEvent<AbstractView>> () {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(connectionViewListener);
			}
			
		});
		
		// Register at Graph
		getObservable().addObserver(new org.vanda.util.Observer<CellEvent<Cell>> () {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(graph.getCellChangeListener());	
			}
			
		});
		
		// create mxCell and add it to Graph
		mxICell sourceVis = source.getVisualization();
		mxICell targetVis = target.getVisualization();

		if (sourceVis != null && targetVis != null) {
			graph.getGraph().getModel().beginUpdate();
			try {
				visualization = (mxCell) graph.getGraph().createEdge(graph.getGraph().getDefaultParent()
					, null, this, sourceVis, targetVis
					, null);
			
				graph.getGraph().addEdge(visualization, graph.getGraph().getDefaultParent(),
						sourceVis, targetVis, null);
			} finally {
				graph.getGraph().getModel().endUpdate();
			}
		} else
			assert (false);
		
	}
	
	@Override
	public String getType() {
		return "ConnectionCell";
	}

	@Override
	public void setSelection(View view) {
		if (connectionKey != null)
			view.getConnectionView(connectionKey).setSelected(true);
		else 
			view.clearSelection();
	}

	@Override
	public void onRemove(View view) {
		System.out.println("remove Connection ConCell");
		if (connectionKey != null) {
			if (connectionKey.target.isInserted()) // TODO it could be necessary to do more checks here
				view.getWorkflow().removeConnection(connectionKey);
		}
		//visualization.removeFromParent();
		//getObservable().notify(new CellRemovedEvent<Cell>(this));
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		// not in the model -> hand-drawn edge
		if (connectionKey == null) {
			mxIGraphModel model = graph.getGraph().getModel();
			Object source = model.getTerminal(cell, true);
			Object target = model.getTerminal(cell, false);

			// ignore "unfinished" edges
			if (source != null && target != null) {
				
				PortCell sval = (PortCell) model.getValue(source);
				PortCell tval = (PortCell) model.getValue(target);
				JobCell sparval = (JobCell) model.getValue(model
						.getParent(source));
				JobCell tparval = (JobCell) model.getValue(model
						.getParent(target));

				connectionKey = new ConnectionKey(tparval.job, tval.port);
				visualization = (mxCell) cell;
				
				//Create ConnectionAdapter
				PresentationModel pm = (PresentationModel) 
						((WorkflowCell) sparval.getVisualization()
								.getParent().getValue()).getPresentationModel();
				
				// Add Connection to Workflow
				graph.getView().getWorkflow().addConnection(connectionKey, 
							sparval.job.bindings.get(sval.port));
				
				// Add Connection to PM 
				pm.addConnectionAdapter(this, connectionKey);
				
				this.connectionViewListener = new ConnectionViewListener();
				
				// Register at ConnectionView
				graph.getView().getConnectionView(connectionKey).getObservable().addObserver(new Observer<ViewEvent<AbstractView>> () {

					@Override
					public void notify(ViewEvent<AbstractView> event) {
						event.doNotify(connectionViewListener);
					}
					
				});
				
				// Register at Graph
				getObservable().addObserver(new Observer<CellEvent<Cell>> () {

					@Override
					public void notify(CellEvent<Cell> event) {
						event.doNotify(graph.getCellChangeListener());	
					}
					
				});
				

			}
		} 		
	}

	@Override
	public void onResize(mxGraph graph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractView getView(View view) {
		return view.getConnectionView(connectionKey);
	}

}
