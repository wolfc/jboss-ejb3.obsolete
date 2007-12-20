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
package org.jboss.ejb3.test.initial;

import java.util.Map;
import java.util.HashMap;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Remote;
import javax.ejb.Local;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
@Stateful
@Remote(StatefulTestRemote.class)
@Local(StatefulTestLocal.class)
public class StatefulTestBean implements StatefulTestRemote, StatefulTestLocal, java.io.Serializable
{
   private String state;

   public void setState(String state)
   {
      this.state = state;
   }

   public String getState()
   {
      System.out.println("**JDK15 getstate");
      return state;
   }

   @Remove
   public void endSession()
   {
   }


   public static Map obj = new HashMap();

   public Map getObject()
   {
      return obj;
   }

   public Object echo(Object e)
   {
      return e;
   }
}
