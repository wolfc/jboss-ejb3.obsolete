/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.clusteredsession.nested.base.std;

import java.rmi.dgc.VMID;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.test.stateful.nested.base.VMTracker;
import org.jboss.ejb3.test.stateful.nested.base.std.NestedStateful;
import org.jboss.ejb3.test.stateful.nested.base.std.OptimizedNestedStatefulBean;

/**
 * Nested SFSB with a remote interface.
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 * @version $Revision: 60066 $
 */
@Clustered
@Stateful(name="testNestedStateful")
@CacheConfig(maxSize=1000, idleTimeoutSeconds=1)
@Remote(NestedStateful.class)
public class ClusteredRemoteNestedStatefulBean extends OptimizedNestedStatefulBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   public VMID getVMID()
   {
      return VMTracker.VMID;
   }
}
