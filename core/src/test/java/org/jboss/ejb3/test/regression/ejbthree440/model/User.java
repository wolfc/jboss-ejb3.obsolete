package org.jboss.ejb3.test.regression.ejbthree440.model;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name="TBUSER")
@TableGenerator(name="USER_SEQ")
public class User implements Serializable {
  private int id, version;
  private boolean active;
  private String name, password;
  private double homeX, homeY;
  private Double posX, posY;

  @Id
  @GeneratedValue(strategy=GenerationType.TABLE, generator="USER_SEQ")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Column(name="homepos_x")
  public double getHomeX() {
    return homeX;
  }

  public void setHomeX(double homeX) {
    this.homeX = homeX;
  }

  @Column(name="homepos_y")
  public double getHomeY() {
    return homeY;
  }

  public void setHomeY(double homeY) {
    this.homeY = homeY;
  }

  @Column(name="actualpos_x")
  public Double getPosX() {
    return posX;
  }

  public void setPosX(Double posX) {
    this.posX = posX;
  }

  @Column(name="actualpos_y")
  public Double getPosY() {
    return posY;
  }

  public void setPosY(Double poxY) {
    this.posY = poxY;
  }

  @Column(name="username")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column(name="passwrd")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
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
