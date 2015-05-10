package org.vanda.studio.modules.workflows.tools.semantic;

import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.HasId;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.run.BuildContext;

public interface SemanticsToolFactory extends HasId {
	Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, BuildContext bc);
}
