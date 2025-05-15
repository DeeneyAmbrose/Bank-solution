package com.customer_service.customer_service.customer;

import com.customer_service.customer_service.utilities.EntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    public EntityResponse<?> create(CustomerDto customerDto) {
        EntityResponse<Customer> response = new EntityResponse<>();
        try {
            Customer customer = new Customer();
            customer.setFirstName(customerDto.getFirstName());
            customer.setLastName(customerDto.getLastName());
            customer.setOtherName(customerDto.getOtherName());
            customer.setCreatedAt(LocalDateTime.now());
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

    public EntityResponse<?> fetchAll() {
        EntityResponse<List<Customer>> response = new EntityResponse<>();
        try {
            List<Customer> customers = customerRepository.findAll();
            if (customers.isEmpty()){
                response.setPayload(null);
                response.setMessage("Not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
            response.setPayload(customers);
            response.setMessage("Customers fetched successfully");
            response.setStatusCode(HttpStatus.OK.value());

        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }


    public EntityResponse<CustomerDto> fetchById(Long customerId) {
        EntityResponse<CustomerDto> response = new EntityResponse<>();
        Customer customer = customerRepository.findById(customerId).orElse(null);

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

    public EntityResponse<?> editCustomer(Long customerId, CustomerDto updatedCustomerData) {
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
            existingCustomer.setUpdatedAt(LocalDateTime.now());

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

    public EntityResponse<?> deleteCustomer(Long customerId) {
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

    public EntityResponse<?> searchCustomers(String keyword) {
        EntityResponse<List<Customer>> response = new EntityResponse<>();
        try {
            List<Customer> customers = customerRepository.findByKeyword(keyword);

            if (customers.isEmpty()) {
                response.setPayload(null);
                response.setMessage("No customers found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            } else {
                response.setPayload(customers);
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
