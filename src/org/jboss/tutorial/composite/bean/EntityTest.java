/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.composite.bean;

import javax.ejb.Remote;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Remote
public interface EntityTest
{
   Flight findFlightById(Long id) throws Exception;

   void manyToManyCreate() throws Exception;
}
