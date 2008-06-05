package org.jboss.ejb3.session;

import java.util.Hashtable;

import org.jboss.aop.Domain;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * SessionSpecContainer
 * 
 * A SessionContainer with support for Session Beans defined 
 * specifically by the EJB3 Specification
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionSpecContainer extends SessionContainer
{
   
   // Constructor
   
   public SessionSpecContainer(ClassLoader cl, String beanClassName, String ejbName, Domain domain,
         Hashtable ctxProperties, Ejb3Deployment deployment, JBossSessionBeanMetaData beanMetaData)
         throws ClassNotFoundException
   {
      super(cl, beanClassName, ejbName, domain, ctxProperties, deployment, beanMetaData);
   }
   
   /**
    * Create a remote proxy (EJBObject) for an enterprise bean identified by id
    * 
    * @param id
    * @return
    * @throws Exception
    */
   public Object createProxyRemoteEjb21(String businessInterfaceType) throws Exception
   {
      RemoteBinding binding = this.getRemoteBinding();
      return this.createProxyRemoteEjb21(binding, businessInterfaceType);
   }

   /**
    * Create a remote proxy (EJBObject) for an enterprise bean identified by id on a given binding
    * 
    * @param id
    * @param binding
    * @return
    * @throws Exception
    */
   public abstract Object createProxyRemoteEjb21(RemoteBinding binding, String businessInterfaceType) throws Exception;

   /**
    * Create a local proxy (EJBLocalObject) for an enterprise bean identified by id
    * 
    * @param id
    * @return
    * @throws Exception
    */
   public Object createProxyLocalEjb21(String businessInterfaceType) throws Exception
   {
      LocalBinding binding = this.getAnnotation(LocalBinding.class);
      return this.createProxyLocalEjb21(binding, businessInterfaceType);
   }

   /**
    * Create a local proxy (EJBLocalObject) for an enterprise bean identified by id, with
    * the specified LocalBinding
    * 
    * @param id
    * @return
    * @throws Exception
    */
   public abstract Object createProxyLocalEjb21(LocalBinding binding, String businessInterfaceType) throws Exception;
}
