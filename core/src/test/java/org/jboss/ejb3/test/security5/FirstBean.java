/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.security5;

import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;


//$Id$

/**
 *  First Level Bean that defines runAs on some methods
 *  @author Anil.Saldhana@redhat.com
 *  @since  Aug 20, 2007 
 *  @version $Revision$
 */

@Stateless
@Local(SimpleSessionInterface.class)
@RunAs("InternalRole")
public class FirstBean extends SimpleSessionBean
{   
   private InitialContext context = null;
   
   @RolesAllowed({"InternalRole"}) 
   public String echo(String arg)
   {   
      SimpleSessionInterface ssi = null;
      try
      { 
         context = new InitialContext();
         String jndiName = "SecondBean/local";
         ssi = (SimpleSessionInterface)context.lookup(jndiName);
      } 
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
      String str = ssi.echo(arg);
      System.out.println("RESPONSE FROM SECOND BEAN="+str);
      if(str.equals(arg) == false)
         throw new IllegalStateException("Second Bean returned:"+str); 
      return arg;
   } 
}
