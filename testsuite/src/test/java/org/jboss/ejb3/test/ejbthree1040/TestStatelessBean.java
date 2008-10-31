package org.jboss.ejb3.test.ejbthree1040;

import java.io.Serializable;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

@Stateless
@Remote(TestStateless1040Remote.class)
@RemoteBinding(jndiBinding = TestStatelessBean.JNDI_BINDING_REMOTE)
public class TestStatelessBean implements TestStateless1040Remote, Serializable
{

   // Class Members
   private static final long serialVersionUID = -5772694054091690784L;

   /**
    * Expose the JNDI Binding for this Bean
    */
   public static final String JNDI_BINDING_REMOTE = "TestStatelessBean/remote";

}
