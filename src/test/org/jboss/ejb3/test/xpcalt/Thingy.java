/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.xpcalt;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A thingy
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 64905 $
 */
@Entity
public class Thingy implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private Long id;
   private String text;
   
   public Thingy()
   {
      this(null, null);
   }
   
   public Thingy(Long id)
   {
      this(id, null);
   }
   
   public Thingy(Long id, String s)
   {
      this.id = id;
      this.text = s;
   }
   
   @Id
   public Long getId()
   {
      return id;
   }
   
   public void setId(Long id)
   {
      this.id = id;
   }
   
   public String getText()
   {
      return text;
   }
   
   public void setText(String s)
   {
      this.text = s;
   }
   
   public String toString()
   {
      return super.toString() + "{id=" + id + ",text=" + text + "}";
   }
}
