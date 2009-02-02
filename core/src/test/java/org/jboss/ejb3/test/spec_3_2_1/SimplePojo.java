/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.spec_3_2_1;

import java.io.Serializable;

import org.jboss.ejb3.test.spec_3_2_1.unit.IntraJvmRemoteInvocationPassByValueTestCase;

/**
 * SimplePojo
 * 
 * Used in the {@link IntraJvmRemoteInvocationPassByValueTestCase}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SimplePojo implements Serializable
{

   public static final long UPDATED_VALUE = 123;

   private transient long transientField;

   /**
    * Default constructor which sets the transient field value to some 
    * non-default value.
    * See the {@link IntraJvmRemoteInvocationPassByValueTestCase} for details
    * about how the value of {@link #transientField} is interpreted in the test case
    */
   public SimplePojo()
   {
      this.transientField = UPDATED_VALUE;
   }

   /**
    * Returns the value of {@link #transientField}
    * @return
    */
   public long getTransientField()
   {
      return this.transientField;
   }

   /**
    * Sets the value of {@link #transientField}. 
    * @param val The value to be set
    * @throws IllegalArgumentException If the <code>val</code> to be set 
    *           is 0. We reserve 0 to test the serailization of this {@link SimplePojo}, so
    *           we don't allow the value to be 0.
    *           
    * @see {@link IntraJvmRemoteInvocationPassByValueTestCase}           
    * 
    */
   public void setTransientField(long val)
   {
      if (val == 0)
      {
         throw new IllegalArgumentException(
               "Setting 0 to transient field is NOT allowed in this testcase (0 is considered for a special case in this testcase");
      }
      this.transientField = val;
   }

}
