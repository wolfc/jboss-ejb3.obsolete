/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.asynch.bean;

import javax.ejb.Stateless;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Stateless
public class EchoBean implements Echo
{
   public String echo(String s)
   {
      return s;
   }
}
