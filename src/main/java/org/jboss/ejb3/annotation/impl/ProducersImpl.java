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
package org.jboss.ejb3.annotation.impl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.annotation.Producer;
import org.jboss.ejb3.annotation.Producers;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class ProducersImpl implements Producers, Serializable
{
   private static final long serialVersionUID = 6593673540842223866L;
   
   private List<Producer> producers = new ArrayList<Producer>();
   
   public ProducersImpl()
   {
   }
   
   public Producer[] value()
   {
      Producer[] result = new Producer[producers.size()];
      producers.toArray(result);
      return result;
   }
   
   public void addProducer(Producer producer)
   {
      producers.add(producer);
   }
   
   public Class<? extends Annotation> annotationType()
   {
      return Producers.class;
   }
}
