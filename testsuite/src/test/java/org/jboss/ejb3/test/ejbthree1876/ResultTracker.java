/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1876;

/**
 * ResultTracker
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ResultTracker
{
   public static enum Result
   {
      SUCCESS, FAILURE;
   }

   private Result result = Result.FAILURE;

   private static ResultTracker instance;

   private Exception failureCause;

   public static synchronized ResultTracker getInstance()
   {
      if (instance == null)
      {
         instance = new ResultTracker();
      }
      return instance;
   }

   private ResultTracker()
   {

   }

   public Result getResult()
   {
      return this.result;
   }

   public void setFailed()
   {
      this.result = Result.FAILURE;
   }

   public void setPassed()
   {
      this.result = Result.SUCCESS;
      this.failureCause = null;
   }

   public void setException(Exception e)
   {
      this.result = Result.FAILURE;
      this.failureCause = e;
   }

   public Exception getFailureCause()
   {
      return this.failureCause;
   }
}
