/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;
import java.util.Set;

import org.vanda.types.Type;
import org.vanda.util.Message;
import org.vanda.util.Observable;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public interface Application {

	String createUniqueId();

	Set<Type> getTypes();  // TODO this must go
	
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
