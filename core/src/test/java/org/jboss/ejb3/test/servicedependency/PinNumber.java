/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.servicedependency;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;

/**
 * A PinNumberMBean.
 * 
 * @author <a href="galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision$
 */
@Service(objectName="acme:service=pinnumber")
@Management(PinNumberMBean.class)
public class PinNumber implements PinNumberMBean
{
   @Depends ("acme:service=uniqueid")
   private UniqueIdMBean uniqueId;

   public short createRandom()
   {
      return (short)uniqueId.generate().getLeastSignificantBits();    
   }

}
