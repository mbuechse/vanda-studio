package org.vanda.studio.modules.inspector;

import javax.swing.JComponent;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.swing.data.ElementSelector;
import org.vanda.swing.data.LiteralEditorPanel;
import org.vanda.util.Factory;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.MutableWorkflow;

public class LiteralEditor implements ElementEditorFactory<Literal> {

	private final RootDataSource rds;
	private final Factory<DataSource, ElementSelector> fr;

	public LiteralEditor(RootDataSource rds, Factory<DataSource, ElementSelector> fr) {
		this.rds = rds;
		this.fr = fr;
	}

	@Override
	public JComponent createEditor(Database d, MutableWorkflow wf, final Literal l) {
		return new LiteralEditorPanel(d, l, rds, fr);
	}

}
