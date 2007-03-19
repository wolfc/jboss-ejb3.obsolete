/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.clusteredsession.nested;

import javax.ejb.Local;
import javax.ejb.Stateful;

import org.jboss.annotation.ejb.Clustered;
import org.jboss.annotation.ejb.cache.tree.CacheConfig;
import org.jboss.ejb3.test.stateful.nested.base.std.NestedStateful;
import org.jboss.ejb3.test.stateful.nested.base.std.NestedStatefulBean;

/**
 * Nested SFSB with only a local interface.
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 * @version $Revision: 45372 $
 */
@Clustered
@Stateful(name="testLocalNestedStateful")
@CacheConfig(maxSize=10000, idleTimeoutSeconds=1) 
@Local(NestedStateful.class)
public class ClusteredLocalNestedStatefulBean extends NestedStatefulBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
}
