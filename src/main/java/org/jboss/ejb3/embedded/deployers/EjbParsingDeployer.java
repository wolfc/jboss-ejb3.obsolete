/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.embedded.deployers;

import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;

/**
 * An ObjectModelFactoryDeployer for translating ejb-jar.xml descriptors into
 * ApplicationMetaData instances.
 *
 * Minus the JMX interface for embedded usage.
 *
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author Jaikiran Pai
 * @version $Revision$
 * @deprecated Since 1.0.0. Use the following configuration in *-beans.xml instead:
 * <pre>
 * &lt;bean name="EjbParsingDeployer" class="org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer"&gt;
 *    &lt;constructor&gt;
 *      &lt;parameter class="java.lang.Class"&gt;org.jboss.metadata.ejb.spec.EjbJarMetaData&lt;/parameter&gt;
 *    &lt;/constructor&gt;
 *    &lt;property name="name"&gt;ejb-jar.xml&lt;/property&gt;
 *    &lt;!-- We need to be fast, so no schema validation --&gt;
 *    &lt;property name="useSchemaValidation"&gt;false&lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 */
//@JMX(name="jboss.ejb:service=EjbParsingDeployer", exposedInterface=EjbParsingDeployerMBean.class)
@Deprecated
public class EjbParsingDeployer extends SchemaResolverDeployer<EjbJarMetaData>// implements EjbParsingDeployerMBean
{
   /**
    * Create a new EjbParsingDeployer.
    */
   public EjbParsingDeployer()
   {
      super(EjbJarMetaData.class);
      setName("ejb-jar.xml");
   }
}
