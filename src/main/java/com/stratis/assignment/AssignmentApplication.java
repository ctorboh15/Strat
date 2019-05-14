package com.stratis.assignment;

import com.stratis.assignment.model.People;
import com.stratis.assignment.model.ResidentialProperty;
import com.stratis.assignment.service.ResidentialPropertyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @GetMapping
  public List<People> test() {
    return residentialPropertyDataService.getPeople();
  }
}
