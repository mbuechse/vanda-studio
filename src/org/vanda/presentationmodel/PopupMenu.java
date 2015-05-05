package org.vanda.presentationmodel;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/**
 * a context popup menu that displays a components title
 * 
 * @author afischer
 * 
 */
@SuppressWarnings("serial")
public class PopupMenu extends JPopupMenu {

	public PopupMenu(String title) {
		add(new JLabel("<html><b>" + title + "</b></html>"));
		addSeparator();
	}
}