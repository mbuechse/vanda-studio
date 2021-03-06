package org.vanda.studio.modules.inspector;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;

public class WorkflowElementEditor implements ElementEditorFactory<MutableWorkflow> {

	@Override
	public JComponent createEditor(Database d, MutableWorkflow wf,
			final MutableWorkflow mwf) {
		final JLabel label = new JLabel("Name:");
		final JTextField text = new JTextField(mwf.getName(), 20);
		text.addKeyListener(new KeyListener() {		
			@Override
			public void keyTyped(KeyEvent e) { }
			
			@Override
			public void keyReleased(KeyEvent e) { }
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					mwf.setName(text.getText());	
			}
		});
		
//		text.getDocument().addDocumentListener(new DocumentListener() {
//
//			@Override
//			public void insertUpdate(DocumentEvent e) {
//				mwf.setName(text.getText());
//			}
//
//			@Override
//			public void removeUpdate(DocumentEvent e) {
//				mwf.setName(text.getText());
//			}
//
//			@Override
//			public void changedUpdate(DocumentEvent e) {
//			}
//
//		});

		JPanel editor = new JPanel();
		GroupLayout layout = new GroupLayout(editor);
		editor.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(label).addComponent(text));
		layout.setVerticalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(label).addComponent(text));
		return editor;
	}

}
