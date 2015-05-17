package org.vanda.studio.editor;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.studio.app.LayoutSelector;
import org.vanda.util.Action;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;


public interface WorkflowEditor {
	void addAction(Action a, KeyStroke keyStroke);
	void addAction(Action a, KeyStroke keyStroke, int pos);
	void addToolWindow(JComponent c, LayoutSelector layout);
	void focusToolWindow(JComponent c);

	View getView();
	Database getDatabase();
	<T> T getContext(Class<T> clazz);
	
	void removeToolWindow(JComponent c);
	void addAction(Action a, String imageName, KeyStroke keyStroke, int pos);
	void enableAction(Action a);
	void disableAction(Action a);
}
