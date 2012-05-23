package org.vanda.studio.modules.profile.model;

import java.util.ArrayList;

import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.model.types.Type;

public interface FragmentCompiler extends RepositoryItem {

	Fragment compile(String name, ArrayList<JobInfo> jobs,
			ArrayList<String> fragments);

	Type getFragmentType();
	
}