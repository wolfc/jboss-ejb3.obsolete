package org.jboss.ejb3.test.regression.ejbthree440.session;

import java.util.Date;
import org.jboss.serial.io.MarshalledObject;
import java.io.IOException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.annotation.ejb.RemoteBinding;

import org.jboss.ejb3.test.regression.ejbthree440.model.MyResource;
import org.jboss.ejb3.test.regression.ejbthree440.model.Resource;
import org.jboss.ejb3.test.regression.ejbthree440.model.User;
import org.jboss.ejb3.test.regression.ejbthree440.session.i.IInheritanceDemo;

@Stateless
@RemoteBinding(jndiBinding="InheritanceDemo/remote")
@Remote(IInheritanceDemo.class)
public class InheritanceDemo implements IInheritanceDemo {
  @PersistenceContext(unitName="mlog")
  protected EntityManager em;

  public void create() {
    User u = new User();
    u.setName("Test User");
    u.setPassword("acuia.sckln");
    u.setActive(false);
    em.persist(u);
    
    MyResource r = new MyResource();
    r.setUser(u);
    r.setSkills("0");
    r.setActive(false);
    r.setDescription("Inheritance Demo Resource");
    r.setMyField("hello world");
    r.setCreated(new Date());
    r.setUpdated(new Date());
    em.persist(r);
  }
  
  public Resource read() {
    Query q = em.createQuery("SELECT u FROM Resource u WHERE u.description = :d");
    q.setParameter("d", "Inheritance Demo Resource");
    Resource r = (Resource) q.getSingleResult();
    return r;
  }

   public MarshalledObject readFromMO()
   {
      try
      {
         return new MarshalledObject(read());
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }
  
  public void remove() {
    Resource r = read();
    em.remove(r);
  }
}
