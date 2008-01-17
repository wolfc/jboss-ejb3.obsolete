package org.jboss.ejb3.resource.adaptor.socket;

/**
 * 
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class HttpRequestMessage
{
   // Instance Members

   String request;

   // Constructor

   public HttpRequestMessage(String request)
   {
      this.setRequest(request);
   }

   // Accessors / Mutators
   public String getRequest()
   {
      return request;
   }

   public void setRequest(String request)
   {
      this.request = request;
   }

}
