/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.embedded;

import javax.transaction.xa.Xid;
import org.jboss.tm.*;

/**
 * Stupid wrapper because tons of services use the XidFactoryMBean interface.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class XidFactoryMBean implements org.jboss.tm.XidFactoryMBean
{
   private XidFactoryBase base;

   public XidFactoryMBean(XidFactoryBase base)
   {
      this.base = base;
   }

   public org.jboss.tm.XidFactoryMBean getInstance()
   {
      return this;
   }

   public String getName()
   {
      throw new RuntimeException("should not be called");
   }

   public int getState()
   {
      throw new RuntimeException("should not be called");
   }

   public String getStateString()
   {
      throw new RuntimeException("should not be called");
   }

   public void jbossInternalLifecycle(String method) throws Exception
   {
      throw new RuntimeException("should not be called");
   }

   public void create() throws Exception
   {
   }

   public void start() throws Exception
   {
   }

   public void stop()
   {
   }

   public void destroy()
   {
   }

   public String getBaseGlobalId()
   {
      return base.getBaseGlobalId();
   }

   public void setBaseGlobalId(String baseGlobalId)
   {
      base.setBaseGlobalId(baseGlobalId);
   }

   public long getGlobalIdNumber()
   {
      return base.getGlobalIdNumber();
   }

   public void setGlobalIdNumber(long globalIdNumber)
   {
      base.setGlobalIdNumber(globalIdNumber);
   }

   public String getBranchQualifier()
   {
      return base.getBranchQualifier();
   }

   public void setBranchQualifier(String branchQualifier)
   {
      base.setBranchQualifier(branchQualifier);
   }

   public boolean isPad()
   {
      return base.isPad();
   }

   public void setPad(boolean pad)
   {
      base.setPad(pad);
   }

   public XidImpl newXid()
   {
      return base.newXid();
   }

   public XidImpl newBranch(GlobalId globalId)
   {
      return base.newBranch(globalId);
   }

   public XidImpl newBranch(XidImpl xid, long branchIdNum)
   {
      return base.newBranch(xid, branchIdNum);
   }

   public XidImpl recreateXid(long localId)
   {
      return base.recreateXid(localId);
   }

   public XidImpl recreateXid(long localId, GlobalId globalId)
   {
      return base.recreateXid(localId, globalId);
   }

   public byte[] localIdToGlobalId(long localId)
   {
      return base.localIdToGlobalId(localId);
   }

   public long extractLocalIdFrom(byte[] globalId)
   {
      return base.extractLocalIdFrom(globalId);
   }

   public String getBaseBranchQualifier(byte[] branchQualifier)
   {
      return base.getBaseBranchQualifier(branchQualifier);
   }

   public String toString(Xid xid)
   {
      return base.toString(xid);
   }
}
