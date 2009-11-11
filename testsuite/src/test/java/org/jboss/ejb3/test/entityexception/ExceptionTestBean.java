/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.test.entityexception;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Stateless
@Remote(ExceptionTest.class)
public class ExceptionTestBean implements ExceptionTest
{
   @PersistenceContext
   EntityManager manager;

   
   public Person createEntry(Person person)
   {
      manager.persist(person);
      return person;
   }
   
   public Person removeEntry(Person person)
   {
      manager.remove(person);
      return person;
   }
   
   @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)   
   public void testTransactionRequiredException()
   {
      System.out.println("*** testEMPersistTransactionRequiredException");
      Person person = new Person(100, "No Tx");
      
      try
      {
         manager.persist(person);
         throw new RuntimeException("TransactionRequiredException not thrown for persist()");
      }
      catch(TransactionRequiredException e)
      {
      }
      
      try
      {
         manager.merge(person);
         throw new RuntimeException("TransactionRequiredException not thrown for merge()");
      }
      catch(TransactionRequiredException e)
      {
      }
      
      try
      {
         manager.remove(person);
         throw new RuntimeException("TransactionRequiredException not thrown for remove()");
      }
      catch(TransactionRequiredException e)
      {
      }

      try
      {
         manager.refresh(person);
         throw new RuntimeException("TransactionRequiredException not thrown for refresh()");
      }
      catch(TransactionRequiredException e)
      {
      }

      /*
      try
      {
         manager.contains(person);
         throw new RuntimeException("TransactionRequiredException not thrown for contains()");
      }
      catch(TransactionRequiredException e)
      {
      }
      */
      
      try
      {
         manager.flush();
         throw new RuntimeException("TransactionRequiredException not thrown for flush()");
      }
      catch(TransactionRequiredException e)
      {
      }
      
      
   }
   
   public void testEMPersistExceptions()
   {
      System.out.println("*** testEMPersistIllegalArgumentExceptions");
      NonEntity nonEntity = new NonEntity();
      try
      {
         manager.persist(nonEntity);
         throw new RuntimeException("IllegalArgumentException not thrown when saving non-entity");
      }
      catch(IllegalArgumentException e)
      {
      }

      //TODO - IllegalArgumentException should be thrown if entity is in detached state
   }
   
   public void testEMMergeExceptions()
   {
      System.out.println("*** testEMMergeIllegalArgumentException");
      try
      {
         NonEntity nonEntity = new NonEntity();
         manager.merge(nonEntity);
         throw new RuntimeException("IllegalArgumentException not thrown when merging non-entity");
      }
      catch(IllegalArgumentException e)
      {
      }
      
      //TODO - IllegalArgumentException should be thrown if entity is in removed state
   }

   public void testEMRemoveIllegalArgumentException()
   {
      System.out.println("*** testEMRemoveIllegalArgumentException");
      try
      {
         NonEntity nonEntity = new NonEntity();
         manager.remove(nonEntity);
         throw new RuntimeException("IllegalArgumentException not thrown when merging non-entity");
      }
      catch(IllegalArgumentException e)
      {
      }
      
      //TODO - IllegalArgumentException should be thrown if entity is in detached or removed state
   }
   
   public boolean testEMFindExceptions()
   {
      System.out.println("*** testEMFindExceptions");
      manager.find(Person.class, 1);
      try
      {

         Person person = manager.getReference(Person.class, 2);
         person.getId();
         throw new RuntimeException("EntityNotFoundException not thrown: " + person);
      }
      catch(EntityNotFoundException e) 
      {
      }
      
      try
      {
         manager.find(NonEntity.class, 1);
         throw new RuntimeException("IllegalArgumentException not thrown");
      }
      catch(IllegalArgumentException e)
      {
      }

      try
      {
         manager.find(Person.class, "abc");
         throw new RuntimeException("IllegalArgumentException not thrown");
      }
      catch(IllegalArgumentException e)
      {
      }


      return true;
   } 
   
   public void testEMCreateQueryExceptions()
   {
      try
      {
         manager.createQuery("This is all nonsense");
         //TODO, according to spec invalid EJBQL should throw IllegalArgumentExceptions
         //throw new RuntimeException("IllegalArgumentException not thrown");
      }
      catch(IllegalArgumentException e)
      {
      }
   }
   
   public void testEMRefreshExceptions()
   {
      try
      {
         manager.refresh(new NonEntity());
         throw new RuntimeException("IllegalArgumentException not thrown when refreshing a non-entity");
      }
      catch(IllegalArgumentException e)
      {
      }

      //TODO - IllegalArgumentException should be thrown if argument is not in managed state
   }
 
   public void testEMContainsExceptions()
   {
      try
      {
         manager.contains(new NonEntity());
         //TODO - IllegalArgumentException should be thrown 
         //throw new RuntimeException("IllegalArgumentException not thrown for contains a non-entity");
      }
      catch(IllegalArgumentException e)
      {
      }
   }
   
   public void testQuerySingleResultExceptions()
   {
      createEntry(new Person(11, "A"));
      createEntry(new Person(12, "B"));
      createEntry(new Person(13, "C"));
      
      Query query = manager.createQuery("from Person");
      try
      {
         System.out.println("Before query");
         query.getSingleResult();
         System.out.println("After query");
         throw new RuntimeException("NonUniqueResultException not thrown for getSingleResult");
      }
      catch(NonUniqueResultException e)
      {

         System.out.println("Actual exception");
         e.printStackTrace( );
      }

      System.out.println("Second query");
      Query query2 = manager.createQuery("from Person where id=999");
      try
      {
         query2.getSingleResult();
         throw new RuntimeException("NoResultException not thrown for getSingleResult returning no results");
      }
      catch(NoResultException e)
      {
      }
   }
   
   public void testQueryNonEntity()
   {
      try
      {
         manager.createQuery("from NonEntity");
         throw new RuntimeException("Expected IllegalArgumentException");
      }
      catch(IllegalArgumentException e)
      {
         
      }
   }
   
   public void testQuerySetHintAndParameter()
   {
      
      try
      {
         Query query = manager.createQuery("from Person");
         query.setHint("org.hibernate.timeout", "Not an integer");
         throw new RuntimeException("IllegalArgumentException not thrown for setHint");
      }
      catch(IllegalArgumentException e)
      {
      }
      
      Query query = manager.createQuery("from Person where id=:id and name=:name");
      try
      {
         query.setParameter("nosuchparam", "Whateverrrr");
         throw new RuntimeException("IllegalArgumentException not thrown for setParameter (Wrong name)");
      }
      catch(IllegalArgumentException e)
      {
      }
      
      query.setParameter("name", "Kabir");
      try
      {
         query.setParameter("id", "HELLO");
         //Acceptable not to throw an exception when using wrong type (query will fail)
      }
      catch(IllegalArgumentException e)
      {
      }
      
      query = manager.createQuery("from Person where id=?1 and name=?2");
      query.setParameter(1, 1);
      query.setParameter(2, "XXX");
      try
      {
         query.setParameter(-1, "HELLO");
         throw new RuntimeException("IllegalArgumentException not thrown for setParameter (Wrong index)");
      }
      catch(IllegalArgumentException e)
      {
         // spec
      }
      catch(IndexOutOfBoundsException e)
      {
         // current impl
      }

      try
      {
         query.setParameter(10, "HELLO");
         throw new RuntimeException("IllegalArgumentException not thrown for setParameter (Wrong index)");
      }
      catch(IllegalArgumentException e)
      {
         // spec
      }
      catch(IndexOutOfBoundsException e)
      {
         // current impl
      }
   }

}
