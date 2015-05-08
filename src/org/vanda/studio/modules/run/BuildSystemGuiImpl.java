package org.vanda.studio.modules.run;

import org.vanda.runner.BuildSystemImpl;
import org.vanda.workflows.run.BuildContext;
import org.vanda.workflows.run.BuildSystem;

class BuildSystemGuiImpl implements BuildSystem {
	
	private final BuildSystemImpl delegate;

	@Override
	public String getCategory() {
		return delegate.getCategory();
	}

	@Override
	public String getContact() {
		return delegate.getContact();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getVersion() {
		return delegate.getVersion();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public BuildContext createBuildContext() {
		return new BuildContextGuiImpl(delegate.createBuildContext());
	}
	
	public BuildSystemGuiImpl(BuildSystemImpl delegate) {
		this.delegate = delegate;
	}
	
}