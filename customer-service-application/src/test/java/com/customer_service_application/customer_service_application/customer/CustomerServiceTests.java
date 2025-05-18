package com.customer_service_application.customer_service_application.customer;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomerServiceTests {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    private CustomerDto sampleDto;
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleDto = new CustomerDto();
        sampleDto.setFirstName("John");
        sampleDto.setLastName("Doe");
        sampleDto.setOtherName("M");

        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setFirstName("John");
        sampleCustomer.setLastName("Doe");
        sampleCustomer.setOtherName("M");
        sampleCustomer.setCreatedAt(LocalDate.now());
        sampleCustomer.setDeletedFlag("N");
    }

    @Test
    void createCustomer_ShouldReturnCreatedCustomer() {
        CustomerDto dto = new CustomerDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setOtherName("M");

        Customer savedCustomer = new Customer();
        savedCustomer.setCustomerId("CUS202500001");
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Doe");
        savedCustomer.setOtherName("M");
        savedCustomer.setDeletedFlag("N");

        when(customerRepository.findLastCustomerIdForYear(anyString())).thenReturn(null);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        EntityResponse<Customer> response = customerService.create(dto);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Customer created successfully", response.getMessage());
        assertEquals("John", response.getPayload().getFirstName());
        assertNotNull(response.getPayload().getCustomerId());
    }

    @Test
    void fetchById_WhenFound_ShouldReturnDto() {
        when(customerRepository.findByCustomerId("CUS202500001")).thenReturn(sampleCustomer);

        EntityResponse<CustomerDto> response = customerService.fetchById("CUS202500001");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customer fetched successfully", response.getMessage());
        assertEquals("John", response.getPayload().getFirstName());
    }

    @Test
    void fetchById_WhenNotFound_ShouldReturnNotFound() {
        when(customerRepository.findByCustomerId("CUS999999999")).thenReturn(null);

        EntityResponse<CustomerDto> response = customerService.fetchById("CUS999999999");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Customer not found", response.getMessage());
        assertNull(response.getPayload());
    }

    // Test for editCustomer(Long customerId, CustomerDto updatedCustomerData)
    @Test
    void editCustomer_WhenCustomerExists_ShouldUpdate() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        EntityResponse<Customer> response = customerService.editCustomer(1L, sampleDto);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customer updated successfully", response.getMessage());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void editCustomer_WhenCustomerNotFound_ShouldReturnNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        EntityResponse<Customer> response = customerService.editCustomer(1L, sampleDto);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Customer not found", response.getMessage());
    }

    // Test for deleteCustomer(Long customerId)
    @Test
    void deleteCustomer_WhenFound_ShouldSoftDelete() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        EntityResponse<Customer> response = customerService.deleteCustomer(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customer deleted successfully (soft delete)", response.getMessage());
        verify(customerRepository).save(any(Customer.class));
        assertEquals("Y", sampleCustomer.getDeletedFlag());
    }

    @Test
    void deleteCustomer_WhenNotFound_ShouldReturnNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        EntityResponse<Customer> response = customerService.deleteCustomer(1L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Customer not found", response.getMessage());
    }

    @Test
    void searchCustomers_WhenFound_ShouldReturnPagedResult() {
        Page<Customer> page = new PageImpl<>(List.of(sampleCustomer), PageRequest.of(0, 10), 1);
        when(customerRepository.findByKeywordAndDateRange(
                eq("John"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        EntityResponse<Map<String, Object>> response = customerService.searchCustomers("John", null, null, PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customers found", response.getMessage());
        assertNotNull(response.getPayload());

        Map<String, Object> payload = response.getPayload();
        assertNotNull(payload.get("content"));
        assertEquals(1, ((List<?>)payload.get("content")).size());
        assertEquals(0, payload.get("currentPage"));
        assertEquals(1L, payload.get("totalItems"));
        assertEquals(1, payload.get("totalPages"));
    }

    @Test
    void searchCustomers_WhenNotFound_ShouldReturnNotFound() {
        Page<Customer> emptyPage = Page.empty();
        when(customerRepository.findByKeywordAndDateRange(
                eq("Unknown"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        EntityResponse<Map<String, Object>> response = customerService.searchCustomers("Unknown", null, null, PageRequest.of(0, 10));

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("No customers found", response.getMessage());
        assertNull(response.getPayload());
    }

}
