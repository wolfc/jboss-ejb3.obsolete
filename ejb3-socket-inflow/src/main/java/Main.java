import org.jboss.ejb3.resource.adaptor.socket.TestNonBlockingSocketServer;

/**
 * Standalone Application to launch Server for Testing
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Main
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      TestNonBlockingSocketServer server = new TestNonBlockingSocketServer();
      server.start();

      while (true)
      {

      }

   }

}
