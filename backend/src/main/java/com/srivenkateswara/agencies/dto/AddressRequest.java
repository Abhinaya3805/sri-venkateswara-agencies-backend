package com.srivenkateswara.agencies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid 10-digit Indian mobile number")
    private String mobileNumber;

    @NotBlank(message = "House/Flat number is required")
    private String houseNumber;

    @NotBlank(message = "Street name is required")
    private String street;

    private String area;

    @NotBlank(message = "City is required")
    private String city;

    private String district;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid 6-digit Indian PIN code")
    private String pincode;

    private String landmark;

    private Boolean defaultAddress;
}
