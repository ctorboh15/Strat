package com.stratis.assignment.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;

/**
 * The thought behind this class is that I wanted to have a way to cleany tie residents to their
 * allowed devices ... By using an api object I can do that and keep a uniform response to clients
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnitApiObject {
  @JsonProperty("resident")
  private People person;

  @JsonProperty("allowed_devices")
  private Devices devices;

  public UnitApiObject(People person) {
    this.person = person;
  }

  public UnitApiObject(People person, Devices devices) {
    this(person);
    this.devices = devices;
  }
}
