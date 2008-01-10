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
package org.jboss.ejb3.test.standalone;

import java.util.ArrayList;
import java.util.Hashtable;
import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.ejb3.Container;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateful
public class ShoppingCartBean implements ShoppingCart
{
   private ArrayList<String> cart = new ArrayList<String>();

   private @EJB CalculatorLocal calculator;
   private @EJB CalculatorRemote calculatorRemote;
   private @Resource SessionContext ctx;

   private CalculatorRemote setCalc;

   @EJB
   public void setCalculatorSetter(CalculatorRemote r)
   {
      setCalc = r;
   }

   public ArrayList<String> getCart()
   {
      return cart;
   }

   @Remove
   public void checkout()
   {
      calculator.add(1, 1);
      Hashtable props = new Hashtable();
      props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
      props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.LocalOnlyContextFactory");
      try
      {
         InitialContext initialContext = new InitialContext(props);
         calculator = (CalculatorLocal)initialContext.lookup(Container.ENC_CTX_NAME + "/env/org.jboss.ejb3.test.standalone.ShoppingCartBean/calculator");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      calculator.add(1, 1);
      calculator = (CalculatorLocal)ctx.lookup("org.jboss.ejb3.test.standalone.ShoppingCartBean/calculator");
      calculator.add(1, 1);
      calculatorRemote.add(2, 2);

      setCalc.add(3, 3);


   }
}
