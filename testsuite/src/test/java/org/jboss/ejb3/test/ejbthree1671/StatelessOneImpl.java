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
package org.jboss.ejb3.test.ejbthree1671;


import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * StatelessOneImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote (StatelessOne.class)
@RemoteBinding(jndiBinding=StatelessOneImpl.JNDI_NAME)
public class StatelessOneImpl implements StatelessOne
{
   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(StatelessOneImpl.class);
   
   /**
    * Exposing this through the bean implementation, exposes the
    * impl to the client. But we are just doing some tests, so 
    * doesn't matter
    * 
    * JNDI Name
    */
   public static final String JNDI_NAME = "Anything";
   
   // Inject application specific type @Resource
   @Resource (mappedName=StatelessTwoImpl.JNDI_NAME)
   private StatelessTwo statelessTwo;
   
   // Ideally, we should have created and deployed our own datasource for this testcase
   // to avoid depending on DefaultDS. But because of deployment ordering issues 
   // (datasource gets deployed after EJB, when datasource is packaged and deployed through
   // jboss-app.xml), let's just rely on the DefaultDS, which we assume will be available
   @Resource (mappedName="java:/DefaultDS")
   private DataSource dataSource;
   
   /**
    * @see StatelessOne#doNothing()
    */
   public void doNothing()
   {
      // let's atleast do something, now that we are here ;)
      statelessTwo.add(2, 3);
      logger.info("Datasource has been injected, class = " + dataSource.getClass());
      
   }

}
