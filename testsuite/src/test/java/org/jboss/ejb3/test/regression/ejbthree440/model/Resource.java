package org.jboss.ejb3.test.regression.ejbthree440.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name="TBRESOURCE")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorValue("R")
@DiscriminatorColumn(name="inhtype", length=2)
@TableGenerator(
    name="RES_SEQ"
)
public class Resource implements Serializable{
  private int id, version;
  private Integer region;
  private String description, skills;
  private Date created, updated;
  private Set<Location> locations;
  private Set<ResourceLocation> resourceLocations;
  private Set<Rescalendar> calendars;
  private User user;
  private boolean active;

  @OneToMany(mappedBy="resource")
  public Set<ResourceLocation> getResourceLocations() {
    return resourceLocations;
  }

  public void setResourceLocations(Set<ResourceLocation> resourceLocations) {
    this.resourceLocations = resourceLocations;
  }

  @Column(name="create_dt")
  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  @Column(name="description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Id
  @GeneratedValue(strategy=GenerationType.TABLE, generator="RES_SEQ")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  @Column(name="update_dt")
  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  @Version
  @Column(name="versionnr")
  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public Integer getRegion() {
    return region;
  }

  public void setRegion(Integer region) {
    this.region = region;
  }

  @OneToMany(mappedBy="resource")
  public Set<Rescalendar> getCalendars() {
    return calendars;
  }

  public void setCalendars(Set<Rescalendar> calendars) {
    this.calendars = calendars;
  }

  @ManyToOne(fetch=FetchType.LAZY)
  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
