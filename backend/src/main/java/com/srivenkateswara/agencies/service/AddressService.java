package com.srivenkateswara.agencies.service;

import com.srivenkateswara.agencies.dto.AddressDto;
import com.srivenkateswara.agencies.dto.AddressRequest;
import com.srivenkateswara.agencies.entity.Address;
import com.srivenkateswara.agencies.entity.User;
import com.srivenkateswara.agencies.exception.ResourceNotFoundException;
import com.srivenkateswara.agencies.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses() {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        return addressRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToAddressDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressDto getAddressById(Long id) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));
        return mapToAddressDto(address);
    }

    @Transactional
    public AddressDto createAddress(AddressRequest request) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();

        boolean isFirstAddress = addressRepository.findByUserId(currentUser.getId()).isEmpty();
        boolean setAsDefault = (request.getDefaultAddress() != null && request.getDefaultAddress()) || isFirstAddress;

        if (setAsDefault) {
            unsetOtherDefaultAddresses(currentUser.getId());
        }

        Address address = Address.builder()
                .user(currentUser)
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .houseNumber(request.getHouseNumber())
                .street(request.getStreet())
                .area(request.getArea())
                .city(request.getCity())
                .district(request.getDistrict())
                .state(request.getState())
                .pincode(request.getPincode())
                .landmark(request.getLandmark())
                .defaultAddress(setAsDefault)
                .build();

        Address savedAddress = addressRepository.save(address);
        return mapToAddressDto(savedAddress);
    }

    @Transactional
    public AddressDto updateAddress(Long id, AddressRequest request) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        if (request.getDefaultAddress() != null && request.getDefaultAddress()) {
            unsetOtherDefaultAddresses(currentUser.getId());
            address.setDefaultAddress(true);
        }

        address.setFullName(request.getFullName());
        address.setMobileNumber(request.getMobileNumber());
        address.setHouseNumber(request.getHouseNumber());
        address.setStreet(request.getStreet());
        address.setArea(request.getArea());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLandmark(request.getLandmark());

        Address updatedAddress = addressRepository.save(address);
        return mapToAddressDto(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long id) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        addressRepository.delete(address);
    }

    private void unsetOtherDefaultAddresses(Long userId) {
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        for (Address addr : userAddresses) {
            if (Boolean.TRUE.equals(addr.getDefaultAddress())) {
                addr.setDefaultAddress(false);
                addressRepository.save(addr);
            }
        }
    }

    public AddressDto mapToAddressDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .mobileNumber(address.getMobileNumber())
                .houseNumber(address.getHouseNumber())
                .street(address.getStreet())
                .area(address.getArea())
                .city(address.getCity())
                .district(address.getDistrict())
                .state(address.getState())
                .pincode(address.getPincode())
                .landmark(address.getLandmark())
                .defaultAddress(address.getDefaultAddress())
                .build();
    }
}
