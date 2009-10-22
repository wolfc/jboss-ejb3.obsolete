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
package org.jboss.ejb3.test.ejbthree1926;

import org.jboss.ejb3.test.ejbthree1926.unit.TimerTestCase;

/**
 * ResultTracker
 * 
 * Utility class for tracking result of {@link TimerTestCase}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ResultTracker
{

   private static ResultTracker resultTracker;

   public enum Result {
      SUCCESS, FAILURE
   }

   private Result result = Result.FAILURE;

   private ResultTracker()
   {

   }

   public static synchronized ResultTracker getInstance()
   {
      if (resultTracker == null)
      {
         resultTracker = new ResultTracker();
      }
      return resultTracker;
   }

   public void setSuccess()
   {
      this.result = Result.SUCCESS;
   }

   public void setFailure()
   {
      this.result = Result.FAILURE;
   }

   public Result getResult()
   {
      return this.result;
   }
}
