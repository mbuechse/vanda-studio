package org.vanda.swing.data;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import org.vanda.datasources.DataSource;
import org.vanda.util.Factory;
import org.vanda.util.Observer;

public class DoubleSelector implements ElementSelector, Observer<Element> {

	private Element element;
	private JFormattedTextField jNumber;

	public DoubleSelector() {
		jNumber = new JFormattedTextField(NumberFormat.getNumberInstance());
		final InputVerifier iv = new InputVerifier() {
			
			@Override
			public boolean verify(JComponent arg0) {
				try { 
					Double.parseDouble(jNumber.getText());
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		};
		jNumber.setInputVerifier(iv);
		jNumber.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		jNumber.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (element != null && iv.verify(null))
					element.setValue(jNumber.getText());
			}
		});
		jNumber.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if (element != null && iv.verify(null))
					element.setValue(jNumber.getText());
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
	public void notify(Element event) {
		jNumber.setText(event.getValue());
	}
	
	public static class Fäctory implements Factory<DataSource, ElementSelector> {
		@Override
		public ElementSelector instantiate(DataSource d) {
			return new DoubleSelector();
		}
	}

}