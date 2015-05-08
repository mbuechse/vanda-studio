package org.vanda.studio.modules.workflows.model;

import org.vanda.util.HasId;

public interface ToolFactory extends HasId {
	Object instantiate(WorkflowEditor wfe);
}
