/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.standalone;

import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Service(objectName="jboss.test:service=depends")
@Management(DependsMBean.class)
public class Depends implements DependsMBean
{
   private static final Logger log = Logger.getLogger(Depends.class);

   public void create() throws Exception
   {
      log.info("create()");
   }

   public void start() throws Exception
   {
      log.info("start()");
   }

   public void stop() throws Exception
   {
      log.info("stop()");
   }

   public void destroy() throws Exception
   {
      log.info("destroy()");
   }

}
