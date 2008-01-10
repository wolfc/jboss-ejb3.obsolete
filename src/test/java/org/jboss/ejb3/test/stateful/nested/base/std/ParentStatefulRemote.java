/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.stateful.nested.base.std;

import java.rmi.dgc.VMID;

import org.jboss.ejb3.test.stateful.nested.base.TopLevel;


/**
 * Parent sfsb interface
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * @version $Revision: 60066 $
 */
public interface ParentStatefulRemote 
   extends TopLevel
{
   VMID getVMID();
   
   int increment();

   int incrementLocal();

   void longRunning() throws Exception;
   
   int getLocalNestedPostActivate();

   int getLocalNestedPrePassivate();

   public void setUpFailover(String failover);
   
}
