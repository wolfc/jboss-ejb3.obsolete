package org.jboss.ejb3.test.regression.ejbthree440.model;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name="TBLOCATION")
@TableGenerator(name="LOC_SEQ")
public class Location implements Serializable {
  private int id, version;
  private String name, description;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Id
  @GeneratedValue(strategy=GenerationType.TABLE, generator="LOC_SEQ")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Version
  @Column(name="versionnr")
  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
