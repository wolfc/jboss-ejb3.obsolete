/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.relationships.bean;



/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public interface EntityTest
{
   Flight findFlightById(Long id) throws Exception;

   void manyToManyCreate() throws Exception;
}
