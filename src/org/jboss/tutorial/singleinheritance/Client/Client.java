/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.singleinheritance.Client;


import org.jboss.tutorial.tableperinheritance.bean.Pet;
import org.jboss.tutorial.tableperinheritance.bean.PetDAO;

import javax.naming.InitialContext;

import java.util.List;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      PetDAO dao = (PetDAO) ctx.lookup(PetDAO.class.getName());

      dao.createCat("Toonses", 15.0, 9);
      dao.createCat("Sox", 10.0, 5);
      dao.createDog("Winnie", 70.0, 5);
      dao.createDog("Junior", 11.0, 1);

      List l = dao.findByWeight(14.0);
      for (Object o : l)
      {
         System.out.println(((Pet) o).getName());
      }
   }
}
