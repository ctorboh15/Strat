package com.stratis.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyDevice {
  private int id;
  private int unit;
  private String model;
  private String admin_accessible;

  @JsonIgnore
  public Boolean isAdminAccessible() {
    return Boolean.valueOf(admin_accessible);
  }
}
