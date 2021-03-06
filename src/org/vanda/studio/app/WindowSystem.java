/**
 * 
 */
package org.vanda.studio.app;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.vanda.util.Action;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface WindowSystem {
	
	static final LayoutSelector CENTER = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getCenter();
		}
	};
	
	static final LayoutSelector NORTH = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getNorth();
		}
	};

	static final LayoutSelector NORTHWEST = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getNorthWest();
		}
	};

	static final LayoutSelector WEST = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getWest();
		}
	};

	static final LayoutSelector SOUTHWEST = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getSouthWest();
		}
	};

	static final LayoutSelector SOUTH = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getSouth();
		}
	};

	static final LayoutSelector SOUTHEAST = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getSouthEast();
		}
	};

	static final LayoutSelector EAST = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getEast();
		}
	};

	static final LayoutSelector NORTHEAST = new LayoutSelector() {
		@Override
		public <L> L selectLayout(LayoutAssortment<L> la) {
			return la.getNorthEast();
		}
	};
	
	/**
	 * Call this *before* adding c as a contentWindow.
	 */
	void addAction(JComponent c, Action a, KeyStroke keyStroke);
	
	/**
	 */
	void addAction(JComponent c, Action a, KeyStroke keyStroke, int pos);

	/**
	 * Creates a new tab in the main pane.
	 * The action a is invoked when
	 * the little x-button within the tab title is clicked. If a null action is
	 * used, the tab will simply be removed from the display.
	 */
	void addContentWindow(Icon i, JComponent c, Action a);

	/**
	 */
	void addSeparator();
	
	/**
	 */
	void addToolWindow(JComponent window, Icon i, JComponent c, LayoutSelector layout);
	
	/**
	 */
	void disableAction(Action a);
	
	/**
	 */
	void disableAction(JComponent window, Action a);
	
	/**
	 */
	void enableAction(Action a);
	
	/**
	 */
	void enableAction(JComponent window, Action a);
	/**
	 */
	void focusContentWindow(JComponent c);
	
	void focusToolWindow(JComponent c);
	
	/**
	 */
	JFrame getMainWindow();
	
	/**
	 */
	void removeContentWindow(JComponent c); 

	/**
	 */
	void removeToolWindow(JComponent window, JComponent c);

	/**
	 */
	void addAction(JComponent c, Action a, String imageName, KeyStroke keyStroke, int pos);

}
