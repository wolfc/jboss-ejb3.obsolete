/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.mcint.metadata;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.common.resolvers.spi.EjbReference;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.ejb3.common.resolvers.spi.UnresolvableReferenceException;

/**
 * MockEjbReferenceResolver
 * 
 * A mock EJB Reference resolver which always resolves
 * to a string form based upon the values of the specified
 * reference 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockEjbReferenceResolver implements EjbReferenceResolver
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   private static final String NULL_VALUE = "null";

   private static final char DELIMITER = '-';

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   /**
    * Mock implementation, resolves to:
    * 
    * "[beanName]-[beanInterface]-[mappedName]"
    * 
    * ...of the reference.
    * 
    * In the case any of these properties are null, the String
    * "null" will be used in its place.
    * 
    * @param du
    * @param reference
    * @return The non-resolved string form of the reference
    */
   public String resolveEjb(DeploymentUnit du, EjbReference reference) throws UnresolvableReferenceException
   {
      // Initialize
      StringBuffer buffer = new StringBuffer();

      // Get properties and adjust for blank/null
      String beanName = this.getAdjustedValue(reference.getBeanName());
      String beanInterface = this.getAdjustedValue(reference.getBeanInterface());
      String mappedName = this.getAdjustedValue(reference.getMappedName());

      // Append properties
      buffer.append(beanName);
      buffer.append(DELIMITER);
      buffer.append(beanInterface);
      buffer.append(DELIMITER);
      buffer.append(mappedName);

      // Return
      return buffer.toString();
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   /**
    * Adjusts the specified input to be "null" if it's
    * null or blank
    * 
    * @param input
    * @return
    */
   private String getAdjustedValue(String input)
   {
      return input != null && input.trim().length() > 0 ? input : NULL_VALUE;
   }

}
