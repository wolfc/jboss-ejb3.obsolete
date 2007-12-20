/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree786;

/**
 * 
 * AbstractRemoveBean.
 * 
 * @author <a href="arubinge@redhat.com">ALR</a>
 * @version $Revision:  $
 */
public class AbstractRemoveBean implements Remove
{
   // Class Members
   public static final String RETURN_STRING = "Remove";

   // Required Implementations
   public String remove()
   {
      return AbstractRemoveBean.RETURN_STRING;
   }
}
