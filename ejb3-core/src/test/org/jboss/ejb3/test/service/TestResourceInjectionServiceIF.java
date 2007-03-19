package org.jboss.ejb3.test.service;

import org.jboss.annotation.ejb.Management;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Management
public interface TestResourceInjectionServiceIF {

	public boolean getTestedSuccessful();
	
    public boolean getTestedSuccessfulNow();
    
// - Service life cycle --------------------------------------------------------	
	
	public void create() throws Exception;
	
	public void start() throws Exception;
	
	public void stop();
	
	public void destroy();	
	
}
