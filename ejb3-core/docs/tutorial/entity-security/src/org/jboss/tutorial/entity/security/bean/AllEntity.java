package org.jboss.tutorial.entity.security.bean;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AllEntity implements Serializable
{

   @Id @GeneratedValue(strategy=GenerationType.AUTO)
   public int id;
   
   public String val;
}
