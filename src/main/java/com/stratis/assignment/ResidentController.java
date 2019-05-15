package com.stratis.assignment;

import com.stratis.assignment.api.ResidentialApiObject;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import com.stratis.assignment.security.JwtUtil;
import com.stratis.assignment.service.ResidentialPropertyDataService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

    if (resident != null) {
      return new ResponseEntity<>(jwtTokenUtil.generateToken(resident), HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @GetMapping("/residence/people")
  public ResponseEntity<List<ResidentialApiObject>> getAllResidents(
      @RequestParam Optional<String> unit) {
    return new ResponseEntity<>(
        unit.map(residentialPropertyDataService::getAllResidentsInUnit)
            .orElse(residentialPropertyDataService.getAllResidents()).stream()
            .map(ResidentialApiObject::new)
            .collect(Collectors.toList()),
        HttpStatus.OK);
  }

  @GetMapping("/residence/people/search")
  public ResponseEntity<ResidentialApiObject> getResident(
      @RequestParam String firstName, @RequestParam String lastName) {
    People resident = getResidentFromDataService(firstName, lastName);

    if (resident != null) {
      Devices devices = residentialPropertyDataService.getDevicesForResident(resident);
      return new ResponseEntity<>(new ResidentialApiObject(resident, devices), HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PostMapping("/residence/unit/{unit}")
  public ResponseEntity createResident(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @PathVariable String unit,
      @RequestParam(defaultValue = "false") String isAdmin) {
    firstName = firstName.toLowerCase();
    lastName = lastName.toLowerCase();

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

  @PutMapping("/residence/unit/{unit}")
  public ResponseEntity<People> updateResident(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @PathVariable String unit,
      @RequestParam(defaultValue = "false") String isAdmin) {
    firstName = firstName.toLowerCase();
    lastName = lastName.toLowerCase();

    try {
      residentialPropertyDataService.updateResidentInProperty(
          firstName, lastName, unit, Boolean.valueOf(isAdmin));
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (EntityNotFoundException ex) {
      return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity(getResidentFromDataService(firstName, lastName), HttpStatus.OK);
  }

  @DeleteMapping("/residence/unit/{unit}")
  public ResponseEntity removeResident(
      @RequestParam String firstName, @RequestParam String lastName, @PathVariable String unit) {
    firstName = firstName.toLowerCase();
    lastName = lastName.toLowerCase();

    try {
      residentialPropertyDataService.removeResidentFromProperty(firstName, lastName, unit);
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity(HttpStatus.OK);
  }

  private People getResidentFromDataService(String firstName, String lastName) {
    String fullName = firstName.toLowerCase() + lastName.toLowerCase();
    return residentialPropertyDataService.getResident(fullName);
  }
}
