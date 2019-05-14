package com.stratis.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Devices {
  List<PropertyDevice> thermostats;
  List<PropertyDevice> lights;
  List<PropertyDevice> locks;
}
