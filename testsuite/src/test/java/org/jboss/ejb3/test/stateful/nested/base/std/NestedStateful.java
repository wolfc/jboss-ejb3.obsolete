/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.stateful.nested.base.std;

import java.rmi.dgc.VMID;

import org.jboss.ejb3.test.stateful.nested.base.MidLevel;


/**
 * Comment
 *
 * @author Ben Wang
 * @version $Revision: 60066 $
 */
public interface NestedStateful extends MidLevel
{
   VMID getVMID();
   int increment();
}
