package com.stratis.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** Device data model */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Devices {
  List<PropertyDevice> thermostats;
  List<PropertyDevice> lights;
  List<PropertyDevice> locks;
}
