package org.vanda.runner;

import org.vanda.datasources.DataSourceMount;
import org.vanda.fragment.model.Profile;
import org.vanda.util.Repository;
import org.vanda.workflows.run.BuildSystem;

public class BuildSystemImpl implements BuildSystem {
	
	private final Profile prof;
	private final String defaultPath;

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
	public BuildContextImpl createBuildContext(Repository<String, DataSourceMount> dataSourceRepository) {
		return new BuildContextImpl(prof, defaultPath, dataSourceRepository);
	}
	
	public BuildSystemImpl(Profile prof, String defaultPath) {
		this.prof = prof;
		this.defaultPath = defaultPath;
	}
	
}