/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.embedded.test.datasource;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public @interface DataSourceDefinition {
   String className();
   
   String name();
   
   String description() default "";
   
   String url() default "";
   
   String user() default "";
   
   String password() default "";
   
   String databaseName() default "";
   
   int portNumber() default -1;
   
   String serverName() default "localhost";
   
   int isolationLevel() default -1;
   
   boolean transactional() default true;
   
   int initialPoolSize() default -1;
   
   int maxPoolSize() default -1;
   
   int minPoolSize() default -1;
   
   int maxIdleTime() default -1;
   
   int maxStatements() default -1;
   
   String[] properties() default {};
   
   int loginTimeout() default 0;
}
