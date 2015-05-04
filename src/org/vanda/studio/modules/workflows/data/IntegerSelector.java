package org.vanda.studio.modules.workflows.data;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.vanda.datasources.DataSource;
import org.vanda.util.FactoryRegistry;
import org.vanda.util.Observer;

public class IntegerSelector implements ElementSelector, Observer<Element> {

	private Element element;
	private JSpinner jNumber;

	public IntegerSelector() {
		jNumber = new JSpinner(new SpinnerNumberModel());
		// disable digit grouping
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(jNumber);
		editor.getFormat().setGroupingUsed(false);
		jNumber.setEditor(editor);
		jNumber.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		jNumber.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (element != null)
					element.setValue(jNumber.getModel().getValue().toString());
			}
		});
		jNumber.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				if (element != null)
					element.setValue(jNumber.getModel().getValue().toString());
			}
		});
	}

	@Override
	public JComponent getComponent() {
		final JPanel pan = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		pan.add(jNumber, gbc);
		pan.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				pan.requestFocusInWindow();
			}
			
		});

		return pan;
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
		try {
			jNumber.getModel().setValue(Integer.parseInt(e.getValue()));
		} catch (NumberFormatException nfe) {
			jNumber.getModel().setValue(0);
		}
	}
	
	public static class Factory implements FactoryRegistry.Factory<DataSource, ElementSelector> {
		@Override
		public ElementSelector instantiate(DataSource d) {
			return new IntegerSelector();
		}
	}

}