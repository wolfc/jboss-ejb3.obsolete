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


import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.logging.Logger;


/**
 * TimerBean is a stateless session bean that creates a timer and prints out a
 * message when a timeout occurs.
 * 
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
@Stateless
@Local ({TimerTest.class})
public class TimerTestBean implements TimerTest {
    @Resource
    TimerService timerService;
    
    private static final Logger log = Logger.getLogger(TimerTestBean.class);

    public void createTimer(long intervalDuration) {
        timerService.createTimer(intervalDuration, "Created new timer");
        log.info("created timer in TimerTestBean");
    }

    @Timeout
    public void timeout(Timer timer) {
       log.info("TimerTestBean: timeout occurred");
    }
}
