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

package org.jboss.ejb3.security;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.CodeSource;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain; 
import java.util.Set;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.aop.metadata.SimpleClassMetaDataBinding;
import org.jboss.aop.metadata.SimpleClassMetaDataLoader;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb3.EJBContainer;
import org.jboss.logging.Logger;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.security.RealmMapping; 
import org.jboss.security.RunAsIdentity;

/**
 * JACC Helper class that created permissions as well as done the checks
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @author Anil.Saldhana@jboss.com
 * @version $Revision$
 */
public class JaccHelper
{
   static Logger log = Logger.getLogger(JaccHelper.class);

   /**
    * Creates a new policy configuration on (re)deployment. Context ID used is based on
    * name of app, so we make sure we clean out any existing policy with that id.
    */
   public static PolicyConfiguration initialiseJacc(String contextID) throws Exception
   {
      log.trace("Initialising JACC Context for deployment: " + contextID);
      PolicyConfigurationFactory pcFactory = Ejb3PolicyConfigurationFactory.getPolicyConfigurationFactory();
      boolean removeExistingContext = true;
      PolicyConfiguration pc = pcFactory.getPolicyConfiguration(contextID, removeExistingContext);

      /*Set keys = PolicyContext.getHandlerKeys();
      if (!keys.contains(EnterpriseBeanPolicyContextHandler.EJB_CONTEXT_KEY))
      {
         EnterpriseBeanPolicyContextHandler beanHandler = new EnterpriseBeanPolicyContextHandler();
         PolicyContext.registerHandler(EnterpriseBeanPolicyContextHandler.EJB_CONTEXT_KEY,
               beanHandler, false);
      }
      */
      //Do I need this?
      /*BeanMetaDataPolicyContextHandler metadataHandler = new BeanMetaDataPolicyContextHandler();
      PolicyContext.registerHandler(BeanMetaDataPolicyContextHandler.METADATA_CONTEXT_KEY,
         metadataHandler, false);*/
      /*
      if (!keys.contains(EJBArgsPolicyContextHandler.EJB_ARGS_KEY))
      {
         EJBArgsPolicyContextHandler argsHandler = new EJBArgsPolicyContextHandler();
         PolicyContext.registerHandler(EJBArgsPolicyContextHandler.EJB_ARGS_KEY,
               argsHandler, false);
      }
      */
      return pc;
   }

   public static void putJaccInService(PolicyConfiguration pc, DeploymentUnit di) throws Exception
   {
      //TODO: How do we link this with the parent PC?
      pc.commit(); 
   }
   
   public static void putJaccInService(PolicyConfiguration pc, DeploymentInfo di) throws Exception
   {
      di.context.put("javax.security.jacc.PolicyConfiguration", pc);

      // Link this to the parent PC
      DeploymentInfo current = di;
      while (current.parent != null)
      {
         current = current.parent;
      }

      PolicyConfiguration parentPC = (PolicyConfiguration)
              current.context.get("javax.security.jacc.PolicyConfiguration");

      if (parentPC != null && parentPC != pc)
      {
         parentPC.linkConfiguration(pc);
      }

      pc.commit();
      log.trace("JACC Policy Configuration for deployment has been put in service");
   }

   public static void unregisterJacc(String contextID) throws Exception
   {
      PolicyConfigurationFactory pcFactory = Ejb3PolicyConfigurationFactory.getPolicyConfigurationFactory();
      PolicyConfiguration pc = pcFactory.getPolicyConfiguration(contextID, true);
      pc.delete();
   }


   public static void configureContainer(String jaccContextId, EJBContainer container)
   {
      try
      {
         addJaccContextToContainer(jaccContextId, container);
         PolicyConfigurationFactory pcFactory = Ejb3PolicyConfigurationFactory.getPolicyConfigurationFactory();
         PolicyConfiguration pc = pcFactory.getPolicyConfiguration(jaccContextId, false);

         addPermissions(container, pc);
      }
      catch (Exception e)
      { 
         throw new RuntimeException(e);
      }
   }

   private static void addPermissions(EJBContainer container, PolicyConfiguration pc)
   {
      SecurityDomain sd = (SecurityDomain) container.resolveAnnotation(SecurityDomain.class); 

      PermitAll beanUnchecked = (PermitAll) container.resolveAnnotation(PermitAll.class);
      RolesAllowed beanPermissions = (RolesAllowed) container.resolveAnnotation(RolesAllowed.class);
      
      DeclareRoles beanDeclareRolesPerms = (DeclareRoles)container.resolveAnnotation(DeclareRoles.class);

      if (beanUnchecked != null && beanPermissions != null)
      {
         throw new RuntimeException("Cannot annotate a bean with both @Unchecked and @MethodPermissions");
      }

      String ejbName = container.getEjbName();

      //Add the security role references
      if(beanDeclareRolesPerms != null)
      {
         String[] rolerefs = beanDeclareRolesPerms.value();
         int len = rolerefs != null ? rolerefs.length : 0;
         for(int i=0; i < len; i++)
         {
             try
            {
               pc.addToRole(rolerefs[i], new EJBRoleRefPermission(ejbName, rolerefs[i]));
            }
            catch (PolicyContextException e)
            {
               throw new RuntimeException(e);
            } 
         }
      }
      
      //Am I iterating over the right thing here? Should I be using the stuff from 
      //Advisor.methodInterceptors instead?
      Method[] methods = container.getBeanClass().getDeclaredMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method m = methods[i];
         if (!Modifier.isPublic(m.getModifiers()))
         {
            continue;
         }

         EJBMethodPermission permission = new EJBMethodPermission(ejbName, null, m);
         log.trace("Creating permission: " + permission);

         PermitAll unchecked = (PermitAll) container.resolveAnnotation(m, PermitAll.class);
         RolesAllowed permissions = (RolesAllowed) container.resolveAnnotation(m, RolesAllowed.class);
         DenyAll exclude = (DenyAll) container.resolveAnnotation(m, DenyAll.class);

         int annotationCount = getAnnotationCount(unchecked, permissions, exclude);

         if (annotationCount == 0 && beanPermissions == null && beanUnchecked == null)
         {
            //continue;
            //EJBTHREE-755:Add to unchecked if there are no annotations
            try
            {
               pc.addToUncheckedPolicy(permission);
            }
            catch (PolicyContextException e)
            {
               throw new RuntimeException(e); 
            } 
         }
         else if (annotationCount > 1)
         {
            throw new RuntimeException("You can only use one of @PermitAll, @DenyAll or @RolesAllowed per method");
         }

         try
         {
            //Method level annotations override the bean level annotations
            if (unchecked != null)
            {
               pc.addToUncheckedPolicy(permission);
               log.trace("Adding permission to unchecked policy");
               continue;
            }
            if (permissions != null)
            {
               addToRole(pc, permission, permissions);
               continue;
            }
            if (exclude != null)
            {
               pc.addToExcludedPolicy(permission);
               log.trace("Adding permission to excluded policy");
               continue;
            }

            if (beanUnchecked != null)
            {
               pc.addToUncheckedPolicy(permission);
               log.trace("Adding permission to unchecked policy");
               continue;
            }
            if (beanPermissions != null)
            {
               addToRole(pc, permission, beanPermissions);
               continue;
            }

            //The default is unchecked
            pc.addToUncheckedPolicy(permission);
            log.trace("Adding permission to unchecked policy");
         }
         catch (PolicyContextException e)
         {
            throw new RuntimeException(e);  
         }
      }
   }

   private static int getAnnotationCount(PermitAll u, RolesAllowed mp, DenyAll e)
   {
      int annotations = 0;
      if (u != null) annotations++;
      if (mp != null) annotations++;
      if (e != null) annotations++;

      return annotations;
   }

   private static void addToRole(PolicyConfiguration pc, EJBMethodPermission p, RolesAllowed mp) throws PolicyContextException
   {
      String[] roles = mp.value();
      for (int i = 0; i < roles.length; i++)
      {
         pc.addToRole(roles[i], p);
         log.trace("Adding permission to role: " + roles[i]);
      }
   }

   private static void addJaccContextToContainer(String jaccContextId, EJBContainer container)
   {
      SimpleClassMetaDataLoader loader = SimpleClassMetaDataLoader.singleton;
      String name = container.getBeanClassName();
      SimpleClassMetaDataBinding jaccCtx =
              new SimpleClassMetaDataBinding(loader, name, JaccAuthorizationInterceptor.JACC, container.getBeanClassName());

      jaccCtx.addDefaultMetaData(JaccAuthorizationInterceptor.JACC,
                                 JaccAuthorizationInterceptor.CTX, jaccContextId);

      container.addClassMetaData(jaccCtx);
   }

   public static void checkPermission(CodeSource ejbCS, EJBMethodPermission methodPerm,
         RealmMapping realmMapping) 
   throws EJBAccessException
   {
      try
      {
         Policy policy = Policy.getPolicy();
         // Get the caller
         Subject caller = SecurityActions.getContextSubject();
  
         RunAsIdentity rai = SecurityActions.peekRunAsIdentity();

         Principal[] principals = null;
         if(rai != null)
         {
            Set runAsRoles = rai.getRunAsRoles();
            principals = new Principal[runAsRoles.size()];
            runAsRoles.toArray(principals); 
         }
         else
         {
            /*if (caller != null)
            {
               // Get the caller principals
               Set principalsSet = caller.getPrincipals();
               principals = new Principal[principalsSet.size()];
               principalsSet.toArray(principals);
            }*/
            //Get the current roles from the Authorization Manager
            Principal callerP = SecurityActions.getCallerPrincipal();
            Set principalSet = realmMapping.getUserRoles(callerP);
            principals = new Principal[principalSet.size()];
            principalSet.toArray(principals);
         } 
         
         ProtectionDomain pd = new ProtectionDomain(ejbCS, null, null, principals);
         if (policy.implies(pd, methodPerm) == false)
         {
            String msg = "Denied: " + methodPerm + ", caller=" + caller;
            //SecurityException e = new SecurityException(msg);
            EJBAccessException e = new EJBAccessException(msg);
            throw e;
         }
      }
      catch (PolicyContextException e)
      {
         throw new RuntimeException(e);
      }
   }
}
