package com.srivenkateswara.agencies.controller;

import com.srivenkateswara.agencies.dto.AddressDto;
import com.srivenkateswara.agencies.dto.AddressRequest;
import com.srivenkateswara.agencies.dto.ApiResponse;
import com.srivenkateswara.agencies.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Delivery Addresses", description = "User delivery address management endpoints")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get all delivery addresses for logged-in user")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getUserAddresses() {
        List<AddressDto> addresses = addressService.getUserAddresses();
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<ApiResponse<AddressDto>> getAddressById(@PathVariable Long id) {
        AddressDto address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success("Address retrieved successfully", address));
    }

    @PostMapping
    @Operation(summary = "Add a new delivery address")
    public ResponseEntity<ApiResponse<AddressDto>> createAddress(@Valid @RequestBody AddressRequest request) {
        AddressDto address = addressService.createAddress(request);
        return new ResponseEntity<>(ApiResponse.success("Address added successfully", address), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing delivery address")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        AddressDto address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully"));
    }
}
