
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.GenericValidator;
import org.jboss.ejb3.servicelocator.Ejb3NotFoundException;
import org.jboss.ejb3.servicelocator.ServiceLocatorException;
import org.xml.sax.SAXException;

/**
 * The Service Context Bind Manager maintains a list of services available to
 * the client, as well as the JNP Host (and proximity of this host to the JVM)
 * asssociated with each service. Service/Context/Proximity mappings are defined
 * via external XML Configuration files (jnp_hosts.xml and
 * service_host_proximity_bindings.xml) located (by default, when launched
 * within the JBoss Application Server) in
 * ${jboss.home}/server/${jboss.server.name}/conf, or, if specified, in the
 * System Properties ninem.server.jnp-host-definitions.path &
 * ninem.server.service-host-proximity-bindings-definitions.path
 * 
 * @author ALR
 */
public class ServiceContextProximityBindManagerOld
{
   // Class Members
   private static final Log logger = LogFactory.getLog(ServiceContextProximityBindManagerOld.class);

   private static final String DEFAULT_CONFIGURATION_LOCATION_IN_CONTAINER = System
         .getProperty("jboss.server.config.url");

   private static final boolean RUN_IN_CONTAINER = !GenericValidator
         .isBlankOrNull(ServiceContextProximityBindManagerOld.DEFAULT_CONFIGURATION_LOCATION_IN_CONTAINER);

   private static final String DEFAULT_JNP_HOST_DEFINITIONS_FILE_PATH = ServiceContextProximityBindManagerOld.DEFAULT_CONFIGURATION_LOCATION_IN_CONTAINER
         + "jnp_hosts.xml";

   private static final String DEFAULT_SERVICE_HOST_PROXIMITY_BINDINGS_DEFINITIONS_FILE_PATH = ServiceContextProximityBindManagerOld.DEFAULT_CONFIGURATION_LOCATION_IN_CONTAINER
         + "service_host_proximity_bindings.xml";

   private static final String SYSTEM_PROPERTY_KEY_JNP_HOST_DEFINITIONS_FILE_PATH = "jboss.server.jnp-host-definitions.path";

   private static final String SYSTEM_PROPERTY_KEY_SERVICE_HOST_PROXIMITY_BINDINGS_DEFINITIONS_FILE_PATH = "jboss.server.service-host-proximity-bindings-definitions.path";

   private static final String NAMING_CONTEXT_FACTORY_CLASSNAME = "org.jnp.interfaces.NamingContextFactory";

   private static final String JNP_PROTOCOL = "jnp://";

   private static final String KEY_NAMING_FACTORY_URL_PACKAGES = "java.naming.factory.url.pkgs";

   private static final String VALUE_NAMING_FACTORY_URL_PACKAGES = "org.jboss.naming:org.jnp.interfaces";

   private static final String FILE_URI_PREFIX = "file:/";

   private static final Integer FILE_URI_PREFIX_LENGTH = ServiceContextProximityBindManagerOld.FILE_URI_PREFIX.length();

   private static final String WINDOWS_SPACED_FILE_INDICATOR = " ";

   private static final String DEFAULT_JNP_HOST_CANONICAL_NAME = "default";

   // Instance Members
   /**
    * Mapping of JNP Host names to the InitialContext representing the JNP Host
    */
   private Map<String, InitialContext> contexts = new HashMap<String, InitialContext>();

   /**
    * Mapping of services to JNP Host names
    */
   private Map<String, String> serviceHosts = new HashMap<String, String>();

   /**
    * Mapping of services to JNDI Locations
    */
   private Map<String, String> jndiLocations = new HashMap<String, String>();

   /**
    * Singleton instance
    */
   private static ServiceContextProximityBindManagerOld serviceContextBinding = null;

   // Constructors
   private ServiceContextProximityBindManagerOld()
   {
      super();
      this.loadServiceContextBindings();
   }

   // Singleton
   public synchronized static ServiceContextProximityBindManagerOld getInstance()
   {
      if (serviceContextBinding == null)
         serviceContextBinding = new ServiceContextProximityBindManagerOld();

      return serviceContextBinding;
   }

   // Functional Methods

   /**
    * Returns the InitialContext for the JNP Host bound to the specified
    * canonical name
    * 
    * @param hostCanocialName
    * @return
    */
   public InitialContext getJnpHost(String hostCanocialName)
   {
      InitialContext context = null;
      try
      {
         context = this.contexts.get(hostCanocialName);
      }
      catch (NullPointerException npe)
      {
         this.generateJnpHostNotBoundException(hostCanocialName);
      }
      return context;
   }

   /**
    * Returns the InitialContext for the Default JNP Host
    * 
    * @return
    */
   public InitialContext getDefaultJnpHost()
   {
      return this.getJnpHost(this.getDefaultJnpHostBindName());
   }

   public String getDefaultJnpHostBindName()
   {
      return ServiceContextProximityBindManagerOld.DEFAULT_JNP_HOST_CANONICAL_NAME;
   }

   /**
    * Returns the InitialContext for the proper JNP Host configured to be
    * contacted for the specified service name
    * 
    * @param serviceName
    * @return
    */
   public InitialContext getJnpHostForServiceName(String serviceName)
   {
      InitialContext context = null;
      try
      {
         context = this.contexts.get(this.serviceHosts.get(serviceName));
      }
      catch (NullPointerException npe)
      {
         this.generateServiceNameNotBoundException(serviceName);
      }
      return context;
   }

   /**
    * Returns the JNDI Location for the specified service name
    * 
    * @param serviceName
    * @return
    */
   public String getJndiLocation(String serviceName)
   {
      String jndiLocation = this.jndiLocations.get(serviceName);
      if (GenericValidator.isBlankOrNull(jndiLocation))
      {
         this.generateServiceNameNotBoundException(serviceName);
      }
      return jndiLocation;
   }

   /**
    * Generates a JnpHostNotBoundException with message explaining that the
    * specified JNP Host canonical name is not bound.
    * 
    * @param hostCanocialName
    */
   private void generateJnpHostNotBoundException(String hostCanocialName)
   {
      throw new ServiceLocatorException("JNP Host with canonical name \"" + hostCanocialName + "\" is not bound.");
   }

   /**
    * Generates a ServiceNameNotBoundException with message explaining that the
    * specified service name is not bound.
    * 
    * @param serviceName
    */
   private void generateServiceNameNotBoundException(String serviceName)
   {
      throw new Ejb3NotFoundException("Service name \"" + serviceName + "\" is not bound.");
   }

   /**
    * Returns all bound service names
    * 
    * @return
    */
   public Collection<String> getBoundServiceNames()
   {
      // Return all bound service names
      return this.jndiLocations.keySet();
   }

   /**
    * Loads the JNP Host definitions as well as the named service/host bindings
    * from the specified resources
    */
   @SuppressWarnings(value = "unchecked")
   private void loadServiceContextBindings()
   {
      // Initialize
      Digester jnpHostDefinitionsDigester = new Digester();
      Digester serviceBindingsDigester = new Digester();
      String jnpHostConfigurationLocation = null;
      String serviceHostProximityBindingsConfigurationLocation = null;

      // If run from container, set default configuration file paths
      if (ServiceContextProximityBindManagerOld.RUN_IN_CONTAINER)
      {
         jnpHostConfigurationLocation = ServiceContextProximityBindManagerOld.DEFAULT_JNP_HOST_DEFINITIONS_FILE_PATH;
         serviceHostProximityBindingsConfigurationLocation = ServiceContextProximityBindManagerOld.DEFAULT_SERVICE_HOST_PROXIMITY_BINDINGS_DEFINITIONS_FILE_PATH;
      }

      // If locations for configuration are overridden
      if (!GenericValidator.isBlankOrNull(System
            .getProperty(ServiceContextProximityBindManagerOld.SYSTEM_PROPERTY_KEY_JNP_HOST_DEFINITIONS_FILE_PATH)))
      {
         jnpHostConfigurationLocation = System
               .getProperty(ServiceContextProximityBindManagerOld.SYSTEM_PROPERTY_KEY_JNP_HOST_DEFINITIONS_FILE_PATH);
      }
      if (!GenericValidator
            .isBlankOrNull(System
                  .getProperty(ServiceContextProximityBindManagerOld.SYSTEM_PROPERTY_KEY_SERVICE_HOST_PROXIMITY_BINDINGS_DEFINITIONS_FILE_PATH)))
      {
         serviceHostProximityBindingsConfigurationLocation = System
               .getProperty(ServiceContextProximityBindManagerOld.SYSTEM_PROPERTY_KEY_SERVICE_HOST_PROXIMITY_BINDINGS_DEFINITIONS_FILE_PATH);
      }

      // If no configuration location is specified
      if (GenericValidator.isBlankOrNull(jnpHostConfigurationLocation))
      {
         throw new RuntimeException("JNP Host Configuration URI is not defined in system variable "
               + ServiceContextProximityBindManagerOld.SYSTEM_PROPERTY_KEY_JNP_HOST_DEFINITIONS_FILE_PATH);
      }
      if (GenericValidator.isBlankOrNull(serviceHostProximityBindingsConfigurationLocation))
      {
         throw new RuntimeException(
               "Service/Host/Proximity Binding Configuration URI is not defined in system variable "
                     + ServiceContextProximityBindManagerOld.SYSTEM_PROPERTY_KEY_SERVICE_HOST_PROXIMITY_BINDINGS_DEFINITIONS_FILE_PATH);
      }

      // Add Rules for parsing configuration
      this.addJnpHostDefinitionsParsingRules(jnpHostDefinitionsDigester);
      this.addServiceHostProximityBindingDefinitionsParsingRules(serviceBindingsDigester);

      // Ensure appropriate URI path structure
      jnpHostConfigurationLocation = jnpHostConfigurationLocation.replace(System.getProperty("file.separator")
            .charAt(0), '/');
      serviceHostProximityBindingsConfigurationLocation = serviceHostProximityBindingsConfigurationLocation.replace(
            System.getProperty("file.separator").charAt(0), '/');

      // Parse
      try
      {
         InputStream jnpHostsStream = null;
         // Check if this is a windows uri. Rule to determine right now is
         // presence of space character in string.
         if (jnpHostConfigurationLocation.contains(ServiceContextProximityBindManagerOld.WINDOWS_SPACED_FILE_INDICATOR))
         {

            // Windows URI, so transform into something we can use.
            jnpHostConfigurationLocation = jnpHostConfigurationLocation.substring(FILE_URI_PREFIX_LENGTH);

            jnpHostsStream = new FileInputStream(new File(jnpHostConfigurationLocation));
         }
         else
         {

            jnpHostsStream = new FileInputStream(new File(new URI(jnpHostConfigurationLocation)));
         }

         InputStream serviceContextBindingsStream = null;

         // Same thing here as above...
         if (serviceHostProximityBindingsConfigurationLocation
               .contains(ServiceContextProximityBindManagerOld.WINDOWS_SPACED_FILE_INDICATOR))
         {
            serviceHostProximityBindingsConfigurationLocation = serviceHostProximityBindingsConfigurationLocation
                  .substring(FILE_URI_PREFIX_LENGTH);

            serviceContextBindingsStream = new FileInputStream(new File(
                  serviceHostProximityBindingsConfigurationLocation));

         }
         else
         {
            serviceContextBindingsStream = new FileInputStream(new File(new URI(
                  serviceHostProximityBindingsConfigurationLocation)));
         }

         // Create Initial Context object from each of the
         // configured JNP Hosts
         this.createInitialContexts((Collection<JndiHost>) jnpHostDefinitionsDigester.parse(jnpHostsStream));

         // Create the default
         this.createInitialContext(new JndiHost(ServiceContextProximityBindManagerOld.DEFAULT_JNP_HOST_CANONICAL_NAME,
               null, 0));

         // Parse Service/Host/Proximity Bindings
         Collection<ServiceHostProximityBinding> serviceHostProximityBindings = (Collection<ServiceHostProximityBinding>) serviceBindingsDigester
               .parse(serviceContextBindingsStream);
         // Create Service-Context and Service-Proximity mappings
         this.bindServices(serviceHostProximityBindings);
      }
      catch (IOException ioe)
      {
         throw new RuntimeException(ioe);
      }
      catch (SAXException saxe)
      {
         throw new RuntimeException(saxe);
      }
      catch (URISyntaxException urise)
      {
         throw new RuntimeException(urise);
      }
   }

   /**
    * Adds parsing rules for reading configuration specifying JNP Hosts
    * 
    * @param digester
    */
   private void addJnpHostDefinitionsParsingRules(Digester digester)
   {
      // When the root is encountered, create a List
      // to hold the JNP Host Definitions
      digester.addObjectCreate("jnp-hosts", ArrayList.class);

      // When a new host definition is encountered,
      // create a new JNP Host
      digester.addObjectCreate("jnp-hosts/host", JndiHost.class);

      // Set all properties (in this case, "name")
      // from the "host" entry to the "JnpHost.name"
      // object
      digester.addSetProperties("jnp-hosts/host");

      // Set the address
      digester.addCallMethod("jnp-hosts/host/address", "setAddress", 1);
      digester.addCallParam("jnp-hosts/host/address", 0);

      // Set the port
      digester.addCallMethod("jnp-hosts/host/port", "setPort", 1, new Class[]
      {Integer.class});
      digester.addCallParam("jnp-hosts/host/port", 0);

      // Add the JNP Host to the List
      digester.addSetNext("jnp-hosts/host", "add");

   }

   /**
    * Adds parsing rules for reading configuration binding services to JNP
    * Hosts
    * 
    * @param digester
    */
   private void addServiceHostProximityBindingDefinitionsParsingRules(Digester digester)
   {
      // When the root is encountered, create a List
      // to hold the Service Definitions
      digester.addObjectCreate("services", ArrayList.class);

      // When a new service definition is encountered,
      // create a new URI object to hold its data
      digester.addObjectCreate("services/service", ServiceHostProximityBinding.class);

      // Set all properties (in this case, "name")
      // from the "service" entry to the "ServiceHostProximityBinding.name"
      // object
      digester.addSetProperties("services/service");

      // Set the JNP Host
      digester.addCallMethod("services/service/jnp-host", "setJnpHost", 1);
      digester.addCallParam("services/service/jnp-host", 0);

      // Set the JNDI Location
      digester.addCallMethod("services/service/jndi-location", "setJndiLocation", 1);
      digester.addCallParam("services/service/jndi-location", 0);

      // Set the proximity
      digester.addCallMethod("services/service/local-jvm", "setLocalToJvm", 1, new Class[]
      {Boolean.class});
      digester.addCallParam("services/service/local-jvm", 0);

      // Add the Service to the List
      digester.addSetNext("services/service", "add");

   }

   /**
    * Creates and stores InitialContexts for each of the specified JNP Host
    * configurations
    * 
    * @param jnpHosts
    */
   private void createInitialContexts(Collection<JndiHost> jnpHosts)
   {
      for (JndiHost jnpHost : jnpHosts)
      {
         this.createInitialContext(jnpHost);
      }
   }

   /**
    * Creates an InitialContext from the specified JNP Host and stores it for
    * future use.
    * 
    * @param jnpHost
    * @return
    */
   private void createInitialContext(JndiHost jnpHost)
   {

      // Initialize
      InitialContext context = null;

      // Assemble URI
      String jnpProviderUrl = ServiceContextProximityBindManagerOld.JNP_PROTOCOL + jnpHost.getAddress() + ":"
            + jnpHost.getPort();

      // Create properties
      Hashtable<String, String> properties = new Hashtable<String, String>();

      // If no address is specified, use all defaults (no properties)
      boolean useDefaults = false;
      if (GenericValidator.isBlankOrNull(jnpHost.getAddress()))
      {
         useDefaults = true;
      }

      // Override defaults
      if (!useDefaults)
      {
         properties.put(Context.INITIAL_CONTEXT_FACTORY,
               ServiceContextProximityBindManagerOld.NAMING_CONTEXT_FACTORY_CLASSNAME);
         properties.put(Context.PROVIDER_URL, jnpProviderUrl);
         properties.put(ServiceContextProximityBindManagerOld.KEY_NAMING_FACTORY_URL_PACKAGES,
               ServiceContextProximityBindManagerOld.VALUE_NAMING_FACTORY_URL_PACKAGES);
      }

      // Create the Initial Context
      try
      {
         if (!useDefaults)
         {
            context = new InitialContext(properties);
         }
         else
         {
            context = new InitialContext();
         }
      }
      catch (NamingException ne)
      {
         throw new RuntimeException(ne);
      }

      // Cache
      contexts.put(jnpHost.getId(), context);

      // Log
      if (!useDefaults)
      {
         logger.info("Initial Context created for JNP Host " + jnpHost.getAddress() + ":" + jnpHost.getPort()
               + " and bound to canonical name \"" + jnpHost.getId() + "\".");
      }
      else
      {
         logger.info("Default InitialContext Created and bound to canonical name \"" + jnpHost.getId() + "\".");
      }

   }

   /**
    * Binds all specified services to JNDI Locations, JNP Hosts, and
    * Proximities
    * 
    * @param serviceContextBindings
    */
   private void bindServices(Collection<ServiceHostProximityBinding> serviceContextBindings)
   {
      for (ServiceHostProximityBinding serviceContextBinding : serviceContextBindings)
      {
         this.bindService(serviceContextBinding);
      }
   }

   /**
    * Binds the specified service to the specified JNDI Location, JNP Host, and
    * Proximity
    * 
    * @param serviceContextBinding
    */
   private void bindService(ServiceHostProximityBinding serviceContextBinding)
   {
      // Ensure the service can be bound to an existant JNP Host canonical
      // name
      if (this.contexts.get(serviceContextBinding.host()) == null)
      {
         throw new JndiHostNotBoundException("Cannot bind service \"" + serviceContextBinding.getName()
               + "\" to JNP canonical name \"" + serviceContextBinding.host()
               + "\" as that JNP canonical name has not been registered.");
      }

      // Bind the Service to the JNP Host
      this.serviceHosts.put(serviceContextBinding.getName(), serviceContextBinding.host());
      logger.info("Service \"" + serviceContextBinding.getName() + "\" bound to JNP Host with canonical name \""
            + serviceContextBinding.host() + "\".");

      // Bind the Service to the JNDI Location Prefix
      this.jndiLocations.put(serviceContextBinding.getName(), serviceContextBinding.getJndiLocation());
      logger.info("Service \"" + serviceContextBinding.getName() + "\" bound to JNDI location \""
            + serviceContextBinding.getJndiLocation() + "\".");
   }
}
