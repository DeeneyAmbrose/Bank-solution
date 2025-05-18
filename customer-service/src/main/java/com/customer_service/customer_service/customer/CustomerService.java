package com.customer_service.customer_service.customer;

import com.customer_service.customer_service.utilities.EntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    public EntityResponse<Customer> create(CustomerDto customerDto) {
        EntityResponse<Customer> response = new EntityResponse<>();
        try {
            Customer customer = new Customer();
            customer.setCustomerId(generateCustomerId());
            customer.setFirstName(customerDto.getFirstName());
            customer.setLastName(customerDto.getLastName());
            customer.setOtherName(customerDto.getOtherName());
            customer.setCreatedAt(LocalDate.now());
            customer.setDeletedFlag("N");

            Customer savedCustomer = customerRepository.save(customer);

            response.setPayload(savedCustomer);
            response.setMessage("Customer created successfully");
            response.setStatusCode(HttpStatus.CREATED.value());
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }


    public String generateCustomerId() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "CUS" + year;
        String lastId = customerRepository.findLastCustomerIdForYear(prefix);

        int nextNumber = 1;
        if (lastId != null && lastId.matches(prefix + "\\d{5}")) {
            String numberPart = lastId.substring(prefix.length());
            nextNumber = Integer.parseInt(numberPart) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }



    public EntityResponse<List<Customer>> fetchAll() {
        EntityResponse<List<Customer>> response = new EntityResponse<>();
        try {
            List<Customer> customers = customerRepository.findAll();
            if (customers.isEmpty()){
                response.setPayload(null);
                response.setMessage("Not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }else {
                response.setPayload(customers);
                response.setMessage("Customers fetched successfully");
                response.setStatusCode(HttpStatus.OK.value());
            }

        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }


    public EntityResponse<CustomerDto> fetchById(String customerId) {
        EntityResponse<CustomerDto> response = new EntityResponse<>();
        Customer customer = customerRepository.findByCustomerId(customerId);

        if (customer == null) {
            response.setMessage("Customer not found");
            response.setStatusCode(HttpStatus.NOT_FOUND.value());
        } else {
            CustomerDto dto = new CustomerDto();
            dto.setFirstName(customer.getFirstName());
            dto.setLastName(customer.getLastName());
            dto.setOtherName(customer.getOtherName());
            response.setPayload(dto);
            response.setMessage("Customer fetched successfully");
            response.setStatusCode(HttpStatus.OK.value());
        }

        return response;
    }

    public EntityResponse<Customer> editCustomer(Long customerId, CustomerDto updatedCustomerData) {
        EntityResponse<Customer> response = new EntityResponse<>();
        try {
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if (optionalCustomer.isEmpty()) {
                response.setPayload(null);
                response.setMessage("Customer not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                return response;
            }

            Customer existingCustomer = optionalCustomer.get();
            existingCustomer.setFirstName(updatedCustomerData.getFirstName());
            existingCustomer.setLastName(updatedCustomerData.getLastName());
            existingCustomer.setOtherName(updatedCustomerData.getOtherName());
            existingCustomer.setUpdatedAt(LocalDate.now());

            Customer savedCustomer = customerRepository.save(existingCustomer);

            response.setPayload(savedCustomer);
            response.setMessage("Customer updated successfully");
            response.setStatusCode(HttpStatus.OK.value());

        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public EntityResponse<Customer> deleteCustomer(Long customerId) {
        EntityResponse<Customer> response = new EntityResponse<>();
        try {
            Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
            if (optionalCustomer.isEmpty()) {
                response.setPayload(null);
                response.setMessage("Customer not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                return response;
            }

            Customer customerToDelete = optionalCustomer.get();
            customerToDelete.setDeletedFlag("Y");
            customerRepository.save(customerToDelete);

            response.setPayload(customerToDelete);
            response.setMessage("Customer deleted successfully (soft delete)");
            response.setStatusCode(HttpStatus.OK.value());

        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public EntityResponse<Map<String, Object>> searchCustomers(
            String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        EntityResponse<Map<String, Object>> response = new EntityResponse<>();
        try {
            Page<Customer> customersPage = customerRepository.findByKeywordAndDateRange(keyword, startDate, endDate, pageable);

            if (customersPage.isEmpty()) {
                response.setPayload(null);
                response.setMessage("No customers found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("content", customersPage.getContent());
                result.put("currentPage", customersPage.getNumber());
                result.put("totalItems", customersPage.getTotalElements());
                result.put("totalPages", customersPage.getTotalPages());

                response.setPayload(result);
                response.setMessage("Customers found");
                response.setStatusCode(HttpStatus.OK.value());
            }

        } catch (Exception e) {
            response.setPayload(null);
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }



}
