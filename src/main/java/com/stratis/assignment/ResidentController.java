package com.stratis.assignment;

import com.stratis.assignment.api.ResidentialApiObject;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import com.stratis.assignment.security.JwtUtil;
import com.stratis.assignment.service.ResidentialPropertyDataService;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

/**
 * Rest Controller for interacting with the residence service... I know I should send a payload for
 * non GET requests but because of constraints I added them to PUT and POST requests as a form of
 * "validation"
 */
@RestController
@RequestMapping("/")
public class ResidentController {
  @Autowired private ResidentialPropertyDataService residentialPropertyDataService;
  @Autowired private JwtUtil jwtTokenUtil;

  @GetMapping("/login")
  public ResponseEntity<String> getUserJwt(
      @RequestParam String firstName, @RequestParam String lastName) {
    People resident = getResidentFromDataService(firstName, lastName);

    if (resident != null && resident.isAdmin()) {
      return new ResponseEntity<>(jwtTokenUtil.generateToken(resident), HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
  }

  /**
   * Returns all residents for the property
   *
   * @return
   */
  @GetMapping("/residence/people")
  public ResponseEntity<List<ResidentialApiObject>> getAllResidents() {
    return new ResponseEntity<>(
        residentialPropertyDataService.getAllResidents().stream()
            .map(ResidentialApiObject::new)
            .collect(Collectors.toList()),
        HttpStatus.OK);
  }

  /**
   * Searches for a resident given a first and last name.
   *
   * @param firstName
   * @param lastName
   * @return
   */
  @GetMapping("/residence/people/search")
  public ResponseEntity<ResidentialApiObject> searchForResident(
      @RequestParam String firstName, @RequestParam String lastName) {
    People resident = getResidentFromDataService(firstName, lastName);

    if (resident != null) {
      Devices devices = residentialPropertyDataService.getDevicesForResident(resident);
      return new ResponseEntity<>(new ResidentialApiObject(resident, devices), HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Create a new residence for the property. By default all new residents will not have admin
   * access and will have to be granted explicitly
   *
   * @param firstName
   * @param lastName
   * @param unit
   * @param isAdmin
   * @return
   */
  @PostMapping("/residence/people")
  public ResponseEntity createResident(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam String unit,
      @RequestParam(defaultValue = "false") String isAdmin) {

    try {
      residentialPropertyDataService.createResidentInProperty(
          firstName, lastName, unit, Boolean.valueOf(isAdmin));
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (EntityExistsException ex) {
      return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity(HttpStatus.CREATED);
  }

  /**
   * Endpoint method for updating a resident. One thing to keep in mind is that we explicitly set
   * the admin flag to false. Purpose being is that when the client updates a user... they must
   * explicitly continuously give the resident admin privileges.
   *
   * @param firstName
   * @param lastName
   * @param unit
   * @param isAdmin
   * @return
   */
  @PutMapping("/residence/people")
  public ResponseEntity<People> updateResident(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam String unit,
      @RequestParam(defaultValue = "false") String isAdmin) {

    try {
      residentialPropertyDataService.updateResidentInProperty(
          firstName.toLowerCase(), lastName.toLowerCase(), unit, Boolean.valueOf(isAdmin));
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (EntityNotFoundException ex) {
      return new ResponseEntity("No Resident found", HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity(getResidentFromDataService(firstName, lastName), HttpStatus.OK);
  }

  /**
   * Removes resident from property... requires the unit number as a extra step to ensure the client
   * knows who they are removing
   *
   * @param firstName
   * @param lastName
   * @param unit
   * @return
   */
  @DeleteMapping("/residence/people")
  public ResponseEntity removeResident(
      @RequestParam String firstName, @RequestParam String lastName, @RequestParam String unit) {
    People resident = getResidentFromDataService(firstName, lastName);
    if (resident != null && resident.getUnit().equals(unit)) {
      try {
        residentialPropertyDataService.removeResidentFromProperty(resident);
      } catch (IOException e) {
        e.printStackTrace();
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
      }

      return new ResponseEntity(HttpStatus.OK);
    }
    return new ResponseEntity("No Resident found for specified unit", HttpStatus.BAD_REQUEST);
  }

  /**
   * Gets all residents for a specified unit
   *
   * @param unit
   * @return
   */
  @GetMapping("/residence/unit/{unit}")
  public ResponseEntity<List<People>> getResidentsForUnit(@PathVariable String unit) {

    return new ResponseEntity(
        residentialPropertyDataService.getAllResidentsInUnit(unit), HttpStatus.OK);
  }

  private People getResidentFromDataService(String firstName, String lastName) {
    String fullName = firstName.toLowerCase() + lastName.toLowerCase();
    return residentialPropertyDataService.getResident(fullName);
  }
}
