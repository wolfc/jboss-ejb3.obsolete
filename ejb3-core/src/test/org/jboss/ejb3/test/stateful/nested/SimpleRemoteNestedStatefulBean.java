/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.stateful.nested;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.ejb3.test.stateful.nested.base.std.NestedStateful;
import org.jboss.ejb3.test.stateful.nested.base.std.OptimizedNestedStatefulBean;

/**
 * Nested SFSB with a remote interface.
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 * @version $Revision$
 */
@Stateful(name="testNestedStateful")
@CacheConfig(maxSize=1000, idleTimeoutSeconds=1)
@Remote(NestedStateful.class)
public class SimpleRemoteNestedStatefulBean extends OptimizedNestedStatefulBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
}
