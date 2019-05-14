package com.stratis.assignment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import com.stratis.assignment.model.ResidentialProperty;
import java.io.File;
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

  @Value("${default.resident.name}")
  private String defaultResidentialName;

  private ObjectMapper objectMapper;
  private Map<String, ResidentialProperty> residentialPropertyMap;
  private Map<String, People> peopleMap;

  public ResidentialPropertyDataService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.residentialPropertyMap = new HashMap<>();
    this.peopleMap = new HashMap<>();
  }

  public List<People> getAllResidents() {
    ResidentialProperty residentialProperty = residentialPropertyMap.get(defaultResidentialName);
    List<People> residents = residentialProperty.getPeople().stream().collect(Collectors.toList());
    return residents;
  }

  public List<People> getAllResidentsInUnit(String unitNumber) {
    ResidentialProperty residentialProperty = residentialPropertyMap.get(defaultResidentialName);
    List<People> residents =
        residentialProperty.getPeople().stream()
            .filter(
                person -> {
                  return person.getUnit().equalsIgnoreCase(unitNumber);
                })
            .collect(Collectors.toList());
    return residents;
  }

  public void updateResidentInProperty(
      String firstName, String lastName, String unit, boolean isAdmin) throws IOException {
    String fullname = firstName + lastName;
    People resident;

    if (peopleMap.containsKey(fullname)) {
      resident = peopleMap.get(fullname);
      int residentIndex =
          residentialPropertyMap.get(defaultResidentialName).getPeople().indexOf(resident);

      resident.setUnit(unit);
      if (isAdmin) {
        resident.getRoles().add("Admin");
      }

      residentialPropertyMap.get(defaultResidentialName).getPeople().add(residentIndex, resident);

    } else {
      resident = new People();
      List<String> roles = new ArrayList<>();
      roles.add("Resident");
      if (isAdmin) {
        roles.add("Admin");
      }
      resident.setFirst_name(firstName);
      resident.setLast_name(lastName);
      resident.setUnit(unit);
      resident.setRoles(roles);
      residentialPropertyMap.get(defaultResidentialName).getPeople().add(resident);
    }

    peopleMap.put(resident.getFullName(), resident);
    writeResidentialPropertyChangestoFile();
  }

  public void removeResidentFromProperty(String firstName, String lastName, String unit)
      throws IOException {
    String fullname = firstName + lastName;
    People resident;

    if (peopleMap.containsKey(fullname)) {
      resident = peopleMap.get(fullname);
      residentialPropertyMap.get(defaultResidentialName).getPeople().remove(resident);
      writeResidentialPropertyChangestoFile();
    }
  }

  public People getResident(String fullName) {
    return peopleMap.get(fullName);
  }

  public Devices getDevicesForResident(People resident) {
    Devices residentDevices;
    int unitNumber = Integer.valueOf(resident.getUnit());

    if (resident.isAdmin()) {
      residentDevices = getDevicesForAdminResident(unitNumber);
    } else {
      residentDevices = new Devices();
      residentDevices.setLights(
          residentialPropertyMap.get(defaultResidentialName).getDevices().getLights().stream()
              .filter(
                  propertyDevice -> {
                    return propertyDevice.getUnit() == unitNumber;
                  })
              .collect(Collectors.toList()));

      residentDevices.setLocks(
          residentialPropertyMap.get(defaultResidentialName).getDevices().getLocks().stream()
              .filter(
                  propertyDevice -> {
                    return propertyDevice.getUnit() == unitNumber;
                  })
              .collect(Collectors.toList()));

      residentDevices.setThermostats(
          residentialPropertyMap.get(defaultResidentialName).getDevices().getThermostats().stream()
              .filter(
                  propertyDevice -> {
                    return propertyDevice.getUnit() == unitNumber;
                  })
              .collect(Collectors.toList()));
    }

    return residentDevices;
  }

  private Devices getDevicesForAdminResident(int unitNumber) {

    Devices adminResidentDevices = new Devices();

    adminResidentDevices.setLights(
        residentialPropertyMap.get(defaultResidentialName).getDevices().getLights().stream()
            .filter(
                propertyDevice -> {
                  return propertyDevice.getUnit() == unitNumber
                      || propertyDevice.isAdminAccessible();
                })
            .collect(Collectors.toList()));

    adminResidentDevices.setLocks(
        residentialPropertyMap.get(defaultResidentialName).getDevices().getLocks().stream()
            .filter(
                propertyDevice -> {
                  return propertyDevice.getUnit() == unitNumber
                      || propertyDevice.isAdminAccessible();
                })
            .collect(Collectors.toList()));

    adminResidentDevices.setThermostats(
        residentialPropertyMap.get(defaultResidentialName).getDevices().getThermostats().stream()
            .filter(
                propertyDevice -> {
                  return propertyDevice.getUnit() == unitNumber
                      || propertyDevice.isAdminAccessible();
                })
            .collect(Collectors.toList()));

    return adminResidentDevices;
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

  private void writeResidentialPropertyChangestoFile() throws IOException {
    objectMapper.writeValue(
        new File("property_data_changes.json"), residentialPropertyMap.get(defaultResidentialName));
  }
}
