package org.jboss.ejb3.test.regression.ejbthree440.unit;

import junit.framework.Test;
import org.jboss.ejb3.test.regression.ejbthree440.model.MyResource;
import org.jboss.ejb3.test.regression.ejbthree440.session.i.IInheritanceDemo;
import org.jboss.test.JBossTestCase;
import org.jnp.interfaces.NamingContextFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import java.util.Properties;
import org.jboss.serial.io.MarshalledObject;


/**
 * Demonstrates entity inheritance.
 *
 * @author Ortwin Gl�ck
 */
public class FailingTestCase extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public FailingTestCase(String name)
   {

      super(name);

   }

   public void testSerializationError() throws Exception
   {
      try
      {
         UserTransaction tx = null;
         Context ctx = getInitialContext();

         tx = (UserTransaction) ctx.lookup("UserTransaction");
         IInheritanceDemo playground = (IInheritanceDemo) ctx.lookup("InheritanceDemo/remote");

         tx.begin();
         playground.create();
         tx.commit();

         tx.begin();
         MarshalledObject mo = playground.readFromMO();
         MyResource r = (MyResource)mo.get();
         r = (MyResource) playground.read();
         playground.remove();
         tx.commit();
         tx = null;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(FailingTestCase.class, "ejbthree440.ear");
   }
}
