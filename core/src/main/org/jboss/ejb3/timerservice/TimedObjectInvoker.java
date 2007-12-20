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
package org.jboss.ejb3.timerservice;

// $Id: TimedObjectInvoker.java,v 1.1 2006/06/07 09:26:15 wolfc Exp $

import javax.ejb.Timer;

/**
 * An implementation can invoke the ejbTimeout method on a TimedObject.
 *
 * The TimedObjectInvoker has knowledge of the TimedObjectId, it
 * knows which object to invoke.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 * @since 07-Apr-2004
 */
public interface TimedObjectInvoker extends org.jboss.ejb.txtimer.TimedObjectInvoker
{
   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param timer the Timer that is passed to ejbTimeout
    */
   void callTimeout(Timer timer) throws Exception;

}
