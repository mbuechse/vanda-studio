package org.vanda.studio.modules.run;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.vanda.runner.RunConfig;

// MB: TODO MVC editor for RunConfig objects

public class RunConfigEditor {

	private JPanel pan;
	private JTextField tFolder;
	private JLabel lFolder;
	public File dir;
	
	public JComponent getComponent() {
		return pan;
	}

	public RunConfigEditor(final RunConfig rc) {		
		// Panel and basic Layout
		pan = new JPanel();
		GroupLayout layout = new GroupLayout(pan);
		pan.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		// Execution Environment Folder Selection
		dir = new File(rc.getPath());
		lFolder = new JLabel("Execution Environment");
		tFolder = new JTextField(dir.getAbsolutePath());
		tFolder.setMaximumSize(new Dimension(Short.MAX_VALUE, JTextField.HEIGHT));
		tFolder.setPreferredSize(new Dimension(200, 20));
		JButton bFolder = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = -2965900290520148139L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(new File(tFolder.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					dir = fc.getSelectedFile();
					tFolder.setText(dir.getAbsolutePath());
				}
			}
		});
		bFolder.setMaximumSize(new Dimension(bFolder.getPreferredSize().width, JTextField.HEIGHT));

		SequentialGroup exexutionEnvironmentHorizontal = layout.createSequentialGroup().addComponent(lFolder)
				.addComponent(tFolder).addComponent(bFolder);
		ParallelGroup executionEnvironmentVertical = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lFolder).addComponent(tFolder).addComponent(bFolder);

		// AssignmentSelectionTable
		JPanel tablePane = new JPanel();
		GroupLayout tableLayout = new GroupLayout(tablePane);
		tablePane.setLayout(tableLayout);

		JButton exButton = new JButton(new AbstractAction("Save and close") {
			private static final long serialVersionUID = 3626621817499179974L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO MVC stuff
			}
		});

	}
}
