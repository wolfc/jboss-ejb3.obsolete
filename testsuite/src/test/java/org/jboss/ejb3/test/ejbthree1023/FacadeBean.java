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
package org.jboss.ejb3.test.ejbthree1023;

import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.aop.Advised;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Stateless
public class FacadeBean implements FacadeRemote
{
   public void callWovenBeanWithAnnotatedLocal() throws Exception
   {
      InitialContext ctx = new InitialContext();
      AnnotatedLocal local = (AnnotatedLocal)ctx.lookup("AnnotatedLocalBean/local");
   
      if (!Advised.class.isAssignableFrom(AnnotatedLocalBean.class))
      {
         throw new RuntimeException("AnnotatedLocalBean was not woven");
      }
      
      AnnotatedLocalBean.woven = false;
      AnnotatedLocalBean.notWoven = false;
      TestInterceptor.invoked = false;
      
      local.woven();
      
      if (!AnnotatedLocalBean.woven)
      {
         throw new RuntimeException("woven was not called");
      }
      if (AnnotatedLocalBean.notWoven)
      {
         throw new RuntimeException("notWoven was called");
      }
      if (!TestInterceptor.invoked)
      {
         throw new RuntimeException("TestInterceptor was not invoked");
      }
      
      AnnotatedLocalBean.woven = false;
      AnnotatedLocalBean.notWoven = false;
      TestInterceptor.invoked = false;

      local.notWoven();
      
      if (AnnotatedLocalBean.woven)
      {
         throw new RuntimeException("woven was called");
      }
      if (!AnnotatedLocalBean.notWoven)
      {
         throw new RuntimeException("notWoven was not called");
      }
      if (TestInterceptor.invoked)
      {
         throw new RuntimeException("TestInterceptor was invoked");
      }
      
   }
   
   public void callWovenBeanWithDefaultLocal() throws Exception
   {
      InitialContext ctx = new InitialContext();
      DefaultLocal local = (DefaultLocal)ctx.lookup("DefaultLocalBean/local");

      if (!Advised.class.isAssignableFrom(DefaultLocalBean.class))
      {
         throw new RuntimeException("DefaultLocalBean was not woven");
      }
      
      DefaultLocalBean.woven = false;
      DefaultLocalBean.notWoven = false;
      TestInterceptor.invoked = false;
      
      local.woven();
      
      if (!DefaultLocalBean.woven)
      {
         throw new RuntimeException("woven was not called");
      }
      if (DefaultLocalBean.notWoven)
      {
         throw new RuntimeException("notWoven was called");
      }
      if (!TestInterceptor.invoked)
      {
         throw new RuntimeException("TestInterceptor was not invoked");
      }
      
      DefaultLocalBean.woven = false;
      DefaultLocalBean.notWoven = false;
      TestInterceptor.invoked = false;

      local.notWoven();
      
      if (DefaultLocalBean.woven)
      {
         throw new RuntimeException("woven was called");
      }
      if (!DefaultLocalBean.notWoven)
      {
         throw new RuntimeException("notWoven was not called");
      }
      if (TestInterceptor.invoked)
      {
         throw new RuntimeException("TestInterceptor was invoked");
      }
      
   }

}
