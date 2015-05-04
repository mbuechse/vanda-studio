package org.vanda.studio.modules.workflows.data;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.RootDataSource;
import org.vanda.util.FactoryRegistry;
import org.vanda.util.Observer;

public final class RootElementSelector implements ElementSelector, Observer<Element> {

	private final RootDataSource rds;
	private final FactoryRegistry<DataSource, ElementSelector> fr;

	private DataSourceMount current;
	private Element innerElement;

	private Element element;
	private ElementSelector elementSelector;

	private List<String> dsList;

	private JComboBox jDSList;
	private JComponent component;
	private JPanel selector;

	public RootElementSelector(RootDataSource rds, FactoryRegistry<DataSource, ElementSelector> fr) {
		this.rds = rds;
		this.fr = fr;

		current = null;
		innerElement = null;

		dsList = new ArrayList<String>(rds.getMountTable().getKeys());
		dsList.add(""); // XXX why?
		Collections.sort(dsList);

		selector = new JPanel(new GridBagLayout());

		jDSList = new JComboBox(dsList.toArray());
		jDSList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (element != null) {
					String value = element.getValue();
					String prefix = jDSList.getSelectedItem().toString();
					int i = value.indexOf(':');
					if (i < 0 || !prefix.equals(value.substring(0, i)))
						element.setValue(prefix + ":"
								+ RootElementSelector.this.rds.getMountTable().getItem(prefix).ds.createElement());
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
			jDSList.setSelectedItem(prefix);
			innerElement = new Element(value);
			FactoryRegistry.Factory<DataSource, ElementSelector> factory = fr.registry.get(current.ds.getClass());
			if (factory != null) {
				elementSelector = factory.instantiate(current.ds);
				elementSelector.setElement(innerElement);
				component.add(elementSelector.getComponent(), BorderLayout.CENTER);
			} else {
				elementSelector = null;
				innerElement = null;
			}
		}
		selector.validate();
	}
}