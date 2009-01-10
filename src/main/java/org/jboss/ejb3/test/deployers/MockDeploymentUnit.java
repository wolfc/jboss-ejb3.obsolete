package org.jboss.ejb3.test.deployers;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;

/**
 * MockDeploymentUnit
 * 
 * An implementation of DeploymentUnit used in EJB3 Testing, equipped with
 * ability to:
 * 
 * - Add attachments
 * - Manage the parent/child relationship
 * - Get the ClassLoader
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockDeploymentUnit extends AbstractDeploymentUnit implements DeploymentUnit
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private String name;

   private DeploymentUnit parent;

   private List<DeploymentUnit> children;

   private Map<String, Object> attachments;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public MockDeploymentUnit(String name)
   {
      this.name = name;
      this.children = new ArrayList<DeploymentUnit>();
      this.attachments = new HashMap<String, Object>();
   }

   public MockDeploymentUnit(String name, DeploymentUnit parent)
   {
      this(name);
      this.parent = parent;
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public void addChild(DeploymentUnit child)
   {
      this.children.add(child);
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Override
   public List<DeploymentUnit> getChildren()
   {
      return this.children;
   }

   @Override
   public DeploymentUnit getParent()
   {
      return this.parent;
   }

   @Override
   public Object addAttachment(String name, Object attachment)
   {
      return this.attachments.put(name, attachment);
   }

   @Override
   public Object getAttachment(String name)
   {
      return this.attachments.get(name);
   }

   @Override
   public Map<String, Object> getAttachments()
   {
      return Collections.unmodifiableMap(this.attachments);
   }

   @Override
   public String toString()
   {
      return this.getClass().getName() + ": " + this.name;
   }

   @Override
   public ClassLoader getClassLoader()
   {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });
   }
}
