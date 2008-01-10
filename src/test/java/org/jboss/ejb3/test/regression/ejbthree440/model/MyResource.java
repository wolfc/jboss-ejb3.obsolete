package org.jboss.ejb3.test.regression.ejbthree440.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * This demonstrates entity inheritance.
 * 
 * @author Ortwin Glück
 */
@Entity
@Table(name="TBRESOURCE2")
@Inheritance
@DiscriminatorValue("M")
@PrimaryKeyJoinColumn(name="resource_id")
public class MyResource extends Resource {
  private String myField;
  
  public MyResource() {
    super();
  }

  public String getMyField() {
    return myField;
  }

  public void setMyField(String myField) {
    this.myField = myField;
  }

}
