/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.ejb21_client_adaptors.client;

import org.jboss.tutorial.ejb21_client_adaptors.bean.Session1RemoteHome;
import org.jboss.tutorial.ejb21_client_adaptors.bean.Session1Remote;

import javax.naming.InitialContext;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      accessHomes();
      
      accessDeploymentDescriptorHomes();
   }
   
   public static void accessHomes() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Session1RemoteHome home = (Session1RemoteHome)jndiContext.lookup("Session1Remote");
      Session1Remote session1 = home.create();
      String initValue1 = session1.getInitValue();
      System.out.println("Session1 init value is " + initValue1);
      
      String initValue2 = session1.getLocalSession2InitValue();
      System.out.println("Session2 init value is " + initValue2);
   }
   
   public static void accessDeploymentDescriptorHomes() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Session1RemoteHome home = (Session1RemoteHome)jndiContext.lookup("DeploymentDescriptorSession1Remote");
      Session1Remote session1 = home.create();
      String initValue1 = session1.getInitValue();
      System.out.println("DeploymentDescriptor Session1 init value is " + initValue1);
      
      String initValue2 = session1.getLocalSession2InitValue();
      System.out.println("DeploymentDescriptor Session2 init value is " + initValue2);
   }
}
