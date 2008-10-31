package org.jboss.ejb3.test.regression.ejbthree440.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="TBCALENDAR")
@TableGenerator(
    name="CAL_SEQ"
)
public class Rescalendar implements Serializable {
  private int id, version;
  private Resource resource;
  private Date start, end;
  private int status;
  private boolean active;

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Column(name="end_dt")
  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  @Id
  @GeneratedValue(strategy=GenerationType.TABLE, generator="CAL_SEQ")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @ManyToOne
  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Column(name="start_dt")
  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  @Version
  @Column(name="versionnr")
  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
}
