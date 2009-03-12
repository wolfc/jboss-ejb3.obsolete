/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.impl.common.ejb.slsb;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;




/**
 * 
 * SimpleSLSBean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Local (SimpleSLSBLocal.class)
public class SimpleSLSBean implements SimpleSLSBLocal
{
   
   /**
    * Instance of logger
    */
   private static Logger logger = Logger.getLogger(SimpleSLSBean.class);

   public void printObject(Object obj)
   {
      logger.info("Printing an object ---> " + obj);
    
   }

   public void printMultipleObjects(String string, int val, float f, Float objF, double d, Double objD)
   {
      // No need to do anything
      
      
   }

   public void printObject(String string)
   {
      logger.info("Printing a string ---> " + string);
      
   }

   public int someMethodWithReturnType()
   {
      // just return some value
      return -1;
   }

   public void noop()
   {
      // do nothing
      
   }

}
