/**
 * 
 */
package org.vanda.studio.app;

import org.vanda.util.Factory;
import org.vanda.util.HasId;


/**
 * Resource acquisition is initialization
 * -- don't forget to implement finalize
 * 
 * @author buechse
 * 
 */
public interface Module extends HasId, Factory<Application, Object> {
}
