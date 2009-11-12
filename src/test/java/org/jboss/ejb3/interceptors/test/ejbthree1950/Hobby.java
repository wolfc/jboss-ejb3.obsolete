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
package org.jboss.ejb3.interceptors.test.ejbthree1950;

import java.io.Serializable;

/**
 * Ett falskt enhet Bean.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class Hobby implements Serializable
{
   private static final long serialVersionUID = 1L;

   private Long id;
   private String namn;
   private String beskrivning;
   
   public String getBeskrivning()
   {
      return beskrivning;
   }
   
   public void setBeskrivning(String beskrivning)
   {
      this.beskrivning = beskrivning;
   }
   
   public Long getId()
   {
      return id;
   }
   
   public void setId(Long id)
   {
      this.id = id;
   }
   
   public String getNamn()
   {
      return namn;
   }
   
   public void setNamn(String namn)
   {
      this.namn = namn;
   }
   
   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + ",namn=" + namn + "}";
   }
}
