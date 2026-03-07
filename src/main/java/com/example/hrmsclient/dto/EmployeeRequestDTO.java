package com.example.hrmsclient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class EmployeeRequestDTO {

    @Size(max = 10)
    private String prefix;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Email @NotBlank(message = "Email is required")
    private String emailId;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String contactNumber1;

    private String gender;

    @Past
    private LocalDate dateOfBirth;

    private String nationality;

    @Email
    private String workEmail;

    @PastOrPresent
    private LocalDate joiningDate;

    private String houseNo;
    private String city;
    private String state;
    private String panNumber;
    private String aadharNumber;
    private String passportNumber;
    private String fatherName;
    private String motherName;
    private String maritalStatus;
    private String previousCompanyName;
    private String previousExperience;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Designation is required")
    private String designation;

    private String previousCtc;
    private String higherQualification;

    @Min(0)
    private Long basicEmployeeSalary;

    @NotBlank(message = "Role is required")
    private String role;

    private String bankName;
    private String accountNo;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    private String ifscCode;

    private String bankBranch;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public EmployeeRequestDTO() {}

    public String getPrefix()               { return prefix;               }
    public String getFirstName()            { return firstName;            }
    public String getLastName()             { return lastName;             }
    public String getEmailId()              { return emailId;              }
    public String getContactNumber1()       { return contactNumber1;       }
    public String getGender()               { return gender;               }
    public LocalDate getDateOfBirth()       { return dateOfBirth;          }
    public String getNationality()          { return nationality;          }
    public String getWorkEmail()            { return workEmail;            }
    public LocalDate getJoiningDate()       { return joiningDate;          }
    public String getHouseNo()              { return houseNo;              }
    public String getCity()                 { return city;                 }
    public String getState()               { return state;                }
    public String getPanNumber()            { return panNumber;            }
    public String getAadharNumber()         { return aadharNumber;         }
    public String getPassportNumber()       { return passportNumber;       }
    public String getFatherName()           { return fatherName;           }
    public String getMotherName()           { return motherName;           }
    public String getMaritalStatus()        { return maritalStatus;        }
    public String getPreviousCompanyName()  { return previousCompanyName;  }
    public String getPreviousExperience()   { return previousExperience;   }
    public String getDepartment()           { return department;           }
    public String getDesignation()          { return designation;          }
    public String getPreviousCtc()          { return previousCtc;          }
    public String getHigherQualification()  { return higherQualification;  }
    public Long getBasicEmployeeSalary()    { return basicEmployeeSalary;  }
    public String getRole()                 { return role;                 }
    public String getBankName()             { return bankName;             }
    public String getAccountNo()            { return accountNo;            }
    public String getIfscCode()             { return ifscCode;             }
    public String getBankBranch()           { return bankBranch;           }
    public String getPassword()             { return password;             }
    
    public void setPrefix(String prefix)                          { this.prefix              = prefix;              }
    public void setFirstName(String firstName)                    { this.firstName           = firstName;           }
    public void setLastName(String lastName)                      { this.lastName            = lastName;            }
    public void setEmailId(String emailId)                        { this.emailId             = emailId;             }
    public void setContactNumber1(String contactNumber1)          { this.contactNumber1      = contactNumber1;      }
    public void setGender(String gender)                          { this.gender              = gender;              }
    public void setDateOfBirth(LocalDate dateOfBirth)             { this.dateOfBirth         = dateOfBirth;         }
    public void setNationality(String nationality)                { this.nationality         = nationality;         }
    public void setWorkEmail(String workEmail)                    { this.workEmail           = workEmail;           }
    public void setJoiningDate(LocalDate joiningDate)             { this.joiningDate         = joiningDate;         }
    public void setHouseNo(String houseNo)                        { this.houseNo             = houseNo;             }
    public void setCity(String city)                              { this.city                = city;                }
    public void setState(String state)                            { this.state               = state;               }
    public void setPanNumber(String panNumber)                    { this.panNumber           = panNumber;           }
    public void setAadharNumber(String aadharNumber)              { this.aadharNumber        = aadharNumber;        }
    public void setPassportNumber(String passportNumber)          { this.passportNumber      = passportNumber;      }
    public void setFatherName(String fatherName)                  { this.fatherName          = fatherName;          }
    public void setMotherName(String motherName)                  { this.motherName          = motherName;          }
    public void setMaritalStatus(String maritalStatus)            { this.maritalStatus       = maritalStatus;       }
    public void setPreviousCompanyName(String previousCompanyName){ this.previousCompanyName = previousCompanyName; }
    public void setPreviousExperience(String previousExperience)  { this.previousExperience  = previousExperience;  }
    public void setDepartment(String department)                  { this.department          = department;          }
    public void setDesignation(String designation)                { this.designation         = designation;         }
    public void setPreviousCtc(String previousCtc)                { this.previousCtc         = previousCtc;         }
    public void setHigherQualification(String hq)                 { this.higherQualification = hq;                  }
    public void setBasicEmployeeSalary(Long basicEmployeeSalary)  { this.basicEmployeeSalary = basicEmployeeSalary; }
    public void setRole(String role)                              { this.role                = role;                }
    public void setBankName(String bankName)                      { this.bankName            = bankName;            }
    public void setAccountNo(String accountNo)                    { this.accountNo           = accountNo;           }
    public void setIfscCode(String ifscCode)                      { this.ifscCode            = ifscCode;            }
    public void setBankBranch(String bankBranch)                  { this.bankBranch          = bankBranch;          }
    public void setPassword(String password)                      { this.password            = password;            }
}