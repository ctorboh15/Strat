package com.stratis.assignment;

import com.stratis.assignment.api.ResidentialApiObject;
import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import com.stratis.assignment.model.ResidentialProperty;
import com.stratis.assignment.service.ResidentialPropertyDataService;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class AssignmentApplication {

  public static void main(String[] args) {
    SpringApplication.run(AssignmentApplication.class, args);
  }

  @Autowired private ResidentialPropertyDataService residentialPropertyDataService;

  @GetMapping("/residents/unit")
  public ResponseEntity<List<ResidentialApiObject>> getAllResidents(@RequestParam Optional<String> unitNumber) {
    return new ResponseEntity<>(
        unitNumber.map(residentialPropertyDataService::getAllResidentsInUnit)
            .orElse(residentialPropertyDataService.getAllResidents()).stream()
            .map(ResidentialApiObject::new)
            .collect(Collectors.toList()),
        HttpStatus.OK);
  }

  @GetMapping("/residents")
  public ResponseEntity<ResidentialApiObject> getResident(@RequestParam String firstName,
      @RequestParam String lastName) {
    String fullname = firstName+lastName;
    People resident = residentialPropertyDataService.getResident(fullname);

    if (resident != null) {
      Devices devices = residentialPropertyDataService.getDevicesForResident(resident);
      return new ResponseEntity<>(new ResidentialApiObject(resident, devices), HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PostMapping("/residents/{unit}")
  public ResponseEntity updateResident(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @PathVariable String unit,
      @RequestParam(defaultValue = "false") String isAdmin) {

    try {
      residentialPropertyDataService.updateResidentInProperty(
          firstName, lastName, unit, Boolean.valueOf(isAdmin));
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity(HttpStatus.CREATED);
  }

  @DeleteMapping("/residents/{unit}")
  public ResponseEntity removeResident(
      @RequestParam String firstName, @RequestParam String lastName, @PathVariable String unit) {

    try {
      residentialPropertyDataService.removeResidentFromProperty(firstName, lastName, unit);
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity(HttpStatus.CREATED);
  }
}
