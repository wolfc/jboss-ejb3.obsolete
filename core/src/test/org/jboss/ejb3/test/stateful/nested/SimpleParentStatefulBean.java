/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.stateful.nested;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.test.stateful.nested.base.std.ParentStatefulBean;
import org.jboss.ejb3.test.stateful.nested.base.std.ParentStatefulRemote;

/**
 * Parent SFSB that contains nested SFSB.
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * @version $Revision: 67628 $
 */
@Stateful(name="testParentStateful")
@CacheConfig(maxSize=1000, idleTimeoutSeconds=1)   // this will get evicted the second time eviction thread wakes up
@Remote(ParentStatefulRemote.class)
public class SimpleParentStatefulBean extends ParentStatefulBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
}
