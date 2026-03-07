package com.example.hrmsclient.dto;

import com.example.hrmsclient.entity.EmploymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeResponseDTO {

    private Long id;
    private String employeeId;
    private String fullName;
    private String emailId;
    private String workEmail;
    private String contactNumber1;
    private String gender;
    private LocalDate dateOfBirth;
    private String nationality;
    private LocalDate joiningDate;
    private String department;
    private String designation;
    private String role;
    private Long basicEmployeeSalary;
    private EmploymentStatus employmentStatus;
    private LocalDate resignationDate;
    private LocalDate lastWorkingDay;
    private String city;
    private String state;
    private String profilePhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String maskedPan;
    private String maskedAadhar;
    private String maskedAccount;
    public EmployeeResponseDTO() {}
    
    public Long getId()                        { return id;                }
    public String getEmployeeId()              { return employeeId;        }
    public String getFullName()                { return fullName;          }
    public String getEmailId()                 { return emailId;           }
    public String getWorkEmail()               { return workEmail;         }
    public String getContactNumber1()          { return contactNumber1;    }
    public String getGender()                  { return gender;            }
    public LocalDate getDateOfBirth()          { return dateOfBirth;       }
    public String getNationality()             { return nationality;       }
    public LocalDate getJoiningDate()          { return joiningDate;       }
    public String getDepartment()              { return department;        }
    public String getDesignation()             { return designation;       }
    public String getRole()                    { return role;              }
    public Long getBasicEmployeeSalary()       { return basicEmployeeSalary; }
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public LocalDate getResignationDate()      { return resignationDate;   }
    public LocalDate getLastWorkingDay()       { return lastWorkingDay;    }
    public String getCity()                    { return city;              }
    public String getState()                   { return state;             }
    public String getProfilePhotoUrl()         { return profilePhotoUrl;   }
    public LocalDateTime getCreatedAt()        { return createdAt;         }
    public LocalDateTime getUpdatedAt()        { return updatedAt;         }
    public String getCreatedBy()               { return createdBy;         }
    public String getMaskedPan()               { return maskedPan;         }
    public String getMaskedAadhar()            { return maskedAadhar;      }
    public String getMaskedAccount()           { return maskedAccount;     }

   
    public void setId(Long id)                                  { this.id                = id;                }
    public void setEmployeeId(String employeeId)                { this.employeeId        = employeeId;        }
    public void setFullName(String fullName)                    { this.fullName          = fullName;          }
    public void setEmailId(String emailId)                      { this.emailId           = emailId;           }
    public void setWorkEmail(String workEmail)                  { this.workEmail         = workEmail;         }
    public void setContactNumber1(String contactNumber1)        { this.contactNumber1    = contactNumber1;    }
    public void setGender(String gender)                        { this.gender            = gender;            }
    public void setDateOfBirth(LocalDate dateOfBirth)           { this.dateOfBirth       = dateOfBirth;       }
    public void setNationality(String nationality)              { this.nationality       = nationality;       }
    public void setJoiningDate(LocalDate joiningDate)           { this.joiningDate       = joiningDate;       }
    public void setDepartment(String department)                { this.department        = department;        }
    public void setDesignation(String designation)              { this.designation       = designation;       }
    public void setRole(String role)                            { this.role              = role;              }
    public void setBasicEmployeeSalary(Long basicEmployeeSalary){ this.basicEmployeeSalary = basicEmployeeSalary; }
    public void setEmploymentStatus(EmploymentStatus status)    { this.employmentStatus  = status;            }
    public void setResignationDate(LocalDate resignationDate)   { this.resignationDate   = resignationDate;   }
    public void setLastWorkingDay(LocalDate lastWorkingDay)     { this.lastWorkingDay    = lastWorkingDay;    }
    public void setCity(String city)                            { this.city              = city;              }
    public void setState(String state)                          { this.state             = state;             }
    public void setProfilePhotoUrl(String profilePhotoUrl)      { this.profilePhotoUrl   = profilePhotoUrl;   }
    public void setCreatedAt(LocalDateTime createdAt)           { this.createdAt         = createdAt;         }
    public void setUpdatedAt(LocalDateTime updatedAt)           { this.updatedAt         = updatedAt;         }
    public void setCreatedBy(String createdBy)                  { this.createdBy         = createdBy;         }
    public void setMaskedPan(String maskedPan)                  { this.maskedPan         = maskedPan;         }
    public void setMaskedAadhar(String maskedAadhar)            { this.maskedAadhar      = maskedAadhar;      }
    public void setMaskedAccount(String maskedAccount)          { this.maskedAccount     = maskedAccount;     }
}