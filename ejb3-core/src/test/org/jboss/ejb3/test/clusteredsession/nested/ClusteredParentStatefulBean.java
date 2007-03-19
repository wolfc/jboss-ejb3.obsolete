/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.clusteredsession.nested;

import java.rmi.dgc.VMID;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import org.jboss.annotation.ejb.Clustered;
import org.jboss.annotation.ejb.cache.tree.CacheConfig;
import org.jboss.ejb3.test.clusteredsession.ExplicitFailoverInterceptor;
import org.jboss.ejb3.test.stateful.nested.base.std.ParentStatefulBean;
import org.jboss.ejb3.test.stateful.nested.base.std.ParentStatefulRemote;

/**
 * Parent SFSB that contains nested SFSB.
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * @version $Revision: 60408 $
 */
@Clustered
@Stateful(name="testParentStateful")
@CacheConfig(maxSize=1000, idleTimeoutSeconds=1)   // this will get evicted the second time eviction thread wakes up
@Remote(ParentStatefulRemote.class)
public class ClusteredParentStatefulBean extends ParentStatefulBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   // Mimic explict failover
   @Interceptors({ExplicitFailoverInterceptor.class})
   public VMID getVMID()
   {
      return super.getVMID();
   }
}
