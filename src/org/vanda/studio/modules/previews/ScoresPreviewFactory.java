package org.vanda.studio.modules.previews;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.vanda.util.Factory;

public class ScoresPreviewFactory implements Factory<String, JComponent> {
	
	@Override
	public JComponent instantiate(String absolutePath) {
		File scores = new File(absolutePath);
		File meta = new File(absolutePath + ".meta");
		Scanner sScores = null, sMeta = null, sSentences = null;
		try {
			// read stuff from file
			if (!scores.exists())
				return null;
			sScores = new Scanner(scores);
			sMeta = new Scanner(meta);
			File sentences = new File(sMeta.nextLine());
			sMeta.close();
			sSentences = new Scanner(sentences);
			List<String> left = new ArrayList<String>();
			List<Double> right = new ArrayList<Double>();
			while (sScores.hasNextLine() && sSentences.hasNextLine()) {
				left.add(sSentences.nextLine());
				double d = Double.parseDouble(sScores.nextLine());
				if (d < 0)
					right.add(Math.exp(d));
				else
					right.add(d);
			}
			sScores.close();
			sSentences.close();
			
			// write to array
			String[][] data = new String[left.size()][2];
			for (int i = 0; i < left.size(); i++) {
				data[i][0] = left.get(i);
				data[i][1] = Double.toString(right.get(i));
			}
			// build GUI
			JTable jTable = new JTable(data, new String[] {"element", "score"});
			JComponent result = new JScrollPane(jTable);
			return result;
		} catch (FileNotFoundException e) {
			// app.sendMessage(new ExceptionMessage(e));
			return null;
		} finally {
			if (sScores != null)
				sScores.close();
			if (sMeta != null)
				sMeta.close();
			if (sSentences != null)
				sSentences.close();
		}
	}

}
