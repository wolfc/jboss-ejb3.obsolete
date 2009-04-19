/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.stateful.nested;

import java.rmi.dgc.VMID;

import javax.ejb.Local;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.test.stateful.nested.base.VMTracker;
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
@Stateful(name="testLocalNestedStateful")
@CacheConfig(maxSize=10000, idleTimeoutSeconds=1) 
@Local(NestedStateful.class)
public class SimpleLocalNestedStatefulBean extends NestedStatefulBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   public VMID getVMID()
   {
      return VMTracker.VMID;
   }
}
