package org.jboss.ejb3.test.proxy.lang;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.ejb3.proxy.lang.SerializableMethod;
import org.jboss.ejb3.test.proxy.lang.unit.SerializableMethodTestCase;

/**
 * 
 * MyClass - Helper class for getting various {@link Method}s for testing
 * {@link SerializableMethod}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 * @see {@link SerializableMethodTestCase}
 */
public class MyClass
{

   /**
    * 
    */
   public void methodWithNoParamAndReturningVoid()
   {
      //do nothing
   }

   /**
    * 
    * @param i
    */
   public void methodWithParamAndReturningVoid(Integer i)
   {
      //do nothing
   }

   /**
    * 
    * @param s
    */
   public void methodWithParamAndReturningVoid(String s)
   {
      //do nothing
   }

   /**
    * 
    * @param s
    */
   public void methodWithParamAndReturningVoid(int primitive)
   {
      //do nothing
   }

   /**
    * 
    * @param obj
    */
   public void methodWithParamAndReturningVoid(Object obj)
   {
      //do nothing
   }

   /**
    * 
    */
   public String toString()
   {
      return this.getClass().getName();
   }

   /**
    * 
    * @param a
    * @return
    */
   public int methodAcceptingArrayOfPrimitives(int[] a)
   {
      return a[0];
   }

   /**
    * 
    * @param a
    * @return
    */
   public Object methodAcceptingArrayOfObjects(Object[] objs)
   {
      return objs[0];
   }

   /**
    * 
    * @param list
    * @return
    */
   public Class<?> methodWithGenerics(List<?> list, int i)
   {
      return null;
   }

   /**
    * 
    * @param i
    * @return
    */
   public Integer methodReturingInteger(Integer i)
   {
      return null;
   }

   /**
    * 
    * @param m
    * @return
    */
   public MyClass methodAcceptingMyClass(MyClass m)
   {
      return m;
   }

}
