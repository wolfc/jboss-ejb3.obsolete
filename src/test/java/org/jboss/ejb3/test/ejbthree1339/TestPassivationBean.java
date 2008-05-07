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
package org.jboss.ejb3.test.ejbthree1339;

import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * TestPassivationBean
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Remote(TestPassivationRemote.class)
@RemoteBinding(jndiBinding = TestPassivationRemote.JNDI_NAME)
@CacheConfig(idleTimeoutSeconds = 1L)
public class TestPassivationBean implements TestPassivationRemote
{

   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(TestPassivationBean.class);

   // ---------------------------------------------------------------------------||
   // Instance Members ----------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Whether the bean has yet been passivated
    */
   private boolean beenPassivated = false;

   /**
    * Whether the bean has yet been activated
    */
   private boolean beenActivated = false;

   // ---------------------------------------------------------------------------||
   // Functional Methods --------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Returns the expected result
    * 
    * @return
    */
   public String returnTrueString()
   {
      return TestPassivationRemote.EXPECTED_RESULT;
   }

   /**
    * Returns whether or not this instance has been passivated
    * 
    * @return
    */
   public boolean hasBeenPassivated()
   {
      return this.beenPassivated;
   }

   /**
    * Returns whether or not this instance has been activated
    * 
    * @return
    */
   public boolean hasBeenActivated()
   {
      return this.beenActivated;
   }

   // ---------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Sets the passivation flag before reactivation
    */
   @PrePassivate
   public void setPassivateFlag()
   {
      log.info(this.toString() + " PrePassivation...");
      this.beenPassivated = true;
   }

   @PostActivate
   public void setActivateFlag()
   {
      log.info(this.toString() + " Activated.");
      this.beenActivated = true;
   }
}
