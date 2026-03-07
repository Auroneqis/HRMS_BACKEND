package com.example.hrmsclient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CheckInRequestDTO {

    //  Frontend uploads photo first → gets URL → sends URL here
    @NotBlank(message = "Login photo URL is required for check-in")
    private String loginPhotoUrl;

    //  GPS Location — required
    @NotNull(message = "Latitude is required for check-in")
    private Double latitude;

    @NotNull(message = "Longitude is required for check-in")
    private Double longitude;
    private String address;
    private String remarks;

    // Getters & Setters
    public String getLoginPhotoUrl()                   { return loginPhotoUrl; }
    public void setLoginPhotoUrl(String loginPhotoUrl) { this.loginPhotoUrl = loginPhotoUrl; }

    public Double getLatitude()                { return latitude; }
    public void setLatitude(Double latitude)   { this.latitude = latitude; }

    public Double getLongitude()               { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress()                 { return address; }
    public void setAddress(String address)     { this.address = address; }

    public String getRemarks()                 { return remarks; }
    public void setRemarks(String remarks)     { this.remarks = remarks; }
}