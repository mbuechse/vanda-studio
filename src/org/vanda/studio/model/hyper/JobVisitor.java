package org.vanda.studio.model.hyper;

public interface JobVisitor {
	void visitAtomicJob(AtomicJob aj);
	void visitCompositeJob(CompositeJob cj);
}
