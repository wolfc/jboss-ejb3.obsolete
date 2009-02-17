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
package org.jboss.ejb3.test.mail;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
@Stateless
@Remote(StatelessMail.class)
@RemoteBinding(jndiBinding="StatelessMail")
public class StatelessMailBean
   implements StatelessMail
{
   private static final Logger log = Logger.getLogger(StatelessMailBean.class);
   
   @Resource(name="DefaultMail", mappedName="java:/Mail")
   private Session mailSession;
   
   @Resource(mappedName="java:/Mail")
   private Session session;
   
   public void testMail() throws NamingException
   {
      Context initCtx = new InitialContext();
      Context myEnv = (Context) initCtx.lookup(Container.ENC_CTX_NAME + "/env");
      
      // JavaMail Session
      Object obj = myEnv.lookup("mail/DefaultMail");
      if ((obj instanceof javax.mail.Session) == false)
         throw new NamingException("DefaultMail is not a javax.mail.Session");
   }
   
   public void testMailInjection()
   {
      mailSession.getProperties();
      
      session.getProperties();
   }

}
