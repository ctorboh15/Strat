package com.stratis.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Address Data model */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
  private String address_line_1;
  private String city;
  private String state;
  private String zip;
}
