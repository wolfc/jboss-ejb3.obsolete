/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.dependent.client;

import org.jboss.tutorial.dependent.bean.Customer;
import org.jboss.tutorial.dependent.bean.CustomerDAO;

import javax.naming.InitialContext;

import java.util.List;


public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      CustomerDAO dao = (CustomerDAO) ctx.lookup(CustomerDAO.class.getName());

      System.out.println("Create Bill Burke and Monica Smith");
      dao.create("Bill", "Burke", "1 Boston Road", "Boston", "MA", "02115");
      int moId = dao.create("Monica", "Smith", "1 Boston Road", "Boston", "MA", "02115");

      System.out.println("Bill and Monica get married");
      Customer monica = dao.find(moId);
      monica.getName().setLast("Burke");
      dao.merge(monica);

      System.out.println("Get all the Burkes");
      List burkes = dao.findByLastName("Burke");
      System.out.println("There are now " + burkes.size() + " Burkes");
   }
}
