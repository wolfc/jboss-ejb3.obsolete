/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.security.bean;

import javax.ejb.Remote;

@Remote
public interface Calculator
{
   int add(int x, int y);

   int subtract(int x, int y);

   int divide(int x, int y);
}
