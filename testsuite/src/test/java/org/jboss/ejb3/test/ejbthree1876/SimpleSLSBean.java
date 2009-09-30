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

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.test.ejbthree1876.ResultTracker.Result;

/**
 * SimpleSLSBean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote(StatelessRemote.class)
@RemoteBinding(jndiBinding = SimpleSLSBean.JNDI_NAME)
public class SimpleSLSBean implements StatelessRemote
{

   public static final String JNDI_NAME = "SimpleBean";

   public void doNothing()
   {
      // As suggested by the method name, do nothing!

   }

   public void setException(Exception e)
   {
      // just delegate to the singleton result tracker
      ResultTracker.getInstance().setException(e);

   }

   public void setFailed()
   {
      // just delegate to the singleton result tracker
      ResultTracker.getInstance().setFailed();

   }

   public void setPassed()
   {
      // just delegate to the singleton result tracker
      ResultTracker.getInstance().setPassed();

   }

   public Result getResult()
   {
      // just delegate to the singleton result tracker
      return ResultTracker.getInstance().getResult();
   }

   public Exception getFailureCause()
   {
      // just delegate to the singleton result tracker
      return ResultTracker.getInstance().getFailureCause();
   }

}
