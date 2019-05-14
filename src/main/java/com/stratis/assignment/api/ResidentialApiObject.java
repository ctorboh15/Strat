package com.stratis.assignment.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResidentialApiObject {
  @JsonProperty("resident")
  private People person;

  @JsonProperty("allowed_devices")
  private Devices devices;

  public ResidentialApiObject(People person) {
    this.person = person;
  }

  public ResidentialApiObject(People person, Devices devices) {
    this(person);
    this.devices = devices;
  }
}
