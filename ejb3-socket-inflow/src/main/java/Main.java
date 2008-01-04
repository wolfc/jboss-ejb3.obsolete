import org.jboss.ejb3.resource.adaptor.socket.NonBlockingSocketServer;
import org.jboss.ejb3.resource.adaptor.socket.handler.http.CopyHttpRequestToResponseRequestHandler;

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
      NonBlockingSocketServer server = new NonBlockingSocketServer(new CopyHttpRequestToResponseRequestHandler());
      server.start();

      while (true)
      {

      }

   }

}
