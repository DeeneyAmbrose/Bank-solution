package com.customer_service.customer_service.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.customer_service.customer_service.utilities.EntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    void create_ShouldSaveCustomer() {
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        EntityResponse<?> response = customerService.create(sampleDto);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("Customer created successfully", response.getMessage());
        assertTrue(response.getPayload() instanceof Customer);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void fetchAll_WhenEmpty_ShouldReturnNotFound() {
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        EntityResponse<?> response = customerService.fetchAll();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Not found", response.getMessage());
        assertNull(response.getPayload());
    }

    @Test
    void fetchAll_WhenNotEmpty_ShouldReturnCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(sampleCustomer));

        EntityResponse<?> response = customerService.fetchAll();

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customers fetched successfully", response.getMessage());
        assertNotNull(response.getPayload());
        verify(customerRepository).findAll();
    }

    @Test
    void fetchById_WhenFound_ShouldReturnDto() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

        EntityResponse<CustomerDto> response = customerService.fetchById(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customer fetched successfully", response.getMessage());
        assertEquals("John", response.getPayload().getFirstName());
    }

    @Test
    void fetchById_WhenNotFound_ShouldReturnNotFound() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        EntityResponse<CustomerDto> response = customerService.fetchById(2L);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Customer not found", response.getMessage());
        assertNull(response.getPayload());
    }

    @Test
    void editCustomer_WhenCustomerExists_ShouldUpdate() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        EntityResponse<?> response = customerService.editCustomer(1L, sampleDto);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customer updated successfully", response.getMessage());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void editCustomer_WhenCustomerNotFound_ShouldReturnNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        EntityResponse<?> response = customerService.editCustomer(1L, sampleDto);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertEquals("Customer not found", response.getMessage());
    }

    @Test
    void deleteCustomer_WhenFound_ShouldSoftDelete() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        EntityResponse<?> response = customerService.deleteCustomer(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Customer deleted successfully (soft delete)", response.getMessage());
        verify(customerRepository).save(any(Customer.class));
        assertEquals("Y", sampleCustomer.getDeletedFlag());
    }

    @Test
    void deleteCustomer_WhenNotFound_ShouldReturnNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        EntityResponse<?> response = customerService.deleteCustomer(1L);

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
