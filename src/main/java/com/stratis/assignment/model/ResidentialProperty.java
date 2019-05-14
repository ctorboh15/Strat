package com.stratis.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResidentialProperty {
  private String name;
  private Address address;
  private Devices devices;
  private List<People> people;
}
