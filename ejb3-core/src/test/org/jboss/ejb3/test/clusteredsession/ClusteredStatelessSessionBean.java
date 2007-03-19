/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.clusteredsession;

import org.jboss.annotation.ejb.Clustered;
import org.jboss.ha.framework.interfaces.RoundRobin;

import javax.ejb.Stateless;
import javax.ejb.Remote;
import java.rmi.dgc.VMID;

/**
 * Simple statless bean
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
