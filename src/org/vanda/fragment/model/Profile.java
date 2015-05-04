package org.vanda.fragment.model;

import org.vanda.types.Type;
import org.vanda.util.MetaRepository;
import org.vanda.util.RepositoryItem;

public interface Profile extends RepositoryItem {
	
	FragmentCompiler getCompiler(Type t);
	
	MetaRepository<String, FragmentCompiler> getFragmentCompilerMetaRepository();
	
	MetaRepository<String, FragmentLinker> getFragmentLinkerMetaRepository();
	
	MetaRepository<String, Fragment> getFragmentToolMetaRepository();
	
	FragmentLinker getLinker(String id);
	
	FragmentLinker getRootLinker(Type t);
	
	Type getRootType();
	
}
