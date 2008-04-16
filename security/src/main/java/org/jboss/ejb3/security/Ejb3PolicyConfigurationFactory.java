/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.security;

import java.security.SecurityPermission;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;

import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @author Ron Monzillo, Gary Ellison (javadoc)
 * @version $Revision$
 */
public abstract class Ejb3PolicyConfigurationFactory
{
   private static final Logger log = Logger.getLogger(Ejb3PolicyConfigurationFactory.class);
   
   /** The standard name of the system property specifying the JACC
    PolicyConfigurationFactory implementation class name.
    */
   private static final String FACTORY_PROP =
      "javax.security.jacc.PolicyConfigurationFactory.provider";
   /** The default PolicyConfigurationFactory implementation */
   private static final String DEFAULT_FACTORY_NAME = 
      "org.jboss.security.jacc.JBossPolicyConfigurationFactory";
   /** The loaded PolicyConfigurationFactory provider */
   private static PolicyConfigurationFactory factory;

   /** This static method uses the javax.security.jacc.PolicyConfigurationFactory.provider
    * system property to create a provider factory implementation. The provider
    * class must provide a public no-arg ctor.
    * 
    * @return the PolicyConfigurationFactory singleton
    * @throws  SecurityException - when the caller does not have a
    * SecurityPermission(setPolicy) permission. 
    * @throws ClassNotFoundException - when the class named by the system
    * property could not be found or because the value of the system
    * property is null. 
    * @throws PolicyContextException - if the PolicyConfigurationFactory ctor
    * throws an exception other than those in the getPolicyConfigurationFactory
    * method signature. The exception will be encapsulated in a
    * PolicyContextException as its cause.
    */
   public static PolicyConfigurationFactory getPolicyConfigurationFactory()
      throws ClassNotFoundException, PolicyContextException
   {
      
      // Validate the caller permission
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(new SecurityPermission("setPolicy"));

      if (factory == null)
      {
         String factoryName = null;
         Class clazz = null;
         try
         {
            LoadAction action = new LoadAction();
            try
            {
               clazz = (Class) AccessController.doPrivileged(action);
               factoryName = action.getName();
            }
            catch (PrivilegedActionException ex)
            {
               ex.printStackTrace();
               factoryName = action.getName();
               Exception e = ex.getException();
               if (e instanceof ClassNotFoundException)
                  throw (ClassNotFoundException) e;
               else
                  throw new PolicyContextException("Failure during load of class: "+action.getName(), e);
            }
            
            factory = (PolicyConfigurationFactory) clazz.newInstance();
         }
         catch (ClassNotFoundException e)
         {
            String msg = "Failed to find PolicyConfigurationFactory : " + factoryName;
            throw new ClassNotFoundException(msg, e);
         }
         catch (IllegalAccessException e)
         {
            String msg = "Unable to access class : " + factoryName;
            throw new PolicyContextException(msg, e);
         }
         catch (InstantiationException e)
         {
            String msg = "Failed to create instance of: " + factoryName;
            throw new PolicyContextException(msg, e);
         }
         catch (ClassCastException e)
         {
            StringBuffer msg = new StringBuffer(factoryName + " Is not a PolicyConfigurationFactory, ");
            msg.append("PCF.class.CL: "+Ejb3PolicyConfigurationFactory.class.getClassLoader());
            msg.append("\nPCF.class.CS: "+Ejb3PolicyConfigurationFactory.class.getProtectionDomain().getCodeSource());
            msg.append("\nPCF.class.hash: "+System.identityHashCode(Ejb3PolicyConfigurationFactory.class));
            msg.append("\nclazz.CL: "+clazz.getClassLoader());
            msg.append("\nclazz.CS: "+clazz.getProtectionDomain().getCodeSource());
            msg.append("\nclazz.super.CL: "+clazz.getSuperclass().getClassLoader());
            msg.append("\nclazz.super.CS: "+clazz.getSuperclass().getProtectionDomain().getCodeSource());
            msg.append("\nclazz.super.hash: "+System.identityHashCode(clazz.getSuperclass()));
            ClassCastException cce = new ClassCastException(msg.toString());
            cce.initCause(e);
         }
      }
      return factory;
   }

   /** This method is used to obtain an instance of the provider specific class
    * that implements the PolicyConfiguration interface that corresponds to the
    * identified policy context within the provider. The methods of the
    * PolicyConfiguration interface are used to define the policy statements of
    * the identified policy context.
    * 
    * If at the time of the call, the identified policy context does not exist
    * in the provider, then the policy context will be created in the provider
    * and the Object that implements the context's PolicyConfiguration Interface
    * will be returned. If the state of the identified context is "deleted" or
    * "inService" it will be transitioned to the "open" state as a result of the
    * call. The states in the lifecycle of a policy context are defined by the
    * PolicyConfiguration interface.
    * 
    * For a given value of policy context identifier, this method must always
    * return the same instance of PolicyConfiguration and there must be at most
    * one actual instance of a PolicyConfiguration with a given policy context
    * identifier (during a process context).
    * 
    * To preserve the invariant that there be at most one PolicyConfiguration
    * object for a given policy context, it may be necessary for this method to
    * be thread safe. 
    * 
    * @param contextID - the policy context ID indicates which
    * PolicyConfiguration to return. This must not be null.
    * @param remove - A boolean flag that establishes whether or not the policy
    * statements of an existing policy context are to be removed before its
    * PolicyConfiguration object is returned. If the value passed to this
    * parameter is true, the policy statements of an existing policy context
    * will be removed. If the value is false, they will not be removed.
    * @return a PolicyConfiguration instance
    * @throws PolicyContextException
    */
   public abstract PolicyConfiguration getPolicyConfiguration(String contextID,
      boolean remove)
      throws PolicyContextException;

   /** This method determines if the identified policy context exists with state
    * "inService" in the Policy provider associated with the factory.
    * 
    * @param contextID - the context ID for selecting the policy
    * @return true if the identified policy context exists within the provider
    *    and its state is "inService", false otherwise.
    * @throws PolicyContextException
    */
   public abstract boolean inService(String contextID)
      throws PolicyContextException;

   /** A PrivilegedExceptionAction that looks up the class name identified
    * by the javax.security.jacc.PolicyConfigurationFactory.provider system
    * property and loads the class using the thread context class loader.
    */ 
   private static class LoadAction implements PrivilegedExceptionAction
   {
      private String name;
      public String getName()
      {
         return name;
      }
      public Object run()
         throws Exception
      {
         name = System.getProperty(FACTORY_PROP);
         if( name == null )
         {
            // Use the default factory impl
            name = DEFAULT_FACTORY_NAME;
         }
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class factoryClass = loader.loadClass(name);
         return factoryClass;
      }
   }
}
