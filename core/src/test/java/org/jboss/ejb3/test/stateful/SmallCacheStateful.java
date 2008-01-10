package org.jboss.ejb3.test.stateful;

import javax.ejb.Remote;

@Remote
public interface SmallCacheStateful
{
   public void setId(int id);

   public int doit(int id);

}