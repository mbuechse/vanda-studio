/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;
import java.util.Set;

import org.vanda.datasources.DataSourceMount;
import org.vanda.datasources.RootDataSource;
import org.vanda.types.Type;
import org.vanda.util.Message;
import org.vanda.util.MetaRepository;
import org.vanda.util.Observable;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.run.BuildSystem;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface Application {

	String createUniqueId();
	
	PreviewFactory getPreviewFactory(Type type);

	Set<Type> getTypes();
	
	/**
	 * Returns the repository of tool interface repositories. Modules should
	 * add or remove their own repositories here.
	 */
	MetaRepository<String, Tool> getToolMetaRepository();
	
	/**
	 * Returns the repository of DataSource repositories. Modules should
	 * add or remove their own repositories here.
	 */
	MetaRepository<String, DataSourceMount> getDataSourceMetaRepository();
	
	/**
	 * Returns the repository of Runner repositories. Modules should
	 * add or remove their own repositories here.
	 */
	MetaRepository<String, BuildSystem> getRunnerFactoryMetaRepository();
	
	RootDataSource getRootDataSource();
	
	Observable<Message> getMessageObservable();

	/**
	 */
	Observable<Application> getShutdownObservable();

	/**
	 */
	Observable<Application> getUIModeObservable();

	/**
	 */
	UIMode getUIMode();
	
	/**
	 */
	String getProperty(String key);
	
	/**
	 */
	void setProperty(String key, String value);

	/**
	 */
	Collection<UIMode> getUIModes();

	/**
	 */
	WindowSystem getWindowSystem();
	
	/**
	 * if type is null, pf will be regarded as fallback
	 * 
	 * @param type
	 * @param pf
	 */
	void registerPreviewFactory(Type type, PreviewFactory pf);
	
	/**
	 */
	void sendMessage(Message m);

	/**
	 */
	void setUIMode(UIMode m);

	/**
	 * Quit the application.
	 */
	void shutdown();

	/**
	 * Return file name for a given resource name.
	 * 
	 * @param value
	 * @return
	 */
	String findFile(String value);

}
