package org.vanda.studio.modules.workflows.tools.semantic;

import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public interface SemanticsToolFactory {
	Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA);
}
