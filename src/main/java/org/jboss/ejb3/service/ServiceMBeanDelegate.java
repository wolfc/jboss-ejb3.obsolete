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
package org.jboss.ejb3.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.StandardMBean;
import javax.management.NotCompliantMBeanException;
import javax.management.modelmbean.DescriptorSupport;

import org.jboss.mx.modelmbean.XMBean;
import org.jboss.mx.modelmbean.XMBeanConstants;

import org.jboss.logging.Logger;
import org.jboss.util.Classes;


/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class ServiceMBeanDelegate implements DynamicMBean, XMBeanConstants
{
   private static final Logger log = Logger.getLogger(ServiceMBeanDelegate.class);

   MBeanServer server;
   ServiceContainer container;
   ObjectName serviceOn;
   MBeanInfo mbeanInfo;

   HashMap<String, Method> getterMethods = new HashMap<String, Method>();
   HashSet<String> getterBlackList = new HashSet<String>();
   HashMap<String, Method> setterMethods = new HashMap<String, Method>();
   HashSet<String> setterBlackList = new HashSet<String>();
   HashMap<String, Method> operations = new HashMap<String, Method>();
   HashSet<String> operationBlackList = new HashSet<String>();

   public ServiceMBeanDelegate(MBeanServer server, ServiceContainer container, Class intf, ObjectName on)
   {
      this.container = container;
      this.server = server;
      serviceOn = on;
      StandardMBean mbean = null;
      try
      {
         mbean = new StandardMBean(container.getSingleton(), intf);
      }
      catch (NotCompliantMBeanException e)
      {
         throw new RuntimeException(e);
      }
      mbeanInfo = mbean.getMBeanInfo();
   }

   public ServiceMBeanDelegate(MBeanServer server, ServiceContainer container, String xmbean, ObjectName on)
   {
      this.container = container;
      this.server = server;
      serviceOn = on;
      XMBean mbean = null;
      try
      {
         Descriptor descriptor = new DescriptorSupport();
         descriptor.setField(RESOURCE_REFERENCE, container.getSingleton());
         descriptor.setField(RESOURCE_TYPE, xmbean);
         descriptor.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

         mbean = new XMBean(descriptor, DESCRIPTOR);
      }
      catch (NotCompliantMBeanException e)
      {
         throw new RuntimeException(e);
      }
      catch (javax.management.MBeanException e)
      {
         throw new RuntimeException(e);
      }
      mbeanInfo = mbean.getMetaData();
   }

   public ObjectName getObjectName()
   {
      return serviceOn;
   }

   public void register(ObjectName on, Class intf) throws Exception
   {
      server.registerMBean(this, serviceOn);
   }

   public void unregister() throws Exception
   {
      server.unregisterMBean(serviceOn);
   }

   public Object getAttribute(String attribute) throws AttributeNotFoundException,
                                                       MBeanException, ReflectionException
   {
      Method getter = getGetter(attribute);

      try
      {
         return container.localInvoke(getter, new Object[0]);
      }
      catch (Throwable t)
      {
         if (t instanceof Exception) throw new MBeanException((Exception) t);
         else throw new RuntimeException(t);
      }
   }

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
                                                        InvalidAttributeValueException, MBeanException, ReflectionException
   {
      Method setter = getSetter(attribute);
      try
      {
         container.localInvoke(setter, new Object[]{attribute.getValue()});
      }
      catch (Throwable t)
      {
         if (t instanceof Exception) throw new MBeanException((Exception) t);
         else throw new RuntimeException(t);
      }
   }

   public AttributeList getAttributes(String[] attributes)
   {
      AttributeList list = new AttributeList();

      for (int i = 0; i < attributes.length; i++)
      {
         try
         {
            Object obj = getAttribute(attributes[i]);
            list.add(new Attribute(attributes[i], obj));
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error reading attribute: " + attributes[i], e);
         }
      }
      return list;
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      for (Iterator it = attributes.iterator(); it.hasNext();)
      {
         Attribute attribute = (Attribute) it.next();
         try
         {
            setAttribute(attribute);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error setting attribute: " + attribute, e);
         }
      }
      return attributes;
   }

   public Object invoke(String actionName, Object params[], String signature[])
           throws MBeanException, ReflectionException
   {
      if(log.isTraceEnabled())
         log.trace("invoke: " + actionName);

      try
      {
         // EJBTHREE-655: intercept lifecycle methods
//         if(isMagicLifecycleMethod(actionName))
//         {
//            invokeMagicLifecycleMethod(actionName);
//            return null;
//         }

         Method operation = getOperation(actionName, signature);
         return container.localInvoke(operation, params);
      }
      catch (Throwable t)
      {
         if (t instanceof Exception) throw new MBeanException((Exception) t);
         else throw new RuntimeException(t);
      }
   }

   public MBeanInfo getMBeanInfo()
   {
      return mbeanInfo;
   }

   private String getOperationSignature(String actionName, String[] types)
   {
      //Not really the signature, just something unique
      StringBuffer sig = new StringBuffer();
      sig.append(actionName);

      if (types != null)
      {
         for (int i = 0; i < types.length; i++)
         {
            sig.append(" ");
            sig.append(types[i]);
         }
      }
      return sig.toString();
   }

   private Method getGetter(String attribute) throws AttributeNotFoundException
   {
      Method getter = getterMethods.get(attribute);

      if (getter == null && !getterBlackList.contains(attribute))
      {
         synchronized (getterMethods)
         {
            getter = getterMethods.get(attribute);
            if (getter == null)
            {
               try
               {
                  MBeanAttributeInfo[] attrInfos = mbeanInfo.getAttributes();
                  for (int i = 0; i < attrInfos.length; i++)
                  {
                     MBeanAttributeInfo attrInfo = attrInfos[i];
                     if (attrInfo.getName().equals(attribute))
                     {
                        if (!attrInfo.isReadable())
                        {
                           throw new AttributeNotFoundException("Attribute '" + attribute + "' is not writable in " + container.getBeanClass().getName());
                        }

                        String getterName = ((attrInfo.isIs()) ? "is" : "get") + attribute;
                        getter = container.getBeanClass().getMethod(getterName);
                        getterMethods.put(attribute, getter);
                     }
                  }

                  if (getter == null)
                  {
                     throw new AttributeNotFoundException("No attribute called '" + attribute + "' in " + container.getBeanClass());
                  }
               }
               catch (NoSuchMethodException e)
               {
                  throw new AttributeNotFoundException("Could not find getter for attribute '" + attribute + "' on " + container.getBeanClass().getName());
               }
               finally
               {
                  if (getter == null)
                  {
                     getterBlackList.add(attribute);
                  }
               }
            }
         }
      }

      return getter;
   }

   private Method getSetter(Attribute attribute) throws AttributeNotFoundException
   {
      String attrName = attribute.getName();
      Method setter = setterMethods.get(attrName);

      if (setter == null && !setterBlackList.contains(attrName))
      {
         synchronized (setterMethods)
         {
            setter = setterMethods.get(attrName);
            if (setter == null)
            {
               try
               {
                  MBeanAttributeInfo[] attrInfos = mbeanInfo.getAttributes();
                  for (int i = 0; i < attrInfos.length; i++)
                  {
                     MBeanAttributeInfo attrInfo = attrInfos[i];
                     if (attrInfo.getName().equals(attrName))
                     {
                        if (!attrInfo.isWritable())
                        {
                           throw new AttributeNotFoundException("Attribute '" + attrName + "' is not readable in " + container.getBeanClass().getName());
                        }

                        String setterName = "set" + attrName;
                        Class type = Classes.loadClass(attrInfo.getType());
                        setter = container.getBeanClass().getMethod(setterName, type);
                        setterMethods.put(attrName, setter);
                     }
                  }

                  if (setter == null)
                  {
                     throw new AttributeNotFoundException("No attribute called '" + attribute + "' in " + container.getBeanClass());
                  }
               }
               catch (ClassNotFoundException e)
               {
                  throw new AttributeNotFoundException("Could not load setter type for attribute '" + attrName + "' on " + container.getBeanClass().getName());
               }
               catch (NoSuchMethodException e)
               {
                  throw new AttributeNotFoundException("Could not find setter for attribute '" + attrName + "' on " + container.getBeanClass().getName());
               }
               finally
               {
                  if (setter == null)
                  {
                     setterBlackList.add(attrName);
                  }
               }
            }
         }
      }

      return setter;
   }

   private Method getOperation(String actionName, String[] signature) throws ReflectionException
   {
      String opSig = getOperationSignature(actionName, signature);
      Method operation = operations.get(opSig);

      if (operation == null && !setterBlackList.contains(opSig))
      {
         synchronized (setterMethods)
         {
            operation = operations.get(opSig);
            if (operation == null)
            {
               try
               {
                  MBeanOperationInfo[] opInfos = mbeanInfo.getOperations();
                  for (int i = 0; i < opInfos.length; i++)
                  {
                     MBeanOperationInfo op = opInfos[i];
                     if (op.getName().equals(actionName))
                     {
                        boolean match = true;
                        MBeanParameterInfo[] sigTypes = op.getSignature();
                        if (sigTypes.length == signature.length)
                        {
                           for (int j = 0; j < sigTypes.length; j++)
                           {
                              if (!sigTypes[j].getType().equals(signature[j]))
                              {
                                 match = false;
                                 break;
                              }
                           }
                        }

                        if (match)
                        {
                           Class[] types = null;
                           if (signature.length > 0)
                           {
                              types = new Class[signature.length];
                              for (int j = 0; j < signature.length; j++)
                              {
                                 types[j] = Classes.loadClass(signature[j]);
                              }
                           }
                           else
                           {
                              types = new Class[0];
                           }
                           operation = container.getBeanClass().getMethod(actionName, types);
                           operations.put(opSig, operation);
                        }
                     }
                  }

                  if (operation == null)
                  {
                     throw new RuntimeException("No operation called '" + actionName + "' in " + container.getBeanClass());
                  }

               }
               catch (ClassNotFoundException e)
               {
                  throw new RuntimeException("Could not find  type for operation '" + actionName + "' on " + container.getBeanClass().getName());
               }
               catch (NoSuchMethodException e)
               {
                  throw new RuntimeException("Could not find method for operation '" + actionName + "' on " + container.getBeanClass().getName());
               }
               finally
               {
                  if (operation == null)
                  {
                     operationBlackList.add(opSig);
                  }
               }
            }
         }
      }

      return operation;
   }

   /* EJBTHREE-655 has been postponed
   protected void invokeMagicLifecycleMethod(String operationName) throws Exception
   {
      if(operationName.equals("create"))
         container.create();
      else if(operationName.equals("start"))
         container.start();
      else if(operationName.equals("stop"))
         container.stop();
      else if(operationName.equals("destroy"))
         container.destroy();
      else
         throw new IllegalArgumentException("can't invoke " + operationName);
   }

   protected boolean isMagicLifecycleMethod(String methodName)
   {
      if(methodName.equals("create"))
         return true;
      if(methodName.equals("start"))
         return true;
      if(methodName.equals("stop"))
         return true;
      if(methodName.equals("destroy"))
         return true;
      return false;
   }
   */
}
