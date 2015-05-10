package org.vanda.studio.modules.workflows.tools.semantic;

import java.util.ArrayList;
import java.util.Collection;

import org.vanda.datasources.DataSourceMount;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Repository;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.run.BuildContext;
import org.vanda.workflows.run.BuildSystem;

public class SemanticsTool implements ToolFactory {

	private final class Tool {

		private final Database database;
		private final SemanticAnalysis semA;
		private final SyntaxAnalysis synA;
		private final Collection<Object> tools;
		private final BuildContext bc;

		public Tool(WorkflowEditor wfe) {

			database = wfe.getDatabase();
			synA = wfe.getSyntaxAnalysis();
			semA = new SemanticAnalysis();
			synA.getSyntaxChangedObservable().addObserver(semA);
			database.getObservable().addObserver(semA);
			BuildSystem bs = buildSystemRepository.getItems().iterator().next();
			bc = bs.createBuildContext(dataSourceRepository);
			
			semA.notify(new Databases.CursorChange<Database>(database));
			tools = new ArrayList<Object>();
			for (SemanticsToolFactory stf : toolRepository.getItems())
				tools.add(stf.instantiate(wfe, synA, semA, bc));

		}

	}

	private final Repository<String, DataSourceMount> dataSourceRepository;
	private final Repository<String, SemanticsToolFactory> toolRepository;
	private final Repository<String, BuildSystem> buildSystemRepository;

	public SemanticsTool(Repository<String, SemanticsToolFactory> repository,
			Repository<String, BuildSystem> buildSystemRepository,
			Repository<String, DataSourceMount> dataSourceRepository) {
		this.toolRepository = repository;
		this.buildSystemRepository = buildSystemRepository;
		this.dataSourceRepository = dataSourceRepository;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe);
	}

	@Override
	public String getId() {
		return "Semantic plugin-provider editor plugin";
	}

}
