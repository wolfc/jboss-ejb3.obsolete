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
package org.jboss.ejb3.test.security;

import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue; import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.security.auth.Subject;

/** A BMP entity bean that creates beans on the fly with
a key equal to that passed to findByPrimaryKey. Obviously
not a real entity bean. It is used to test Principal propagation
using the echo method. 

@author Scott.Stark@jboss.org
@version $Revision$
*/
@Entity
@Table(name = "ENTITY_BEAN")     
public class EntityBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
    private Long key;
   
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long getKey()
    {
       return key;
    }
    
    public void setKey(Long key)
    {
       this.key = key;
    }

    public String echo(String arg)
    {
        log.debug("EntityBean.echo, arg="+arg);
        // Check the java:comp/env/security/security-domain
        try
        {
           InitialContext ctx = new InitialContext();
           Object securityMgr = ctx.lookup("java:comp/env/security/security-domain");
           log.debug("Checking java:comp/env/security/security-domain");
           if( securityMgr == null )
              throw new EJBException("Failed to find security mgr under: java:comp/env/security/security-domain");
           log.debug("Found SecurityManager: "+securityMgr);
           Subject activeSubject = (Subject) ctx.lookup("java:comp/env/security/subject");
           log.debug("ActiveSubject: "+activeSubject);
        }
        catch(NamingException e)
        {
           log.debug("failed", e);
           throw new EJBException("Naming exception: "+e.toString(true));
        }
        return null;
    }
}
