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
package org.jboss.ejb3.test.webservices;

import org.jboss.ejb3.annotation.RemoteBinding;

import javax.ejb.Stateless;
import javax.xml.ws.WebServiceRef;

/**
 * Simple EJB3 bean that delegates to a webservice injected through @WebServiceRef
 * 
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
@Stateless
@RemoteBinding(jndiBinding = "/test-webservices/WebServiceRefBean")
public class WebServiceRefBean implements BusinessInterface
{
   @WebServiceRef(wsdlLocation = "http://localhost:8080/webservices-ejb3/SimpleEndpoint?wsdl")
   Ejb3WSEndpoint serviceRef;
   
   public String echo(String msg)
   {
      return serviceRef.echo(msg);
   }
}
