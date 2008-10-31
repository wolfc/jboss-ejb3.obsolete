package org.jboss.ejb3.test.service;

import javax.annotation.Resource;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.jboss.logging.Logger;

/**
 * Test EJBTHREE-587
 * 
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Service(objectName = "jboss.ejb3.bugs:service=TestResourceInjectionService")
@Management(TestResourceInjectionServiceIF.class)
@Depends("jboss.mq.destination:name=testTopic,service=Topic")
public class TestResourceInjectionService implements TestResourceInjectionServiceIF {

	private static Logger log = Logger.getLogger(TestResourceInjectionService.class);
	
	public boolean testedSuccessful = false;

	@Resource(mappedName = "topic/testTopic")
	private Topic testTopic;
	
	@Resource(mappedName = "ConnectionFactory")
	private TopicConnectionFactory topicConnectionFactory;
	
	public boolean getTestedSuccessful() {
		return testedSuccessful;
	}

    public boolean getTestedSuccessfulNow() {
       boolean success = true;
       if(testTopic == null)
       {
           log.warn("Dependent resource injection 'testTopic' failed");
           success = false;
       }
       
       if(topicConnectionFactory == null)
       {
           log.warn("Dependent resource injection 'topicConnectionFactory' failed");
           success = false;
       }
       return success;
    }
   
// - Service life cycle --------------------------------------------------------
	
	public void create() throws Exception {
		log.info("TestResourceInjectionService.create()");
        // EJBTHREE-655: resource injection isn't done yet
	}
	
	public void start() throws Exception {
		log.info("TestResourceInjectionService.start()");
        testedSuccessful = true;
		if(testTopic == null)
        {
			log.warn("Dependent resource injection 'testTopic' failed");
            testedSuccessful = false;
        }
        
		if(topicConnectionFactory == null)
        {
			log.warn("Dependent resource injection 'topicConnectionFactory' failed");
            testedSuccessful = false;
        }
	}
	
	public void stop() {
		log.info("TestResourceInjectionService.stop()");
	}
	
	public void destroy() {
		log.info("TestResourceInjectionService.destroy()");
	}	
	
}
