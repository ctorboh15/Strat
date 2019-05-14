package com.stratis.assignment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratis.assignment.model.People;
import com.stratis.assignment.model.ResidentialProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResidentialPropertyDataService {

  @Value("${resource.filename}")
  private String resourceFileName;

  private ObjectMapper objectMapper;
  private Map<String, ResidentialProperty> residentialPropertyMap;
  private Map<String, People> peopleMap;

  public ResidentialPropertyDataService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.residentialPropertyMap = new HashMap<>();
    this.peopleMap = new HashMap<>();
  }

  @PostConstruct
  private void setUpData() throws IOException {

    if (residentialPropertyMap.isEmpty()) {
      InputStream is = getClass().getResourceAsStream(resourceFileName);
      ResidentialProperty residentialProperty =
          objectMapper.readValue(is, ResidentialProperty.class);
      loadPeopleForSignIn(residentialProperty.getPeople());
      residentialPropertyMap.put(
          residentialProperty.getAddress().getAddress_line_1(), residentialProperty);
    }
  }

  private void loadPeopleForSignIn(List<People> peopleList) {
    for (People person : peopleList) {
      peopleMap.put(person.getFullName(), person);
    }
  }

  public List<People> getResidents(String address, String unitNumber) {
    ResidentialProperty residentialProperty = residentialPropertyMap.get(address);
    List<People> residents =
        residentialProperty.getPeople().stream()
            .filter(
                person -> {
                  return person.getUnit().equalsIgnoreCase(unitNumber);
                })
            .collect(Collectors.toList());
    return residents;
  }
}
