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
package org.jboss.ejb3.test.security;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.jboss.logging.Logger;

/** An implemeentation of the JAAS CallbackHandler interface that handles
 NameCallbacks, PasswordCallbac, TextInputCallback and the JBoss
 ByteArrayCallback

 @author Scott.Stark@jboss.org
 @version $Revision$
 */
public class AppCallbackHandler implements CallbackHandler
{
   private static final Logger log = Logger.getLogger(AppCallbackHandler.class);
   
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
         else if( c instanceof ByteArrayCallback )
         {
            ByteArrayCallback bac = (ByteArrayCallback) c;
            bac.setByteArray(data);
         }
         else
         {
            throw new UnsupportedCallbackException(c, "Unrecognized Callback");
         }
      }
   }
}

