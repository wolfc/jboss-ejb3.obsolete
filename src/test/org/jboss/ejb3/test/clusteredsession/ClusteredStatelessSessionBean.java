/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.clusteredsession;

import java.rmi.dgc.VMID;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ha.framework.interfaces.RoundRobin;

/**
 * Simple stateless bean
 *
 * @author Ben Wang
 */
@Stateless(name="clusteredStateless")
@Clustered(loadBalancePolicy = RoundRobin.class)
@Remote(ClusteredStatelessRemote.class)
public class ClusteredStatelessSessionBean implements ClusteredStatelessRemote
{
   public transient VMID myId = null;
   public NodeAnswer getNodeState() {
      if(myId == null)
      {
         myId = new VMID();
      }
      return new NodeAnswer(this.myId, "test");
   }
}
