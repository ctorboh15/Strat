package com.stratis.assignment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import com.stratis.assignment.model.ResidentialProperty;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Data Service for retrieving and persisting resident data */
@Component
public class ResidentialPropertyDataService {

  @Value("${resource.filename}")
  private String resourceFileName;

  @Value("${default.resident.name}")
  private String defaultPropertyName;

  private ObjectMapper objectMapper;
  private Map<String, ResidentialProperty> residentialPropertyMap;
  private Map<String, People> peopleMap;

  public ResidentialPropertyDataService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.residentialPropertyMap = new HashMap<>();
    this.peopleMap = new HashMap<>();
  }

  /**
   * returns all residents in the property
   *
   * @return
   */
  public List<People> getAllResidents() {
    ResidentialProperty residentialProperty = residentialPropertyMap.get(defaultPropertyName);
    List<People> residents = residentialProperty.getPeople().stream().collect(Collectors.toList());
    return residents;
  }

  /**
   * Gets all residents within a unit
   *
   * @param unitNumber
   * @return
   */
  public List<People> getAllResidentsInUnit(String unitNumber) {
    ResidentialProperty residentialProperty = residentialPropertyMap.get(defaultPropertyName);
    List<People> residents =
        residentialProperty.getPeople().stream()
            .filter(person -> person.getUnit().equalsIgnoreCase(unitNumber))
            .collect(Collectors.toList());
    return residents;
  }

  /**
   * Updates an existing resident for the property
   *
   * @param firstName
   * @param lastName
   * @param unit
   * @param isAdmin
   * @throws IOException
   */
  public void updateResidentInProperty(
      String firstName, String lastName, String unit, boolean isAdmin) throws IOException {
    String fullName = firstName + lastName;

    if (!peopleMap.containsKey(fullName)) {
      throw new EntityNotFoundException("No resident found for this unit");
    }

    People resident = peopleMap.get(fullName);

    resident.setUnit(unit);
    resident.toggleAdminFlag(isAdmin);

    writeResidentialPropertyChangestoFile();
  }

  /**
   * Creates a new resident for a property
   *
   * @param firstName
   * @param lastName
   * @param unit
   * @param isAdmin
   * @throws IOException
   */
  public void createResidentInProperty(
      String firstName, String lastName, String unit, boolean isAdmin) throws IOException {
    String fullName = firstName + lastName;
    People resident;

    if (peopleMap.containsKey(fullName)) {
      resident = peopleMap.get(fullName);
      if (resident.getUnit() != unit) {
        throw new EntityExistsException("A resident with this name already exists for this unit");
      }
    }
    resident = new People();
    List<String> roles = new ArrayList<>();
    roles.add("Resident");
    resident.setFirst_name(firstName);
    resident.setLast_name(lastName);
    resident.setUnit(unit);
    resident.setRoles(roles);

    resident.toggleAdminFlag(isAdmin);
    residentialPropertyMap.get(defaultPropertyName).getPeople().add(resident);

    peopleMap.put(resident.getFullName(), resident);
    writeResidentialPropertyChangestoFile();
  }

  /**
   * Removes a resident from the existing Residential Property
   *
   * @param firstName
   * @param lastName
   * @param unit
   * @throws IOException
   */
  public void removeResidentFromProperty(String firstName, String lastName, String unitToRemoveFrom)
      throws IOException {
    String fullname = firstName + lastName;
    People resident;

    if (peopleMap.containsKey(fullname)) {
      resident = peopleMap.get(fullname);

      if (resident.getUnit().equalsIgnoreCase(unitToRemoveFrom)) {
        residentialPropertyMap.get(defaultPropertyName).getPeople().remove(resident);
        peopleMap.remove(resident);
        writeResidentialPropertyChangestoFile();
      }
    }
  }

  /**
   * Get a list of devices a resident has access to based on their full name
   *
   * @param fullName
   * @return
   */
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
          residentialPropertyMap.get(defaultPropertyName).getDevices().getLights().stream()
              .filter(propertyDevice -> propertyDevice.getUnit() == unitNumber)
              .collect(Collectors.toList()));

      residentDevices.setLocks(
          residentialPropertyMap.get(defaultPropertyName).getDevices().getLocks().stream()
              .filter(propertyDevice -> propertyDevice.getUnit() == unitNumber)
              .collect(Collectors.toList()));

      residentDevices.setThermostats(
          residentialPropertyMap.get(defaultPropertyName).getDevices().getThermostats().stream()
              .filter(propertyDevice -> propertyDevice.getUnit() == unitNumber)
              .collect(Collectors.toList()));
    }

    return residentDevices;
  }

  /**
   * Return a list of devices for Admins
   *
   * @param unitNumber
   * @return
   */
  private Devices getDevicesForAdminResident(int unitNumber) {

    Devices adminResidentDevices = new Devices();

    adminResidentDevices.setLights(
        residentialPropertyMap.get(defaultPropertyName).getDevices().getLights().stream()
            .filter(
                propertyDevice ->
                    propertyDevice.getUnit() == unitNumber || propertyDevice.isAdminAccessible())
            .collect(Collectors.toList()));

    adminResidentDevices.setLocks(
        residentialPropertyMap.get(defaultPropertyName).getDevices().getLocks().stream()
            .filter(
                propertyDevice ->
                    propertyDevice.getUnit() == unitNumber || propertyDevice.isAdminAccessible())
            .collect(Collectors.toList()));

    adminResidentDevices.setThermostats(
        residentialPropertyMap.get(defaultPropertyName).getDevices().getThermostats().stream()
            .filter(
                propertyDevice ->
                    propertyDevice.getUnit() == unitNumber || propertyDevice.isAdminAccessible())
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

  /**
   * Writes crud operations to the residency to the specified file
   *
   * @throws IOException
   */
  private void writeResidentialPropertyChangestoFile() throws IOException {
    objectMapper.writeValue(
        new File("property_data_changes.json"), residentialPropertyMap.get(defaultPropertyName));
  }
}
