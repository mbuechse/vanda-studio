package org.vanda.util;

public interface RepositoryItem extends HasId {

	/**
	 * The category is used like a path in a file system. The separator is a
	 * double colon.
	 */
	String getCategory();

	String getContact();
	
	String getDescription();

	String getName();

	String getVersion();
	
}
