/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.security.helpers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.security.SecurityContext;
import org.jboss.security.audit.AuditEvent;
import org.jboss.security.audit.AuditManager;
import org.jboss.security.authorization.Resource;
 
/**
 *  Base Class for helpers
 *  @author Anil.Saldhana@redhat.com
 *  @since  Apr 16, 2008 
 *  @version $Revision$
 */
public class SecurityHelper
{ 
   protected static Logger log = null;
   
   protected SecurityContext securityContext = null;
   
   public SecurityHelper(SecurityContext sc)
   {
      log = Logger.getLogger(getClass());
      if(sc == null)
         sc = SecurityActions.getSecurityContext(); 
      this.securityContext = sc;
   }
   

     //******************************************************
     //  Audit Methods
     //******************************************************
     protected void authorizationAudit(String level, Resource resource, Exception e)
     {
        if(securityContext.getAuditManager() == null)
           return;
        //Authorization Exception stacktrace is huge. Scale it down
        //as the original stack trace can be seen in server.log (if needed)
        String exceptionMessage = e != null ? e.getLocalizedMessage() : "";  
        Map<String,Object> cmap = new HashMap<String,Object>();
        cmap.putAll(resource.getMap());
        cmap.put("Resource:", resource.toString());
        cmap.put("Exception:", exceptionMessage);
        audit(level,cmap,null);
     }  
     
     protected void audit(String level,
           Map<String,Object> contextMap, Exception e)
     { 
        AuditManager am = securityContext.getAuditManager();
        if(am == null)
           return;
        contextMap.put("Source", getClass().getName());
        AuditEvent ae = new AuditEvent(level,contextMap,e); 
        am.audit(ae);
     }    
     
     protected Map<String,Object> getContextMap(Principal principal, String methodName)
     {
        Map<String,Object> cmap = new HashMap<String,Object>();
        cmap.put("principal", principal);
        cmap.put("method", methodName);
        return cmap;
     }  

}
