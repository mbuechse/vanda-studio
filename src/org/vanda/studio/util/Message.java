package org.vanda.studio.util;

import java.util.Date;


public interface Message extends HasActions {

	String getHeadline();
	
	String getMessage();
	
	Date getDate();
	
}