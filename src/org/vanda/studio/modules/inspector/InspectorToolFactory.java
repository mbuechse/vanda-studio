package org.vanda.studio.modules.inspector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import org.vanda.datasources.DataSourceMount;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.types.Type;
import org.vanda.util.Factory;
import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.view.View;
import org.vanda.view.Views.*;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.run.BuildContext;
import org.vanda.workflows.run.BuildSystem;

public class InspectorToolFactory implements ToolFactory {

	private final ElementEditorFactories eefs;
	private final Repository<Type, Factory<String, JComponent>> previewFactories;
	private final Repository<String, DataSourceMount> dataSourceRepository;
	private final Repository<String, BuildSystem> buildSystemRepository;

	public final class Inspector {
		private final WorkflowEditor wfe;
		private final Observer<Object> workflowObserver;
		private final SyntaxAnalysis synA;
		private final SemanticAnalysis semA;
		private final BuildContext bc;
		private final Observer<SemanticAnalysis> semanticObserver;
		private final JPanel contentPane;
		private final JPanel panNorth;
		private final JEditorPane inspector;
		private final JScrollPane therealinspector;
		private JComponent editor;
		private JComponent preview;
		private final View view;
		private final Observer<ViewEvent<View>> viewObserver;

		// public Inspector(WorkflowEditor wfe, Model mm, View view) {
		public Inspector(WorkflowEditor wfe, BuildContext bc) {
			this.wfe = wfe;
			this.synA = wfe.getContext(SyntaxAnalysis.class);
			this.semA = wfe.getContext(SemanticAnalysis.class);
			this.bc = bc;
			this.view = wfe.getView();

			// ws = null;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			Font font = UIManager.getFont("Label.font");
			String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize()
					+ "pt; }";
			((HTMLDocument) inspector.getDocument()).getStyleSheet().addRule(bodyRule);

			therealinspector = new JScrollPane(inspector);
			contentPane = new JPanel(new BorderLayout());
			contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.setPreferredSize(new Dimension(800, 300));
			contentPane.setName("Inspector");
			panNorth = new JPanel(new BorderLayout());
			editor = null;
			this.wfe.addToolWindow(contentPane, WindowSystem.SOUTHEAST);

			final ViewListener<View> listener = new ViewListener<View>() {
				@Override
				public void markChanged(View v) {
					// do nothing
				}

				@Override
				public void selectionChanged(View v) {
					update();
				}
			};

			viewObserver = new Observer<ViewEvent<View>>() {

				@Override
				public void notify(ViewEvent<View> event) {
					event.doNotify(listener);
				}
			};
			wfe.getView().getObservable().addObserver(viewObserver);

			workflowObserver = new Observer<Object>() {

				@Override
				public void notify(Object event) {
					update();
				}

			};
			wfe.getView().getWorkflow().getObservable().addObserver(workflowObserver);

			semanticObserver = new Observer<SemanticAnalysis>() {

				@Override
				public void notify(SemanticAnalysis event) {
					update();
				}

			};
			semA.getObservable().addObserver(semanticObserver);

			update();
		}

		public void setEditor(Factory<Database, JComponent> editorFactory) {
			if (editor != null) {
				contentPane.remove(editor);
				editor = null;
			}
			if (editorFactory != null) {
				editor = editorFactory.instantiate(wfe.getDatabase());
				if (editor != null)
					contentPane.add(editor, BorderLayout.EAST);
			}
			contentPane.validate();
		}

		public void setInspection(String inspection) {
			inspector.setText(inspection);
		}

		public void setPreview(Factory<Repository<Type, Factory<String, JComponent>>, JComponent> previewFactory) {
			contentPane.remove(panNorth);
			if (preview != null) {
				contentPane.remove(preview);
				preview = null;
			}
			if (previewFactory != null) {
				try {
					preview = previewFactory.instantiate(previewFactories);
				} catch (Exception e) {
					preview = new JLabel(e.getMessage());
				}
				if (preview == null)
					preview = new JLabel("Could not create preview.");
				// TODO use actions
				// JComponent buttons =
				// previewFactory.createButtons(previewFactories);
				panNorth.removeAll();
				panNorth.add(therealinspector, BorderLayout.CENTER);
				// panNorth.add(buttons, BorderLayout.EAST);
				contentPane.add(panNorth, BorderLayout.NORTH);
				contentPane.add(preview, BorderLayout.CENTER);
			} else
				contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.validate();
		}

		public void update() {
			setInspection(InspectorialVisitor.inspect(synA, semA, view));
			// editor and preview keep track of changes on their own
			setEditor(EditorialVisitor.createAbstractFactory(eefs, view));
			setPreview(PreviewesqueVisitor.createPreviewFactory(semA, synA, view, bc));
		}

	}

	public InspectorToolFactory(ElementEditorFactories eefs, Repository<Type, Factory<String, JComponent>> previewFactories,
			Repository<String, BuildSystem> buildSystemRepository,
			Repository<String, DataSourceMount> dataSourceRepository) {
		this.eefs = eefs;
		this.previewFactories = previewFactories;
		this.buildSystemRepository = buildSystemRepository;
		this.dataSourceRepository = dataSourceRepository;
	}

	@Override
	// public Object instantiate(WorkflowEditor wfe, Model model, View view) {
	public Object instantiate(WorkflowEditor wfe) {
		BuildSystem bs = buildSystemRepository.getItems().iterator().next();
		BuildContext bc = bs.createBuildContext(dataSourceRepository);
		return new Inspector(wfe, bc);
	}

	@Override
	public String getId() {
		return "Inspector tool editor plugin";
	}
}
