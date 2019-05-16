package com.stratis.assignment.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import com.stratis.assignment.model.ResidentialProperty;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AppDataRepositoryImpl implements AppDataRepository {
  private ObjectMapper objectMapper;
  private Map<String, ResidentialProperty> residentialPropertyMap;
  private Map<String, People> peopleMap;

  @Value("${resource.filename}")
  private String resourceFileName;

  public AppDataRepositoryImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.residentialPropertyMap = new HashMap<>();
    this.peopleMap = new HashMap<>();
  }

  public List<People> findAllResidents(String propertyName) {
    ResidentialProperty residentialProperty = residentialPropertyMap.get(propertyName);
    List<People> residents = residentialProperty.getPeople().stream().collect(Collectors.toList());
    return residents;
  }

  public List<People> findAllResidentsByUnit(String propertyName, String unitNumber) {
    ResidentialProperty residentialProperty = residentialPropertyMap.get(propertyName);
    List<People> residents =
        residentialProperty.getPeople().stream()
            .filter(person -> person.getUnit().equalsIgnoreCase(unitNumber))
            .collect(Collectors.toList());
    return residents;
  }

  public People findResident(String fullName) {
    return peopleMap.get(fullName);
  }

  public void saveResident(String propertyName, People resident) throws IOException {
    residentialPropertyMap.get(propertyName).getPeople().add(resident);
    peopleMap.put(resident.getFullName(), resident);
    writeResidentialPropertyChangesToFile(propertyName);
  }

  public void updateResident(String propertyName, People resident) throws IOException {
    writeResidentialPropertyChangesToFile(propertyName);
  }

  public void deleteResident(String propertyName, People resident) throws IOException {
    residentialPropertyMap.get(propertyName).getPeople().remove(resident);
    peopleMap.remove(resident.getFullName());
    writeResidentialPropertyChangesToFile(propertyName);
  }

  public Devices getDevices(String propertyName) {
    return residentialPropertyMap.get(propertyName).getDevices();
  }

  @PostConstruct
  private void setUpData() throws IOException {

    if (residentialPropertyMap.isEmpty()) {
      InputStream is = getClass().getResourceAsStream(resourceFileName);
      ResidentialProperty residentialProperty =
          objectMapper.readValue(is, ResidentialProperty.class);
      loadPeopleMap(residentialProperty.getPeople());
      residentialPropertyMap.put(residentialProperty.getName(), residentialProperty);
    }
  }

  private void loadPeopleMap(List<People> peopleList) {
    for (People person : peopleList) {
      peopleMap.put(person.getFullName(), person);
    }
  }

  /**
   * Writes crud operations to the residency to the specified file
   *
   * @throws IOException
   */
  private void writeResidentialPropertyChangesToFile(String propertyName) throws IOException {
    objectMapper.writeValue(
        new File("property_data_changes.json"), residentialPropertyMap.get(propertyName));
  }
}
