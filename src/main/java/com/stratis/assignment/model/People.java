package com.stratis.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class People {
  private String first_name;
  private String last_name;
  private String unit;
  private List<String> roles;

  @JsonIgnore
  public String getFullName() {
    return first_name.toLowerCase() + last_name.toLowerCase();
  }

  @JsonIgnore
  public boolean isAdmin() {
    return roles.contains("Admin");
  }
}
