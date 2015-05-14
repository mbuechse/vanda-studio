package org.vanda.studio.modules.previews;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vanda.util.Factory;

public final class LogPreviewFactory implements Factory<String, JComponent> {

	@Override
	public JComponent instantiate(String value) {
		List<LogEntry> entries = new ArrayList<LogEntry>();
		try {
			File file = new File(value);
			FileInputStream fis = new FileInputStream(file);
			fis.skip(Math.max(0, file.length() - 524288));
			BufferedReader input = new BufferedReader(new InputStreamReader(fis));
			try {
				String line = null;
				LogEntry entry = null;
				while ((line = input.readLine()) != null) {
					if (line.startsWith("Checking: ")) {
						entry = new LogEntry(true);
						entries.add(entry);
					} else {
						if (entry == null) {
							entry = new LogEntry(false);
							entries.add(entry);
						}
						entry.appendLine(line);
					}
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			return new JLabel("Log does not exist.");
		}
		if (entries.isEmpty())
			return new JLabel("Log is empty.");
		Collections.reverse(entries);
		final JComboBox<LogEntry> le = new JComboBox<LogEntry>(entries.toArray(new LogEntry[0]));
		final JTextArea ta = new JTextArea(entries.get(0).getText());
		le.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ta.setText(((LogEntry) le.getSelectedItem()).getText());
			}
		});
		JPanel pan = new JPanel(new BorderLayout());
		pan.add(le, BorderLayout.NORTH);
		pan.add(new JScrollPane(ta), BorderLayout.CENTER);
		return pan;
	}
	
	private class LogEntry {
		private String date = "<incomplete>";
		@SuppressWarnings("unused")
		private int exitCode = -1;
		private String text = "";
		private boolean expectDate;
		
		public LogEntry(boolean expectDate) {
			this.expectDate = expectDate;
		}
		
		public void appendLine(String line) {
			text += line;
			if (expectDate) {
				date = line;				
				expectDate = false;
			}
			text += System.getProperty("line.separator");
			String pattern = "[A-Z][a-z] \\d+\\. [A-Z][a-z][a-z] \\d\\d:\\d\\d:\\d\\d [A-Z]* \\d\\d\\d\\d";
			if (date.equals("<incomplete>") && line.matches(pattern)) {
				date = line;
			} else {
				if (line.startsWith("Skipping: ")) {
					exitCode = 0;
				} else if (line.startsWith("Returned: ")) {
					exitCode = Integer.parseInt(line.substring(10));
				}
			}
		};
		
		public String getText() {
			return text;
		}
		
		@Override
		public String toString() {
			return date;
		}
		
	}

}
