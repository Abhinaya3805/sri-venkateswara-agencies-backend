package com.srivenkateswara.agencies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {

    private Long id;
    private String fullName;
    private String mobileNumber;
    private String houseNumber;
    private String street;
    private String area;
    private String city;
    private String district;
    private String state;
    private String pincode;
    private String landmark;
    private Boolean defaultAddress;
}
