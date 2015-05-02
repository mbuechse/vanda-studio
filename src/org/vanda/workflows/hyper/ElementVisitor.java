package org.vanda.workflows.hyper;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;


public interface ElementVisitor {

	public void visitLiteral(Literal l);
	
	public void visitTool(Tool t);

}
