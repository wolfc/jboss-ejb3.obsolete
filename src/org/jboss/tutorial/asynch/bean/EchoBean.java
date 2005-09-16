/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.asynch.bean;

import javax.ejb.Stateless;
import javax.ejb.Remote;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Stateless
@Remote(Echo.class)
public class EchoBean implements Echo
{
   public int echo(int i)
   {
      return i;
   }

   public String echo(String s)
   {
      return s;
   }

}
