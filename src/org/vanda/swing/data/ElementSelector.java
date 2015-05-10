package org.vanda.swing.data;

import javax.swing.JComponent;

public interface ElementSelector {

	JComponent getComponent();
	Element getElement();
	void setElement(Element e);
	
}
