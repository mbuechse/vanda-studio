package org.vanda.studio.modules.workflows.model;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.studio.app.LayoutSelector;
import org.vanda.util.Action;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.SyntaxAnalysis;


public interface WorkflowEditor {
	void addAction(Action a, KeyStroke keyStroke);
	void addAction(Action a, KeyStroke keyStroke, int pos);
	void addToolWindow(JComponent c, LayoutSelector layout);
	void focusToolWindow(JComponent c);

	View getView();
	Database getDatabase();
	SyntaxAnalysis getSyntaxAnalysis();
	
	String getProperty(String key);
	void setProperty(String key, String value);

	void removeToolWindow(JComponent c);
	void addAction(Action a, String imageName, KeyStroke keyStroke, int pos);
	void enableAction(Action a);
	void disableAction(Action a);
}
