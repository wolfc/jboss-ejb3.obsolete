/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.timer.bean;

import javax.ejb.Remote;

@Remote
public interface ExampleTimer
{
   void scheduleTimer(long milliseconds);
}
