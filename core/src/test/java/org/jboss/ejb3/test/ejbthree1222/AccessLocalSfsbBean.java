package org.jboss.ejb3.test.ejbthree1222;

import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.RemoveException;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteBinding;

@Stateful
@Remote(AccessLocalSfsbRemoteBusiness.class)
@RemoteBinding(jndiBinding = AccessLocalSfsbRemoteBusiness.JNDI_NAME)
public class AccessLocalSfsbBean implements AccessLocalSfsbRemoteBusiness
{

   // Instance Members

   @EJB
   TestStatefulWithRemoveMethodLocalHome localHome;
   
   TestStatefulWithRemoveMethodLocal local;

   @EJB
   TestStatefulWithRemoveMethodLocalBusiness localBusiness;

   // Required Implementations

   public void resetOnLocalBusiness()
   {
      this.localBusiness.reset();
   }

   public void removeOnLocalBusiness()
   {
      this.localBusiness.remove();
   }

   public int getCallsOnLocalBusiness()
   {
      return this.localBusiness.getCalls();
   }

   public void resetOnLocal()
   {
      this.getLocal().reset();
   }

   public void removeOnLocal() throws RemoveException
   {
      this.getLocal().remove();
   }

   public int getCallsOnLocal()
   {
      return this.getLocal().getCalls();
   }
   
   private TestStatefulWithRemoveMethodLocal getLocal()
   {
      if(local==null)
      {
         try
         {
            local = localHome.create();
         }
         catch(CreateException ce)
         {
            throw new RuntimeException(ce);
         }
      }
      
      return local;
   }
}
