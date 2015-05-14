package org.vanda.studio.modules;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.vanda.presentationmodel.PresentationModel;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.editor.ToolFactory;
import org.vanda.studio.editor.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.MetaRepository;
import org.vanda.util.Repository;
import org.vanda.util.StaticRepository;
import org.vanda.workflows.elements.Tool;
import org.w3c.dom.Document;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public final class WorkflowPDFModule implements Module {

	private final MetaRepository<String, ToolFactory> toolFactoryMeta;
	private final Repository<String, Tool> toolRepository;

	protected final class WorkflowToPDFToolFactory implements ToolFactory {

		protected final class ExportWorkflowToPDFAction implements Action {

			private final WorkflowEditor wfe;

			public ExportWorkflowToPDFAction(WorkflowEditor wfe) {
				this.wfe = wfe;
			}

			@Override
			public String getName() {
				return "Export to PDF...";
			}

			@Override
			public void invoke() {
				JFileChooser chooser = new JFileChooser("");
				chooser.setDialogType(JFileChooser.SAVE_DIALOG);
				chooser.setVisible(true);
				int result = chooser.showSaveDialog(null);
				// wfe.getApplication().getWindowSystem().getMainWindow()

				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile;
					if (chooser.getSelectedFile().getName().toLowerCase().endsWith(".pdf"))
						chosenFile = chooser.getSelectedFile();
					else
						chosenFile = new File(chooser.getSelectedFile().getPath() + ".pdf");
					try {
						PresentationModel pm = new PresentationModel(wfe.getView(), toolRepository);
						mxGraph graph = pm.getVisualization().getGraph();
						Document svg = mxCellRenderer.createSvgDocument(graph, null, 1, null, null);
						String code = mxUtils.getPrettyXml(svg.getDocumentElement());
						TranscoderInput input = new TranscoderInput(new StringReader(code));
						PDFTranscoder t = new PDFTranscoder();
						TranscoderOutput output = new TranscoderOutput(new FileOutputStream(chosenFile));
						t.transcode(input, output);
						output.getOutputStream().flush();
						output.getOutputStream().close();
					} catch (Exception e) {
						// app.sendMessage(new ExceptionMessage(e));
					}
				}
			}
		}

		@Override
		public Object instantiate(WorkflowEditor wfe) {
			Action a = new ExportWorkflowToPDFAction(wfe);
			wfe.addAction(a, "application-pdf", KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK), 10);
			return a;
		}

		@Override
		public String getId() {
			return "Export to PDF tool";
		}
	}

	@Override
	public String getId() {
		return "PDF exporter tool";
	}

	@Override
	public Object instantiate(Application d) {
		StaticRepository<String, ToolFactory> sr = new StaticRepository<String, ToolFactory>();
		ToolFactory tf = new WorkflowToPDFToolFactory();
		sr.put(tf.getId(), tf);
		toolFactoryMeta.addRepository(sr);
		return sr;
	}

	public WorkflowPDFModule(MetaRepository<String, ToolFactory> toolFactoryMeta,
			Repository<String, Tool> toolRepository) {
		this.toolFactoryMeta = toolFactoryMeta;
		this.toolRepository = toolRepository;
	}

}