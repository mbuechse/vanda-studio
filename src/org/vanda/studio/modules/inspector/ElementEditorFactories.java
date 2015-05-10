package org.vanda.studio.modules.inspector;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public class ElementEditorFactories {
	
	public final CompositeElementEditorFactory<ConnectionKey> connectionFactories;
	
	public final CompositeElementEditorFactory<Literal> literalFactories;
	
	public final CompositeElementEditorFactory<Tool> toolFactories;
	
	public final CompositeElementEditorFactory<Location> variableFactories;
	
	public final CompositeElementEditorFactory<MutableWorkflow> workflowFactories;
	
	{
		connectionFactories = new CompositeElementEditorFactory<ConnectionKey>();
		literalFactories = new CompositeElementEditorFactory<Literal>();
		toolFactories = new CompositeElementEditorFactory<Tool>();
		variableFactories = new CompositeElementEditorFactory<Location>();
		workflowFactories = new CompositeElementEditorFactory<MutableWorkflow>();
	}

}
