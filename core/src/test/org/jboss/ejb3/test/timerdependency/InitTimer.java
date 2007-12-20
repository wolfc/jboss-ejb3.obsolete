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
package org.jboss.ejb3.test.timerdependency;

import javax.ejb.EJB;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision: 67628 $
 */
@Service (objectName="telcordia.smg:service=InitTimer")
@Management(InitTimerMBean.class)
@Depends(value="jboss.j2ee:jar=timerdependency.jar,name=TimerTestBean,service=EJB3")
public class InitTimer implements InitTimerMBean
{
   private static final Logger log = Logger.getLogger(InitTimer.class);
   
    @EJB
    TimerTest timer;
    
    private long intervalDuration_ = 1;	
    
    public void start()
	{
    	log.info("called start");
	    startTimer();
	}

	public void create()
	{
       log.info("called create");
	}
	
	public void stop()
	{
       log.info("called stop");
	}
	
	public void destroy()
	{
		
	}
	
	public long getInterval() {
		// TODO Auto-generated method stub
		return intervalDuration_;
	}

	public void setInterval(long interval) {
		// TODO Auto-generated method stub
		intervalDuration_ = interval;
		//startTimer();
	}	
	
	void startTimer()
	{
	      try {
             log.info("Creating a timer with an interval duration of " +
	            		intervalDuration_ + " ms.");
             log.info("is timer valid: " + timer);
	            timer.createTimer(intervalDuration_);
	        } catch (Exception ex) {
               log.error("Caught an unexpected exception.");
	            ex.printStackTrace();
	        }		
	}

}
