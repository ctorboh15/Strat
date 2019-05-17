package com.stratis.assignment.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResidentApiObject {

  @NotBlank(message = "First name is invalid")
  private String first_name;

  @NotBlank(message = "Last name is invalid")
  private String last_name;

  @JsonProperty private String unit;

  @JsonProperty private boolean isAdmin = false;
}
