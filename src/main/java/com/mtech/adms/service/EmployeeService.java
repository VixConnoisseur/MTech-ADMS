package com.mtech.adms.service;

import com.mtech.adms.dao.EmployeeDao;
import com.mtech.adms.exception.RecordNotFoundException;
import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Employee;

import java.util.List;

public class EmployeeService {

    private final EmployeeDao employeeDao;

    public EmployeeService() {
        this.employeeDao = new EmployeeDao();
    }

    public List<Employee> getAll() {
        return employeeDao.findAll();
    }

    public List<Employee> search(String keyword) {
        return employeeDao.search(keyword);
    }

    public Employee getById(int id) {
        return employeeDao.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Employee not found: id " + id));
    }

    public Employee create(Employee employee) {
        validate(employee);

        employee.setEmployeeCode(employeeDao.getNextEmployeeCode());
        employee.setActive(true);

        return employeeDao.insert(employee);
    }

    public void update(Employee employee) {
        validate(employee);

        if (employee.getId() == null) {
            throw new ValidationException("Cannot update an employee without an ID.");
        }
        employeeDao.update(employee);
    }

    public void setActive(int id, boolean active) {
        Employee employee = getById(id);
        employee.setActive(active);
        employeeDao.update(employee);
    }

    private void validate(Employee employee) {
        if (employee.getFullName() == null || employee.getFullName().isBlank()) {
            throw new ValidationException("Full name is required.");
        }
        if (employee.getFullName().trim().length() < 2) {
            throw new ValidationException("Full name must be at least 2 characters.");
        }
    }
}