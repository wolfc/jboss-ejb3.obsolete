package org.jboss.ejb3.servicelocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JndiCachingServiceLocator
 * 
 * JNDI-based implementation of the Service Locator; will 
 * attempt to obtain services from one of a set of configured 
 * JNDI Directories (Hosts).
 * 
 * @version $Revision $
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
 */
public final class JndiCachingServiceLocator extends CachingServiceLocator
{

   // Class Members
   private static final Log logger = LogFactory.getLog(JndiCachingServiceLocator.class);

   private static JndiCachingServiceLocator instance = null;

   // Instance Members

   /**
    * List of JNDI Hosts on which Services may be bound
    */
   private List<JndiHost> jndiHosts = Collections.synchronizedList(new ArrayList<JndiHost>());

   // Constructor

   /**
    * Constructor
    */
   JndiCachingServiceLocator(List<JndiHost> jndiHosts)
   {
      super();
      this.jndiHosts = jndiHosts;
   }

   // Contracts

   /**
    * Obtains the object associated with the specified business interface 
    * from one of the configured remote hosts.
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   public <T> T getObjectFromRemoteHost(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {
      throw new RuntimeException("IMPLEMENT");
   }

}
