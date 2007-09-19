package org.jboss.ejb3.servicelocator;

/**
 * ServiceLocator
 * 
 * 
 * 
 * @version $Revision $
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
 */
public interface ServiceLocator
{

   /**
    * Obtains a stub to the the SLSB service with the specified business 
    * interface.  If this is the first request for this service, it will 
    * be obtained from JNDI and placed in a cache such that subsequent 
    * requests will not require the overhead of a JNDI lookup. 
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
   public <T> T getStatelessService(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException;

   /**
    * Obtains a stub to the the SFSB with the specified business 
    * interface.  This call will always result in a call to JNDI 
    * for a new stub; no caching will take place
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
   public <T> T getStatefulBean(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException;

   /**
    * Obtains a stub to the the JMX (MBean, Singleton) service with 
    * the specified business interface.  If this is the first 
    * request for this service, it will be obtained from JNDI and 
    * placed in a cache such that subsequent requests will not 
    * require the overhead of a JNDI lookup.  Convenience
    * method; equivalent to <code>getStatelessService</code>
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
   public <T> T getJmxService(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException;

}
