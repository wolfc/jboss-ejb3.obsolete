/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.blob.bean;

import java.util.HashMap;
import javax.ejb.Remote;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Remote
public interface LobTester
{
   long create();

   HashMap findBlob(long id) throws Exception;

   String findClob(long id) throws Exception;

   long create2();

   BlobEntity2 findBlob2(long id) throws Exception;
}
