package org.vanda.runner;

import java.io.IOException;

import org.vanda.fragment.impl.GeneratorImpl;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.Profile;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.data.Databases.CursorChange;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.Workflows.UpdatedEvent;
import org.vanda.workflows.run.Runner;
import org.vanda.workflows.run.RunnerFactory;

public class RunnerFactoryImpl implements RunnerFactory {
	
	private final Profile prof;

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContact() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void clean(MutableWorkflow wf, Database db) {
		// TODO implement
	}

	@Override
	public Runner createRunner(MutableWorkflow wf, Database db) {
		Generator gen = new GeneratorImpl(prof);
		SyntaxAnalysis synA = new SyntaxAnalysis();
		SemanticAnalysis semA = new SemanticAnalysis();
		synA.notify(new UpdatedEvent<MutableWorkflow>(wf));
		semA.notify(new CursorChange<Database>(db));
		semA.notify(synA);
		String id;
		try {
			id = gen.generate(wf, synA, semA);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new RunnerImpl(id);
	}
	
	public RunnerFactoryImpl(Profile prof) {
		this.prof = prof;
	}
	
}