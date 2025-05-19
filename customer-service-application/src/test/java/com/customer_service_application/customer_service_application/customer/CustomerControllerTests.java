package com.customer_service_application.customer_service_application.customer;

import com.customer_service_application.customer_service_application.utilities.EntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)

public class CustomerControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private CustomerDto sampleDto;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleDto = new CustomerDto();
        sampleDto.setFirstName("John");
        sampleDto.setLastName("Doe");
        sampleDto.setOtherName("M");

        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setFirstName("John");
        sampleCustomer.setLastName("Doe");
        sampleCustomer.setOtherName("M");
    }






    @Test
    void createCustomer_ShouldReturnCreated() throws Exception {
        EntityResponse<Customer> response = new EntityResponse<>();
        response.setStatusCode(201);
        response.setMessage("Customer created successfully");
        response.setPayload(sampleCustomer);

        when(customerService.create(any(CustomerDto.class))).thenReturn(response);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "firstName": "John",
                          "lastName": "Doe",
                          "otherName": "M"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer created successfully"))
                .andExpect(jsonPath("$.payload.firstName").value("John"));

        verify(customerService).create(any(CustomerDto.class));
    }

    @Test
    void fetchAll_ShouldReturnOk() throws Exception {
        EntityResponse<List<Customer>> response = new EntityResponse<>();
        response.setStatusCode(200);
        response.setMessage("Customers fetched successfully");
        response.setPayload(Collections.singletonList(sampleCustomer));

        when(customerService.fetchAll()).thenReturn(response);

        mockMvc.perform(get("/customers/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customers fetched successfully"))
                .andExpect(jsonPath("$.payload[0].firstName").value("John"));

        verify(customerService).fetchAll();
    }


    @Test
    void fetchById_ShouldReturnOk() throws Exception {
        CustomerDto dto = new CustomerDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setOtherName("M");

        EntityResponse<CustomerDto> response = new EntityResponse<>();
        response.setStatusCode(200);
        response.setMessage("Customer fetched successfully");
        response.setPayload(dto);

        when(customerService.fetchById("1")).thenReturn(response);

        mockMvc.perform(get("/customers")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer fetched successfully"))
                .andExpect(jsonPath("$.payload.firstName").value("John"));

        verify(customerService).fetchById("1");
    }


    @Test
    void searchCustomers_ShouldReturnOk() throws Exception {
        // Prepare a Page<Customer> with sample data
        Page<Customer> customerPage = new PageImpl<>(Collections.singletonList(sampleCustomer));

        // Build the Map<String, Object> payload that matches your service method
        Map<String, Object> result = new HashMap<>();
        result.put("content", customerPage.getContent());
        result.put("currentPage", customerPage.getNumber());
        result.put("totalItems", customerPage.getTotalElements());
        result.put("totalPages", customerPage.getTotalPages());

        // Prepare the response to return from the mock
        EntityResponse<Map<String, Object>> response = new EntityResponse<>();
        response.setStatusCode(200);
        response.setMessage("Customers found");
        response.setPayload(result);

        // Mock the service method call with proper argument matchers
        when(customerService.searchCustomers(
                eq("John"),
                isNull(LocalDate.class),
                isNull(LocalDate.class),
                any(Pageable.class))
        ).thenReturn(response);

        // Perform the request and verify response JSON paths
        mockMvc.perform(get("/customers/search")
                        .param("q", "John")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customers found"))
                .andExpect(jsonPath("$.payload.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.payload.currentPage").value(0))
                .andExpect(jsonPath("$.payload.totalItems").value(1))
                .andExpect(jsonPath("$.payload.totalPages").value(1));

        // Verify the service call
        verify(customerService).searchCustomers(
                eq("John"),
                isNull(LocalDate.class),
                isNull(LocalDate.class),
                any(Pageable.class));
    }


    @Test
    void updateCustomer_ShouldReturnOk() throws Exception {
        EntityResponse<Customer> response = new EntityResponse<>();
        response.setStatusCode(200);
        response.setMessage("Customer updated successfully");
        response.setPayload(sampleCustomer);

        when(customerService.editCustomer(eq(1L), any(CustomerDto.class))).thenReturn(response);

        mockMvc.perform(put("/customers")
                        .param("customerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
              "firstName": "John",
              "lastName": "Doe",
              "otherName": "M"
            }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer updated successfully"));

        verify(customerService).editCustomer(eq(1L), any(CustomerDto.class));
    }

    @Test
    void deleteCustomer_ShouldReturnOk() throws Exception {
        EntityResponse<Customer> response = new EntityResponse<>();
        response.setStatusCode(200);
        response.setMessage("Customer deleted successfully (soft delete)");

        when(customerService.deleteCustomer(1L)).thenReturn(response);

        mockMvc.perform(delete("/customers")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted successfully (soft delete)"));

        verify(customerService).deleteCustomer(1L);
    }

}
