
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.servicelocator.Ejb3NotFoundException;
import org.jboss.ejb3.servicelocator.ServiceLocatorException;

/**
 * ServiceLocator
 * 
 * 
 * @version $Revision $
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
 */
public class ServiceLocatorOldImpl
{

   // Class Members
   private static final Log logger = LogFactory.getLog(ServiceLocatorOldImpl.class);

   private static ServiceLocatorOldImpl serviceLocator = null;

   // Instance Members

   private Map<Class, List<Object>> jndiCache = null;

   // Constructors
   protected ServiceLocatorOldImpl()
   {
      super();
   }

   // Singleton Accessor
   public static synchronized ServiceLocatorOldImpl getInstance()
   {
      if (ServiceLocatorOldImpl.serviceLocator == null)
      {
         ServiceLocatorOldImpl.serviceLocator = new ServiceLocatorOldImpl();
      }

      return ServiceLocatorOldImpl.serviceLocator;
   }

   // Functional Methods

   /**
    * Obtains the Object stored in the specified JNDI Address at the location
    * bound to the specified JNP Host Canonical Name
    * 
    * @param jndiAddress
    * @param jnpHostCanonicalName
    * @return
    */
   public Object getObjectFromJnpHost(String jndiAddress, String jnpHostCanonicalName) throws ServiceLocatorException
   {
      // Initialize
      jnpHostCanonicalName = jnpHostCanonicalName.trim().toLowerCase();

      // Obtain InitialContext associated with JNP Host
      logger.trace("Obtaining InitialContext for JNP Host Canonical Name '" + jnpHostCanonicalName + "'");
      InitialContext context = ((InitialContext) ServiceContextProximityBindManagerOld.getInstance().getJnpHost(
            jnpHostCanonicalName));

      // Ensure the JNP Host was defined
      if (context == null)
         throw new RuntimeException("JNP Host with canonical name \"" + jnpHostCanonicalName
               + "\" is not currently configured and cannot be obtained.");

      try
      {
         // Lookup JNDI Location
         return this.getObjectFromContext(jndiAddress, context);
      }
      catch (NamingException ne)
      {
         throw new ServiceLocatorException("Error encountered in lookup of JNDI Location '" + jndiAddress
               + "' for host with canonical name '" + jnpHostCanonicalName + "'.", ne);
      }
   }

   /**
    * Obtains the Object stored in the specified JNDI Address at the specified
    * Context
    * 
    * @param jndiAddress
    * @param context
    * @return
    */
   public Object getObjectFromContext(String jndiAddress, InitialContext context) throws NamingException
   {
      // Lookup JNDI Location
      return context.lookup(jndiAddress);

   }

   /**
    * Convenience method to obtain cached, bound object by interface. Should
    * only be used to obtain cached references to Stateless EJB and JBoss JMX
    * Service Beans, as each call to this method will return the same proxy
    * object retrieved from JNDI. Stateful EJBs may NOT be looked up through
    * here, as every call to this one cached object will execute within the
    * same session.
    * 
    * @param <T>
    * @param clazz
    * @throws Ejb3NotFoundException
    *             If more than one bound class implements the specified
    *             interface
    * @throws IllegalArgumentException
    *             If no bound classes implement the specified interface
    * @return
    */
   @SuppressWarnings(value = "unchecked")
   public <T> T getBoundObjectForInterface(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException
   {

      // Ensure an interface
      if (!clazz.isInterface())
         throw new IllegalArgumentException("Specified class \"" + clazz.getName() + "\" is not an interface");

      // Ensure cache is initialized
      this.ensureCacheInitialzedWithInterface(clazz);

      // Obtain all bound classes implementing this interface
      List implementingObjects = this.jndiCache.get(clazz);

      // Ensure a bound object implements the specified interface
      if (implementingObjects == null || implementingObjects.size() == 0)
      {
         throw new Ejb3NotFoundException("No bound objects exist for specified interface : " + clazz.getName());
      }

      // Ensure there is only one bound object implementing this interface,
      // otherwise it will have to be accessed with its bound service name
      if (implementingObjects.size() > 1)
      {
         throw new IllegalArgumentException("More than one bound object implements the specified interface \""
               + clazz.getName() + "\".  Must be accessed via " + this.getClass().getName() + ".getService(String).");
      }

      return (T) implementingObjects.get(0);

   }

   /**
    * Initialized the interface/object cache if not already done
    */
   private synchronized <T> void ensureCacheInitialzedWithInterface(Class<T> clazz)
   {
      // Ensure Initialized
      if (jndiCache == null)
      {

         this.jndiCache = new HashMap<Class, List<Object>>();
      }

      // Ensure in cache
      if (this.jndiCache.get(clazz) == null)
      {

         // For each bound service name, obtain
         for (String serviceName : ServiceContextProximityBindManagerOld.getInstance().getBoundServiceNames())
         {
            // Get object
            Object obj = null;
            try
            {
               obj = this.getService(serviceName);
            }
            // Error in finding service for this configurtion; note and
            // continue
            catch (ServiceLocatorException sle)
            {
               logger.warn("Service with canonical name " + serviceName
                     + " could not be found or is not available at this time");
               continue;
            }

            // Determine interfaces
            for (Class interfaze : obj.getClass().getInterfaces())
            {

               // Bind all interfaces and their superclasses
               this.addInterfaceAndSuperclassesToBoundObjectsCache(interfaze, obj);
            }
         }
      }

   }

   /**
    * Adds the specified class and all superclasses to the cache of bound
    * objects
    * 
    * @param interfaze
    * @param obj
    */
   private void addInterfaceAndSuperclassesToBoundObjectsCache(Class interfaze, Object obj)
   {
      // Ensure exists in the mapping
      if (this.jndiCache.get(interfaze) == null)
      {
         this.jndiCache.put(interfaze, new ArrayList<Object>());
      }

      // Add the object to the list of objects implementing this
      // interface
      this.jndiCache.get(interfaze).add(obj);

      // Add all superclasses
      for (Class interfaze2 : interfaze.getInterfaces())
      {
         this.addInterfaceAndSuperclassesToBoundObjectsCache(interfaze2, obj);
      }
   }

   /**
    * Obtains the Object stored in the specified JNDI Address at the default
    * JNP Host
    * 
    * @param jndiAddress
    * @return
    */
   public Object getObjectFromDefaultJnpHost(String jndiAddress) throws ServiceLocatorException
   {
      try
      {
         return this.getObjectFromContext(jndiAddress, ServiceContextProximityBindManagerOld.getInstance()
               .getDefaultJnpHost());
      }
      catch (NamingException ne)
      {
         throw new ServiceLocatorException("Error encountered in lookup of JNDI Location '" + jndiAddress
               + "' for default JNP Host.", ne);
      }
   }

   /**
    * Obtains the stub to the service associated with the specified service
    * name
    * 
    * @param context
    * @param jndiBinding
    * @return
    */
   public Object getService(String serviceName) throws ServiceLocatorException
   {

      logger.trace("Obtaining Service '" + serviceName + "'...");
      try
      {
         return this.getObjectFromContext(ServiceContextProximityBindManagerOld.getInstance().getJndiLocation(
               serviceName), ServiceContextProximityBindManagerOld.getInstance().getJnpHostForServiceName(serviceName));
      }
      catch (NamingException ne)
      {
         throw new ServiceLocatorException("Error encountered in lookup of service '" + serviceName + "'.", ne);
      }
   }
}
