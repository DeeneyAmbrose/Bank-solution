package com.customer_service.customer_service.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CustomerDto customer) {
        var response= customerService.create(customer);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/all")
    public ResponseEntity<?> fetchAll() {
        var response= customerService.fetchAll();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @GetMapping("search")
    public ResponseEntity<?> searchCustomers(
            @RequestParam("q") String keyword,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        var response = customerService.searchCustomers(keyword, startDate, endDate, pageable);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping()
    public ResponseEntity<?> fetchById(@RequestParam Long customerId) {
        var response= customerService.fetchById(customerId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping()
    public ResponseEntity<?> editCustomer(@RequestParam Long customerId, @RequestBody CustomerDto updatedCustomerData) {
        var response= customerService.editCustomer(customerId,updatedCustomerData);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @DeleteMapping()
    public ResponseEntity<?> deleteCustomer(@RequestParam Long customerId) {
        var response= customerService.deleteCustomer(customerId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
