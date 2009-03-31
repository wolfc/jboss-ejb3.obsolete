/**
 * 
 */
package org.jboss.ejb3.kernel;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jboss.kernel.plugins.registry.AbstractKernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;

/**
 * LazyKernelRegistryEntry
 * 
 * An implementation of {@link KernelRegistryEntry} which returns objects
 * from JNDI, for the given entry name, lazily.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class LazyJNDIKernelRegistryEntry extends AbstractKernelRegistryEntry
{

   /**
    * JNDI context
    */
   private Context ctx;

   /**
    * Constructor 
    * 
    * @param ctx JNDI context
    * @param name The entry name
    */
   public LazyJNDIKernelRegistryEntry(Context ctx, String name)
   {
      super(name);
      this.ctx = ctx;
   }

   /**
    * @see KernelRegistryEntry#getTarget()
    */
   @Override
   public Object getTarget()
   {

      assert name instanceof String : "Cannot determine target for object of type " + this.getName()
            + " - expected String";
      Object boundObject;
      try
      {
         boundObject = this.ctx.lookup((String) this.getName());
         if (log.isTraceEnabled())
         {
            log.trace("Found KernelRegistryEntry with name " + this.getName() + " in jndi");
         }
         return boundObject;
      }
      catch (NameNotFoundException nnfe)
      {
         // TODO: do we really need to take care of NameNotFoundException and return null?
         // Or do we treat it as just another exception? The getTarget() API doesn't specify
         // what the expected behaviour is, if target is not found
         if (log.isTraceEnabled())
         {
            log.trace("Target for KernelRegistryEntry with name " + this.getName() + " not found in jndi");
         }
         return null;
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not obtain target from KernelRegistryEntry for name " + this.getName(), e);
      }

   }

}
