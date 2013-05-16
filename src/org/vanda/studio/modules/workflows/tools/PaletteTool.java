package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;
//import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;
import org.vanda.studio.modules.workflows.jgraph.mxDragGestureListener;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.types.Types;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.ToolAdapter;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;

public class PaletteTool implements ToolFactory {
	// TODO see below: beamer mode and stuff
	// public Palette(Application app) {
	// app.getUIModeObservable().addObserver(new Observer<Application>() {
	// @Override
	// public void notify(Application a) {
	// if (a.getUIMode().isLargeContent())
	// component.zoomTo(1.5, false);
	// else
	// component.zoomActual();
	// }
	// });
	// }

	// protected final Application app;
	// protected SemanticsModule sm;

	public static class Palette {
		protected final WorkflowEditor wfe;
		protected mxGraphComponent searchGraph;
		protected JPanel palette;
		protected JXTaskPaneContainer taskPaneContainer;
		protected JTextField textField;
		// protected JSplitPane paletteComponent;
		protected JScrollPane scrollPane;
		protected JXTaskPane resultPane;
		protected JPanel searchPane;
		protected final ArrayList<Job> templates;

		public Palette(WorkflowEditor wfe) {
			this.wfe = wfe;
			taskPaneContainer = new JXTaskPaneContainer();
			scrollPane = new JScrollPane();
			templates = new ArrayList<Job>();
			// paletteComponent = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			// 		scrollPane, new JPanel());
			textField = new JTextField(20);
			textField.setDragEnabled(true);
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					updateSearch(textField.getText());
				}
			});
			resultPane = new JXTaskPane("Search Results");
			resultPane.setVisible(false);
			
			JLabel lab = new JLabel("Search");
			searchPane = new JPanel();
			searchPane.add(lab);
			searchPane.add(textField);
			
			GroupLayout layout = new GroupLayout(searchPane);
			searchPane.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup().addComponent(lab))
					.addGroup(layout.createParallelGroup().addComponent(textField)));

			layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lab).addComponent(textField)));

			
			palette = new JPanel(new BorderLayout());
			palette.setName("Palette");
			palette.add(searchPane, BorderLayout.NORTH);
			palette.add(scrollPane, BorderLayout.CENTER);
			update();
			wfe.setPalette(palette);
		}

		public void update() {
			taskPaneContainer.add(resultPane);

			// get all palette items
			for (Tool t : wfe.getApplication().getToolMetaRepository()
					.getRepository().getItems()) {
				if ("".equals(t.getStatus()))
					templates.add(new Job(new ToolAdapter(t)));
			}
			// templates.add(new AtomicJob(new Choice()));
			// templates.add(new AtomicJob(new InputPort()));
			// templates.add(new AtomicJob(new OutputPort()));
			templates.add(new Job(new LiteralAdapter(new Literal(
					Types.undefined, "literal", null))));
			Collections.sort(templates, new Comparator<Job>() {
				@Override
				public int compare(Job o1, Job o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			// partition items into categories
			Map<String, List<Job>> catMap = new HashMap<String, List<Job>>();
			for (Job template : templates) {
				List<Job> catList = catMap.get(template.getElement()
						.getCategory());
				if (catList == null) {
					catList = new ArrayList<Job>();
					catMap.put(template.getElement().getCategory(), catList);
				}
				catList.add(template);
			}

			// create a new TaskPane for every category and fill it with
			// an mxGraphComponent that contains all tools of this category
			List<String> categories = new ArrayList<String>();
			categories.addAll(catMap.keySet());
			Collections.sort(categories);
			for (String category : categories) {
				JXTaskPane categoryPane = new JXTaskPane(category);
				mxGraphComponent graphComp = renderTemplates(catMap
						.get(category));
				categoryPane.add(graphComp);
				categoryPane.setCollapsed(true);
				taskPaneContainer.add(categoryPane);
			}

			scrollPane.getViewport().add(taskPaneContainer);
		}

		private void updateSearch(String text) {

			// remove previous results
			if (searchGraph != null) {
				resultPane.remove(searchGraph);
				searchGraph = null;
			}

			// find all items that contain the search string
			if (!text.isEmpty()) {
				List<Job> searchResults = new LinkedList<Job>();
				text = text.toLowerCase();
				for (Job template : templates) {
					if (template.getName().toLowerCase().contains(text))
						searchResults.add(template);
				}

				// create graph with search results and add it to display
				if (!searchResults.isEmpty()) {
					searchGraph = renderTemplates(searchResults);
					resultPane.add(searchGraph);
				}
				resultPane.revalidate(); // in particular if searchResults.isEmpty()
				resultPane.setCollapsed(false);
				resultPane.setVisible(true);
			} else
				resultPane.setVisible(false);
		}

	}

	protected static mxGraphComponent renderTemplates(List<Job> ts) {
		DrecksAdapter da = new DrecksAdapter(null);
//		PresentationModel pm = new PresentationModel(null);
//		pm.getVisualization().getGraph().setCellsLocked(true);
//		pm.getVisualization().getGraph().setDropEnabled(false);
//		pm.getVisualization().getGraph().getModel().beginUpdate();
		da.getGraph().setCellsLocked(true);
		da.getGraph().setDropEnabled(false);
		da.getGraph().getModel().beginUpdate();
		try {
			// top left corner of first palette tool, width, height
			double[] d = { 20, 10, 100, 80 };
			for (Job template : ts) {
				template.setDimensions(d);
				mxICell cell = da.renderChild(null, template);
//				mxICell cell = pm.addJobAdapter(template).getJobCell().getVisualization();
				cell.setId(template.getElement().getId());
				d[1] += cell.getGeometry().getHeight() + 10;
			}
		} finally {
//			pm.getVisualization().getGraph().getModel().endUpdate();
			da.getGraph().getModel().endUpdate();
		}
		mxGraphComponent c = new mxGraphComponent(da.getGraph());
//		mxGraphComponent c = new mxGraphComponent(pm.getVisualization().getGraph());
		c.setConnectable(false);
		c.setDragEnabled(false);
		DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(c.getGraphControl(),
				DnDConstants.ACTION_COPY_OR_MOVE,
				new mxDragGestureListener(c.getGraph()));
		return c;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Palette(wfe);
	}

}