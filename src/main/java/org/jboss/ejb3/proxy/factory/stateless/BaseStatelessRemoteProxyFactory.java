package org.jboss.ejb3.proxy.factory.stateless;

import javax.ejb.EJBObject;
import javax.ejb.RemoteHome;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.AdviceStack;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactory;
import org.jboss.ejb3.proxy.handler.stateless.StatelessRemoteProxyInvocationHandler;
import org.jboss.ejb3.session.ProxyAccessType;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.ejb3.stateless.StatelessHandleRemoteImpl;
import org.jboss.remoting.InvokerLocator;

public abstract class BaseStatelessRemoteProxyFactory extends BaseStatelessProxyFactory implements RemoteProxyFactory
{
   
   // Instance Members
   
   private RemoteBinding binding;
   
   private InvokerLocator locator;
   
   // Constructor
   public BaseStatelessRemoteProxyFactory(SessionSpecContainer container, RemoteBinding binding)
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
   protected final ProxyAccessType getProxyAccessType()
   {
      return ProxyAccessType.REMOTE;
   }  
   
   @Override
   protected final void validateEjb21Views()
   {
      // Obtain Container
      SessionContainer container = this.getContainer();
      
      // Obtain @RemoteHome
      RemoteHome remoteHome = container.getAnnotation(RemoteHome.class);

      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.validateEjb21Views(remoteHome == null ? null : remoteHome.value(), ProxyFactoryHelper
            .getRemoteInterfaces(container));
   }
   
   // Specifications
   
   abstract String getStackNameInterceptors();
   
   // Functional Methods 
   
   protected boolean bindHomeAndEjb21ViewTogether(SessionContainer container)
   {
      String homeJndiName = ProxyFactoryHelper.getHomeJndiName(container);
      String remoteBusinessJndiName = ProxyFactoryHelper.getRemoteBusinessJndiName(container);
      return homeJndiName.equals(remoteBusinessJndiName);
   }
   
   public Object createProxyBusiness()
   {
      return this.createProxyBusiness(null);
   }
   
   public Object createProxyBusiness(String businessInterfaceType)
   {
      return this.createProxy(SpecificationInterfaceType.EJB30_BUSINESS, businessInterfaceType);
   }
   
   public Object createProxy(SpecificationInterfaceType type, String businessInterfaceType)
   {
      String stackName = this.getStackNameInterceptors();
      if (binding.interceptorStack() != null && !binding.interceptorStack().equals(""))
      {
         stackName = binding.interceptorStack();
      }
      AdviceStack stack = AspectManager.instance().getAdviceStack(stackName);
      StatelessRemoteProxyInvocationHandler proxy = new StatelessRemoteProxyInvocationHandler(getContainer(),
            stack.createInterceptors(getContainer().getAdvisor(), null), locator, businessInterfaceType);
      
      if(type.equals(SpecificationInterfaceType.EJB21))
      {
         return this.constructEjb21Proxy(proxy);
      }
      else
      {
         return this.constructProxyBusiness(proxy);
      }
   }
   
   @Override
   public final StatelessHandleRemoteImpl createHandle()
   {
      EJBObject proxy = this.createProxyEjb21(null);
      StatelessHandleRemoteImpl handle = new StatelessHandleRemoteImpl(proxy);
      return handle;
   }
   
   @SuppressWarnings("unchecked")
   public <T extends EJBObject> T createProxyEjb21(String businessInterfaceType)
   {
      // Cast explicitly to catch improper proxies
      return (T) this.createProxy(SpecificationInterfaceType.EJB21, businessInterfaceType);
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
