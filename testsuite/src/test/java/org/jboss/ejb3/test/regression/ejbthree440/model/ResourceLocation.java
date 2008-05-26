package org.jboss.ejb3.test.regression.ejbthree440.model;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name="TBRESOURCELOCATION")
@TableGenerator(
    name="RL_SEQ"
)
public class ResourceLocation implements Serializable {
  private int id, version;
  private Resource resource;
  private Location location;
  private int factor;

  public int getFactor() {
    return factor;
  }

  public void setFactor(int factor) {
    this.factor = factor;
  }

  @Id
  @GeneratedValue(strategy=GenerationType.TABLE, generator="RL_SEQ")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @ManyToOne
  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  @ManyToOne
  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Version
  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}
