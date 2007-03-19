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
package org.jboss.ejb3.test.ejbthree655;

import org.jboss.logging.Logger;

/**
 * Comment
 * 
 * FIXME: This is thing might be useless because all the lifecycle methods are optional.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractStateChecker
{
   private static final Logger log = Logger.getLogger(AbstractStateChecker.class);
   
   public static enum State { INITIATED, CREATED, STARTED, STOPPED, DESTROYED };
   
   private State currentState = State.INITIATED;
   
   public void create()
   {
      log.info("create called on " + this);
      
      setState(State.INITIATED, State.CREATED);
   }
   
   public void destroy()
   {
      log.info("destroy called on " + this);
      
      setState(State.STOPPED, State.DESTROYED);
   }
   
   public State getState()
   {
      return currentState;
   }
   
   private void setState(State expectedState, State newState)
   {
      //log.info("setState expected = " + expectedState + ", current = " + getState() + ", new = " + newState);
      if(!this.currentState.equals(expectedState))
      {
         // the exception is gobled up somewhere
         log.warn("state should be " + expectedState + ", not " + currentState);
         throw new IllegalStateException("state should be " + expectedState + ", not " + currentState);
      }
      
      this.currentState = newState;
   }
   
   public void start()
   {
      log.info("start called on " + this);
      
      if(currentState.equals(State.STOPPED))
         setState(State.STOPPED, State.STARTED);
      else
         setState(State.CREATED, State.STARTED);
   }
   
   public void stop()
   {
      //new Throwable().printStackTrace();
      log.info("stop called on " + this);
      
      setState(State.STARTED, State.STOPPED);
   }
}
