package org.jboss.ejb3.stateful;

import javax.ejb.EJBObject;
import javax.ejb.RemoteHome;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.remoting.InvokerLocator;

public abstract class BaseStatefulRemoteProxyFactory extends BaseStatefulProxyFactory implements RemoteProxyFactory
{
   
   // Instance Members
   
   private RemoteBinding binding;
   
   private InvokerLocator locator;
   
   // Constructor
   public BaseStatefulRemoteProxyFactory(SessionContainer container, RemoteBinding binding)
   {
      super(container, binding.jndiBinding());
      
      this.binding = binding;
      
      try
      {
         String clientBindUrl = ProxyFactoryHelper.getClientBindUrl(this.getBinding());
         this.locator = new InvokerLocator(clientBindUrl);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   // Required Implementations

   @Override
   protected ProxyAccessType getProxyAccessType()
   {
      return ProxyAccessType.REMOTE;
   }

   @Override
   protected void validateEjb21Views()
   {
      // Obtain Container
      SessionContainer container = this.getContainer();
      
      // Obtain @RemoteHome
      RemoteHome remoteHome = container.getAnnotation(RemoteHome.class);

      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.validateEjb21Views(remoteHome == null ? null : remoteHome.value(), ProxyFactoryHelper
            .getRemoteInterfaces(container));
   }
   
   public Object createProxy()
   {
      Object id = getContainer().createSession();
      return this.createProxy(id);
   }
   
   public Object createProxy(Object id)
   {
      return this.createProxy(id,SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   // Specifications
   
   abstract String getStackNameInterceptors();
   
   // Functional Methods 
   
   Object createProxy(Object id,SpecificationInterfaceType type)
   {
      String stackName = this.getStackNameInterceptors();
      RemoteBinding binding = this.getBinding();
      if (binding.interceptorStack() != null && !binding.interceptorStack().trim().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      if (stack == null) throw new RuntimeException("unable to find interceptor stack: " + stackName);
      StatefulRemoteProxy proxy = new StatefulRemoteProxy(getContainer(), stack.createInterceptors(getContainer()
            .getAdvisor(), null), this.getLocator(), id);
      
      if(type.equals(SpecificationInterfaceType.EJB21))
      {
         return this.constructEjb21Proxy(proxy);
      }
      else
      {
         return this.constructBusinessProxy(proxy);
      }
   }
   
   @Override
   protected StatefulRemoteHandleImpl createHandle()
   {
      Object proxy = this.createProxyEjb21();
      return this.createHandle(proxy);
   }
   
   protected StatefulRemoteHandleImpl createHandle(Object proxy)
   {
      StatefulRemoteHandleImpl handle = new StatefulRemoteHandleImpl((EJBObject)proxy);
      return handle;
   } 
   
   public Object createProxyEjb21()
   {
      Object id = getContainer().createSession();
      return this.createProxyEjb21(id);
   }
   
   public Object createProxyEjb21(Object id)
   {
      return this.createProxy(id,SpecificationInterfaceType.EJB21);
   }

   // Accessors / Mutators
   
   RemoteBinding getBinding()
   {
      assert this.binding!=null : "RemoteBinding has not been initialized";
      return this.binding;
   }
   
   InvokerLocator getLocator()
   {
      assert this.locator!=null : "InvokerLocator has not been initialized"; 
      return this.locator;
   }

}
