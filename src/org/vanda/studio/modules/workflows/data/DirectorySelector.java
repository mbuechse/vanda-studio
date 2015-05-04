package org.vanda.studio.modules.workflows.data;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DirectoryDataSource;
import org.vanda.util.FactoryRegistry;
import org.vanda.util.Observer;

public class DirectorySelector implements ElementSelector, Observer<Element> {

	private final DirectoryDataSource directoryDataSource;
	private Element element;
	private JList selector;
	private JScrollPane scroll;

	public DirectorySelector(DirectoryDataSource directoryDataSource) {
		this.directoryDataSource = directoryDataSource;
		selector = new JList();
		String[] l = new File(this.directoryDataSource.path).list(new FilenameFilter() {
			@Override
			public boolean accept(File _, String name) {
				return name.matches(DirectorySelector.this.directoryDataSource.filter);
			}
		});
		Arrays.sort(l, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		if (l != null)
			selector.setListData(l);
		selector.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (element != null)
					element.setValue(selector.getSelectedValue().toString());
			}
		});
		scroll = new JScrollPane(selector);
	}

	@Override
	public JComponent getComponent() {
		return scroll;
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
	public void notify(Element event) {
		selector.setSelectedValue(event.getValue(), true);
	}
	
	public static class Factory implements FactoryRegistry.Factory<DataSource, ElementSelector> {
		@Override
		public ElementSelector instantiate(DataSource d) {
			return new DirectorySelector((DirectoryDataSource) d);
		}
	}
}