package org.vanda.workflows.hyper;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;

public interface ElementReturnVisitor<R> {

	public R visitLiteral(Literal l);
	
	public R visitTool(Tool t);

}
