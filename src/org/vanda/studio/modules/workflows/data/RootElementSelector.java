package org.vanda.studio.modules.workflows.data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.RootDataSource;
import org.vanda.util.Factory;
import org.vanda.util.Observer;

public final class RootElementSelector implements ElementSelector, Observer<Element> {

	private final RootDataSource rds;
	private final Factory<DataSource, ElementSelector> fr;

	private DataSourceMount current;
	private Element innerElement;
	private Observer<Element> innerListener;

	private Element element;
	private ElementSelector elementSelector;

	private List<DataSourceMount> dsList;

	private JComboBox<DataSourceMount> jDSList;
	private JComponent component;
	private JPanel selector;

	public RootElementSelector(RootDataSource rds, Factory<DataSource, ElementSelector> fr) {
		this.rds = rds;
		this.fr = fr;

		current = null;
		innerElement = new Element("");
		innerListener = new Observer<Element>() {
			@Override
			public void notify(Element event) {
				String v0 = element.getValue();
				int i = v0.indexOf(':');
				element.setValue(v0.substring(0, i) + ":" + event.getValue());
			}
		};
		innerElement.getObservable().addObserver(innerListener);

		dsList = new ArrayList<DataSourceMount>(rds.getMountTable().getItems());
		Collections.sort(dsList, new Comparator<DataSourceMount>() {
			@Override
			public int compare(DataSourceMount o1, DataSourceMount o2) {
				return o1.id.compareTo(o2.id);
			}
		});

		selector = new JPanel(new GridBagLayout());

		jDSList = new JComboBox<DataSourceMount>(dsList.toArray(new DataSourceMount[dsList.size()]));
		jDSList.setRenderer(new DataSourceMountRenderer());
		jDSList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (element != null) {
					String value = element.getValue();
					DataSourceMount selected = (DataSourceMount) jDSList.getSelectedItem();
					int i = value.indexOf(':');
					if (i < 0 || !selected.id.equals(value.substring(0, i)))
						element.setValue(selected.id + ":" + selected.ds.createElement());
				}
			}
		});

		component = new JPanel(new BorderLayout());

		Insets i = new Insets(2, 2, 2, 2);
		int anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;

		selector.add(jDSList, new GridBagConstraints(0, 0, 3, 1, 1, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));

		selector.add(component, new GridBagConstraints(0, 2, 3, 1, 1, 1, anchor, GridBagConstraints.BOTH, i, 1, 1));
	}

	@Override
	public JComponent getComponent() {
		return selector;
	}

	@Override
	public Element getElement() {
		return element;
	}

	@Override
	public void setElement(Element e) {
		if (element != e) {
			if (element != null)
				element.getObservable().removeObserver(this);
			element = e;
			if (element != null) {
				element.getObservable().addObserver(this);
				notify(element);
			}
		}
	}

	@Override
	public void notify(Element e) {
		String v = e.getValue();
		int i = v.indexOf(':');
		if (i == -1) {
			System.err.println(v);
			return;
		}
		String prefix = v.substring(0, i);
		String value = v.substring(i + 1);
		if (current == null || !current.id.equals(prefix)) {
			component.removeAll();
			if (elementSelector != null)
				elementSelector.setElement(null);
			current = rds.getMountTable().getItem(prefix);
			jDSList.setSelectedItem(current);
			innerElement.setValue(value);
			elementSelector = fr.instantiate(current.ds);
			elementSelector.setElement(innerElement);
			component.add(elementSelector.getComponent(), BorderLayout.CENTER);
		}
		selector.validate();
	}

	// XXX this is almost a verbatim copy from DataSourceRepositoryEditor...
	private static class DataSourceMountRenderer implements ListCellRenderer<DataSourceMount> {

		DefaultListCellRenderer r;

		public DataSourceMountRenderer() {
			r = new DefaultListCellRenderer();
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends DataSourceMount> list, DataSourceMount value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String s = "";
			if (value != null)
				s = value.id;
			return r.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
		}

	}
}