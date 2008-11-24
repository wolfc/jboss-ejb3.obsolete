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
package org.jboss.ejb3.test.tck5sec.unit;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import junit.framework.Test;

import org.jboss.ejb3.test.tck5sec.StatefulSessionTest;
import org.jboss.logging.Logger;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase;


/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class Tck5SecTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(Tck5SecTestCase.class);

   public Tck5SecTestCase(String name)
   {
      super(name);
   }

   public void test2() throws Exception
   {
      AppCallbackHandler handler = new AppCallbackHandler("j2ee", "j2ee".toCharArray());
      LoginContext lc = new LoginContext("spec-test", handler);
      lc.login();
      
      InitialContext jndiContext = new InitialContext();
      StatefulSessionTest sfsb = (StatefulSessionTest)jndiContext.lookup("tck5sec-test/StatefulTestBean/remote");
      boolean success = sfsb.EjbOverloadedSecRoleRefs("Employee", "Administrator");
      assertTrue(success);
   }
   
   public void atest3() throws Exception
   {
      AppCallbackHandler handler = new AppCallbackHandler("j2ee", "j2ee".toCharArray());
      LoginContext lc = new LoginContext("spec-test", handler);
      lc.login();
      
      InitialContext jndiContext = new InitialContext();
      StatefulSessionTest sfsb = (StatefulSessionTest)jndiContext.lookup("tck5sec-test/StatefulTestBean/remote");
      boolean success = sfsb.EjbSecRoleRef("Employee");
      assertTrue(success);
   }

   public static Test suite() throws Exception
   {
      Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
      return getDeploySetup(Tck5SecTestCase.class, "tck5sec-test.ear");

   }
   
   class AppCallbackHandler implements CallbackHandler
   {  
      private String username;
      private char[] password;
      private byte[] data;
      private String text;

      public AppCallbackHandler(String username, char[] password)
      {
         this.username = username;
         this.password = password;
      }
      public AppCallbackHandler(String username, char[] password, byte[] data)
      {
         this.username = username;
         this.password = password;
         this.data = data;
      }
      public AppCallbackHandler(String username, char[] password, byte[] data, String text)
      {
         this.username = username;
         this.password = password;
         this.data = data;
         this.text = text;
      }

      public void handle(Callback[] callbacks) throws
            IOException, UnsupportedCallbackException
      {
         for (int i = 0; i < callbacks.length; i++)
         {
            Callback c = callbacks[i];
    
            if( c instanceof NameCallback )
            {
               NameCallback nc = (NameCallback) c;
               nc.setName(username);
            }
            else if( c instanceof PasswordCallback )
            {
               PasswordCallback pc = (PasswordCallback) c;
               pc.setPassword(password);
            }
            else if( c instanceof TextInputCallback )
            {
               TextInputCallback tc = (TextInputCallback) c;
               tc.setText(text);
            }
            else
            {
               throw new UnsupportedCallbackException(c, "Unrecognized Callback");
            }
         }
      }
   }
}
