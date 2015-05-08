package org.vanda.studio.modules.workflows.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.util.Factory;
import org.vanda.util.Observer;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Elements.ElementEvent;
import org.vanda.workflows.elements.Elements.ElementListener;

public class LiteralEditorPanel extends JPanel {
	
	private static final long serialVersionUID = -8903445266661640539L;
	private final RootDataSource rds;
	private final ElementObserver elemObs;
	private final LiteralObserver litObs;
	private final DatabaseObserver dbObs;
	private final ElementSelector selector;

	public class ElementObserver implements Observer<Element> {
		final Database d;
		final Literal l;

		public ElementObserver(Database d, Literal l) {
			this.d = d;
			this.l = l;
		}

		@Override
		public void notify(Element e) {
			// event.doNotify(this);
			String s = e.getValue();
			if (!s.equals(d.get(l.getKey()))) {
				l.setType(rds.getType(e.getValue()));
				d.put(l.getKey(), s);
			}
		}
	}

	public class LiteralObserver implements Observer<ElementEvent<Literal>>, ElementListener<Literal> {
		final JTextField value;

		public LiteralObserver(JTextField value) {
			this.value = value;
		}

		@Override
		public void notify(ElementEvent<Literal> e) {
			e.doNotify(this);
		}

		@Override
		public void typeChanged(Literal l) {
			// do nothing
		}

		@Override
		public void valueChanged(Literal l) {
			value.setText(l.getName());
		}
	}

	public class DatabaseObserver implements Observer<DatabaseEvent<Database>>, DatabaseListener<Database> {

		final Literal l;
		final Element e;

		public DatabaseObserver(Literal l, Element e) {
			this.l = l;
			this.e = e;
		}

		@Override
		public void notify(DatabaseEvent<Database> event) {
			event.doNotify(this);

		}

		@Override
		public void cursorChange(Database d) {
			String s = d.get(l.getKey());
			if (s != null && !("").equals(s)) {
				e.beginUpdate();
				try {
					e.setValue(d.get(l.getKey()));
				} finally {
					e.endUpdate();
				}
			}
		}

		@Override
		public void dataChange(Database d, Integer key) {
			if (key.equals(l.getKey())) {
				e.beginUpdate();
				try {
					e.setValue(d.get(key));
				} finally {
					e.endUpdate();
				}
			}
		}

		@Override
		public void nameChange(Database d) {
		}

	}

	public LiteralEditorPanel(Database d, final Literal l, RootDataSource rds, Factory<DataSource, ElementSelector> fr) {
		super(new BorderLayout());
		this.rds = rds;
		JLabel label1 = new JLabel("Name");
		final JTextField value = new JTextField(l.getName());
		value.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.setName(value.getText());
			}
		});

		final Element e = new Element(d.get(l.getKey()));
		elemObs = new ElementObserver(d, l);
		e.getObservable().addObserver(elemObs);
		litObs = new LiteralObserver(value);
		l.getObservable().addObserver(litObs);
		dbObs = new DatabaseObserver(l, e);
		d.getObservable().addObserver(dbObs);
		selector = new RootElementSelector(rds, fr);
		selector.setElement(e);

		JPanel editor = new JPanel();
		GroupLayout layout = new GroupLayout(editor);
		editor.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(label1))
				.addGroup(layout.createParallelGroup().addComponent(value)));

		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label1).addComponent(value)));

		this.add(editor, BorderLayout.NORTH);
		this.add(selector.getComponent(), BorderLayout.CENTER);
	}
}