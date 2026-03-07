package com.example.hrmsclient.service;

import com.example.hrmsclient.dto.EmployeeRequestDTO;
import com.example.hrmsclient.dto.EmployeeResponseDTO;
import com.example.hrmsclient.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    //  DTO → Entity
    public Employee toEntity(EmployeeRequestDTO dto) {
        Employee e = new Employee();
        e.setPrefix(dto.getPrefix());
        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setEmailId(dto.getEmailId());
        e.setContactNumber1(dto.getContactNumber1());
        e.setGender(dto.getGender());
        e.setDateOfBirth(dto.getDateOfBirth());
        e.setNationality(dto.getNationality());
        e.setWorkEmail(dto.getWorkEmail());
        e.setJoiningDate(dto.getJoiningDate());
        e.setHouseNo(dto.getHouseNo());
        e.setCity(dto.getCity());
        e.setState(dto.getState());
        e.setPanNumber(dto.getPanNumber());
        e.setAadharNumber(dto.getAadharNumber());
        e.setPassportNumber(dto.getPassportNumber());
        e.setFatherName(dto.getFatherName());
        e.setMotherName(dto.getMotherName());
        e.setMaritalStatus(dto.getMaritalStatus());
        e.setPreviousCompanyName(dto.getPreviousCompanyName());
        e.setPreviousExperience(dto.getPreviousExperience());
        e.setDepartment(dto.getDepartment());
        e.setDesignation(dto.getDesignation());
        e.setPreviousCtc(dto.getPreviousCtc());
        e.setHigherQualification(dto.getHigherQualification());
        e.setBasicEmployeeSalary(dto.getBasicEmployeeSalary());
        e.setRole(dto.getRole());
        e.setBankName(dto.getBankName());
        e.setAccountNo(dto.getAccountNo());
        e.setIfscCode(dto.getIfscCode());
        e.setBankBranch(dto.getBankBranch());
        return e;
    }

    //  Entity → Response DTO
    public EmployeeResponseDTO toResponse(Employee e) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(e.getId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setFullName(e.getFullName());
        dto.setEmailId(e.getEmailId());
        dto.setWorkEmail(e.getWorkEmail());
        dto.setContactNumber1(e.getContactNumber1());
        dto.setGender(e.getGender());
        dto.setDateOfBirth(e.getDateOfBirth());
        dto.setNationality(e.getNationality());
        dto.setJoiningDate(e.getJoiningDate());
        dto.setDepartment(e.getDepartment());
        dto.setDesignation(e.getDesignation());
        dto.setRole(e.getRole());
        dto.setBasicEmployeeSalary(e.getBasicEmployeeSalary());
        dto.setEmploymentStatus(e.getEmploymentStatus());
        dto.setResignationDate(e.getResignationDate());
        dto.setLastWorkingDay(e.getLastWorkingDay());
        dto.setCity(e.getCity());
        dto.setState(e.getState());
        dto.setProfilePhotoUrl(e.getProfilePhotoUrl());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setMaskedPan(mask(e.getPanNumber(), 5));
        dto.setMaskedAadhar(mask(e.getAadharNumber(), 8));
        dto.setMaskedAccount(mask(e.getAccountNo(), 8));
        return dto;
    }

    // Update existing entity from DTO
    public void updateEntity(Employee e, EmployeeRequestDTO dto) {
        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setContactNumber1(dto.getContactNumber1());
        e.setDepartment(dto.getDepartment());
        e.setDesignation(dto.getDesignation());
        e.setCity(dto.getCity());
        e.setState(dto.getState());
        e.setBasicEmployeeSalary(dto.getBasicEmployeeSalary());
        e.setRole(dto.getRole());
        e.setBankName(dto.getBankName());
        e.setIfscCode(dto.getIfscCode());
        e.setBankBranch(dto.getBankBranch());
    }
    private String mask(String value, int visibleFromEnd) {
        if (value == null || value.isBlank()) return null;
        if (value.length() <= visibleFromEnd)  return value;
        return "X".repeat(value.length() - visibleFromEnd)
             + value.substring(value.length() - visibleFromEnd);
    }
}