package org.jboss.ejb3.servicelocator;


import java.io.Serializable;

/**
 * Represents a JNDI Host location on which remote and local services may be
 * deployed.
 * 
 * @author ALR
 */
public class JndiHost implements Serializable
{
   // Class Members
   private static final long serialVersionUID = 4367726854123681529L;

   // Instance Members
   private String id;

   private String address;

   private int port;

   // Constructors
   public JndiHost()
   {
   }

   public JndiHost(String name, String address, int port)
   {
      this.setId(name);
      this.setAddress(address);
      this.setPort(port);
   }

   // Accessors/Mutators
   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }

}
