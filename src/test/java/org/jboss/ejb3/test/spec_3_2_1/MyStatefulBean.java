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

import javax.ejb.Remote;
import javax.ejb.Stateful;
/**
 * MyStatefulBean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateful
@Remote(MyRemote.class)
public class MyStatefulBean implements MyRemote
{

   // No state maintained in this bean. Just a dummy SFSB for testing
   
   /**
    * @see {@link MyRemote#changeAndReturn(SimplePojo)}
    */
   public SimplePojo changeAndReturn(SimplePojo sp)
   {
      if (sp == null)
      {
         return null;
      }
      // set the transient field to some random value.
      // See the org.jboss.ejb3.test.spec_3_2_1.unit.IntraJvmRemoteInvocationPassByValueTestCase for 
      // more details about how this "transientField" value is interpreted in the testcase
      sp.setTransientField(432);

      return sp;
   }

   /**
    * @see {@link MyRemote#doNothingAndReturn(SimplePojo)}
    */
   public SimplePojo doNothingAndReturn(SimplePojo sp)
   {
      return sp;
   }

   /**
    * @see {@link MyRemote#getPojo()}
    */
   public SimplePojo getPojo()
   {
      SimplePojo sp = new SimplePojo();
      // remember, the default constructor of SimplePojo sets the 
      // transient field value to non-default. 
      // See the org.jboss.ejb3.test.spec_3_2_1.unit.IntraJvmRemoteInvocationPassByValueTestCase for 
      // more details about how this "transientField" value is interpreted in the testcase
      return sp;
   }
}
