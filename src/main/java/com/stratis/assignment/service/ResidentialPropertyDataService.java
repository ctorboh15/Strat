package com.stratis.assignment.service;

import com.stratis.assignment.repository.AppDataRepository;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Data Service for CRUD operations on residence data */
@Component
public class ResidentialPropertyDataService {

  @Value("${init.resource.filename}")
  private String resourceFileName;

  @Value("${default.resident.name}")
  private String defaultPropertyName;

  private AppDataRepository appDataRepository;

  public ResidentialPropertyDataService(AppDataRepository appDataRepository) {
    this.appDataRepository = appDataRepository;
  }

  /**
   * returns all residents in the property
   *
   * @return
   */
  public List<People> getAllResidents() {
    return appDataRepository.findAllResidents(defaultPropertyName);
  }

  /**
   * Gets all residents within a unit
   *
   * @param unitNumber
   * @return
   */
  public List<People> getAllResidentsInUnit(String unitNumber) {
    return appDataRepository.findAllResidentsByUnit(defaultPropertyName, unitNumber);
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

    People resident = appDataRepository.findResident(fullName);

    if (resident == null) {
      throw new EntityNotFoundException("No resident found for this unit");
    }

    resident.setUnit(unit);
    resident.toggleAdminFlag(isAdmin);

    appDataRepository.updateResident(defaultPropertyName, resident);
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
    fullName = fullName.toLowerCase();

    People resident = appDataRepository.findResident(fullName);

    if (resident != null && resident.getUnit().equals(unit)) {
      throw new EntityExistsException("A resident with this name already exists for this unit");
    }
    resident = new People();
    List<String> roles = new ArrayList<>();
    roles.add("Resident");
    resident.setFirst_name(firstName);
    resident.setLast_name(lastName);
    resident.setUnit(unit);
    resident.setRoles(roles);

    resident.toggleAdminFlag(isAdmin);

    appDataRepository.saveResident(defaultPropertyName, resident);
  }

  /**
   * Removes a resident from the existing Residential Property
   *
   * @param resident
   * @throws IOException
   */
  public void removeResidentFromProperty(People resident) throws IOException {
    appDataRepository.deleteResident(defaultPropertyName, resident);
  }

  /**
   * Get a list of devices a resident has access to based on their full name
   *
   * @param fullName
   * @return
   */
  public People getResident(String fullName) {
    return appDataRepository.findResident(fullName);
  }

  public Devices getDevicesForResident(People resident) {
    Devices residentDevices;
    int unitNumber = Integer.valueOf(resident.getUnit());

    if (resident.isAdmin()) {
      residentDevices = getDevicesForAdminResident(unitNumber);
    } else {
      residentDevices = new Devices();
      residentDevices.setLights(
          appDataRepository.getDevices(defaultPropertyName).getLights().stream()
              .filter(propertyDevice -> propertyDevice.getUnit() == unitNumber)
              .collect(Collectors.toList()));

      residentDevices.setLocks(
          appDataRepository.getDevices(defaultPropertyName).getLocks().stream()
              .filter(propertyDevice -> propertyDevice.getUnit() == unitNumber)
              .collect(Collectors.toList()));

      residentDevices.setThermostats(
          appDataRepository.getDevices(defaultPropertyName).getThermostats().stream()
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
        appDataRepository.getDevices(defaultPropertyName).getLights().stream()
            .filter(
                propertyDevice ->
                    propertyDevice.getUnit() == unitNumber || propertyDevice.isAdminAccessible())
            .collect(Collectors.toList()));

    adminResidentDevices.setLocks(
        appDataRepository.getDevices(defaultPropertyName).getLocks().stream()
            .filter(
                propertyDevice ->
                    propertyDevice.getUnit() == unitNumber || propertyDevice.isAdminAccessible())
            .collect(Collectors.toList()));

    adminResidentDevices.setThermostats(
        appDataRepository.getDevices(defaultPropertyName).getThermostats().stream()
            .filter(
                propertyDevice ->
                    propertyDevice.getUnit() == unitNumber || propertyDevice.isAdminAccessible())
            .collect(Collectors.toList()));

    return adminResidentDevices;
  }
}
