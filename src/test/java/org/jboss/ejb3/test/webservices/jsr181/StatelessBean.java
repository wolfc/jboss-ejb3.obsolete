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
package org.jboss.ejb3.test.webservices.jsr181;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.rpc.Service;
import javax.xml.ws.WebServiceRef;
import org.jboss.logging.Logger;

import org.jboss.ejb3.Container;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
@Stateless(name="StatelessBean")
@Remote(StatelessRemote.class)
public class StatelessBean implements StatelessRemote
{
   private static final Logger log = Logger.getLogger(StatelessBean.class);
   
   @WebServiceRef(mappedName="jbossws-client/service/TestService")
   EndpointInterface endpoint1;
   
   EndpointInterface endpoint2;
   
   EndpointInterface endpoint3;
   
   EndpointInterface endpoint4;
   
   @WebServiceRef(mappedName="jbossws-client/service/TestService")
   javax.xml.rpc.Service service1;
   
   javax.xml.rpc.Service service2;
   
   javax.xml.rpc.Service service3;
   
   javax.xml.rpc.Service service4;
   
   @WebServiceRef(mappedName="jbossws-client/service/TestService")
   public void setEndpoint2(EndpointInterface endpoint2)
   {
      this.endpoint2 = endpoint2;
   }
   
   public void setEndpoint4(EndpointInterface endpoint4)
   {
      this.endpoint4 = endpoint4;
   }
   
   @WebServiceRef(mappedName="jbossws-client/service/TestService")
   public void setService2(javax.xml.rpc.Service service2)
   {
      this.service2 = service2;
   }
   
   public void setService4(javax.xml.rpc.Service service4)
   {
      this.service4 = service4;
   }
   
   public String echo1(String string) throws Exception
   {
     return endpoint1.echo(string);
   }
   
   public String echo2(String string) throws Exception
   {
     return endpoint2.echo(string);
   }
   
   public String echo3(String string) throws Exception
   {
     return endpoint3.echo(string);
   }
   
   public String echo4(String string) throws Exception
   {
     return endpoint4.echo(string);
   }
   
   public String echo5(String string) throws Exception
   {
      EndpointInterface endpoint = (EndpointInterface)service1.getPort(EndpointInterface.class);
      return endpoint.echo(string);
   }
   
   public String echo6(String string) throws Exception
   {
      EndpointInterface endpoint = (EndpointInterface)service2.getPort(EndpointInterface.class);
      return endpoint.echo(string);
   }
   
   public String echo7(String string) throws Exception
   {
      EndpointInterface endpoint = (EndpointInterface)service3.getPort(EndpointInterface.class);
      return endpoint.echo(string);
   }
   
   public String echo8(String string) throws Exception
   {
      EndpointInterface endpoint = (EndpointInterface)service4.getPort(EndpointInterface.class);
      return endpoint.echo(string);
   }
   
   public String echo9(String string) throws Exception
   {
      InitialContext iniCtx = new InitialContext();
      javax.xml.rpc.Service service = (Service)iniCtx.lookup(Container.ENC_CTX_NAME + "/env/service/Service3");
      
      EndpointInterface endpoint = (EndpointInterface)service.getPort(EndpointInterface.class);
      return endpoint.echo(string);
   }
}
