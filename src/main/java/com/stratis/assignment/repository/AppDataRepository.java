package com.stratis.assignment.repository;

import com.stratis.assignment.model.Devices;
import com.stratis.assignment.model.People;
import java.io.IOException;
import java.util.List;

public interface AppDataRepository {
  List<People> findAllResidents(String propertyName);
  List<People> findAllResidentsByUnit(String propertyName, String unitNumber);
  People findResident(String fullName);
  void saveResident(String propertyName, People resident) throws IOException;
  void updateResident(String propertyName, People resident) throws IOException;
  void deleteResident(String propertyName, People resident) throws IOException;
  Devices getDevices(String propertyName);

}
