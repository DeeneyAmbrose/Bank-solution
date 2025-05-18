package com.card_service.card_service.cards;

import com.card_service.card_service.utilities.EntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class CardsServiceTests {

    @Mock
    private CardsRepository cardsRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CardsService cardsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_shouldCreateCardSuccessfully() {
        CardDto dto = new CardDto();
        dto.setAccountId("1");  // changed to String
        dto.setType(CardType.PHYSICAL);
        dto.setCvv("123");
        dto.setCardAlias("My Card");

        AccountDto accountDto = new AccountDto();
        accountDto.setCustomerId("100");  // String customerId
        EntityResponse<AccountDto> accountResponse = new EntityResponse<>();
        accountResponse.setPayload(accountDto);

        ResponseEntity<EntityResponse<AccountDto>> responseEntity =
                new ResponseEntity<>(accountResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<EntityResponse<AccountDto>>>any()
        )).thenReturn(responseEntity);

        // Mock repository to return no existing cards for the account
        when(cardsRepository.findByAccountId(dto.getAccountId())).thenReturn(Collections.emptyList());

        // Mock save to return the saved card object
        when(cardsRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock last card id for generateCardId
        when(cardsRepository.findLastCardIdForYear(anyString())).thenReturn(null);

        EntityResponse<Card> response = cardsService.createCard(dto);

        assertEquals(201, response.getStatusCode());
        assertEquals("Card created successfully", response.getMessage());
        assertNotNull(response.getPayload());
        assertEquals(dto.getAccountId(), response.getPayload().getAccountId());
        assertEquals(dto.getType(), response.getPayload().getType());
        assertEquals(dto.getCardAlias(), response.getPayload().getCardAlias());
    }

    @Test
    void editCard_shouldUpdateCardAlias() {
        CardDto dto = new CardDto();
        dto.setCardAlias("Updated Alias");

        Card card = new Card();
        card.setDeletedFlag("N");

        when(cardsRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardsRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntityResponse<Card> response = cardsService.editCard(1L, dto);

        assertEquals(200, response.getStatusCode());
        assertEquals("Card alias updated successfully", response.getMessage());
        assertEquals("Updated Alias", response.getPayload().getCardAlias());
    }

    @Test
    void deleteCard_shouldSoftDeleteCard() {
        Card card = new Card();
        card.setDeletedFlag("N");

        when(cardsRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardsRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntityResponse<Card> response = cardsService.deleteCard(1L);

        assertEquals(200, response.getStatusCode());
        assertEquals("Card deleted successfully (soft delete)", response.getMessage());
        assertEquals("Y", response.getPayload().getDeletedFlag());
    }

    @Test
    void fetchCardById_shouldReturnCardIfExistsAndNotDeleted() {
        Card card = new Card();
        card.setDeletedFlag("N");

        when(cardsRepository.findById(1L)).thenReturn(Optional.of(card));

        // Pass 'false' for showSensitive to test masked data scenario
        EntityResponse<Card> response = cardsService.fetchCardById(1L, false);

        assertEquals(200, response.getStatusCode());
        assertEquals("Card fetched successfully", response.getMessage());
        assertEquals(card, response.getPayload());
    }

    @Test
    void fetchCardById_shouldReturnCardWithSensitiveData() {
        Card card = new Card();
        card.setDeletedFlag("N");
        card.setPan("1234567890123456");
        card.setCvv("123");

        when(cardsRepository.findById(1L)).thenReturn(Optional.of(card));

        EntityResponse<Card> response = cardsService.fetchCardById(1L, true);

        assertEquals(200, response.getStatusCode());
        assertEquals("Card fetched successfully", response.getMessage());
        assertEquals(card, response.getPayload());
    }


    @Test
    void searchCards_shouldNotMaskPan_WhenShowSensitiveIsTrue() {
        Card card = new Card();
        card.setPan("1234567812345678");
        card.setCvv("123");

        when(cardsRepository.searchCardsNative(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(card));

        CardFilterRequest request = new CardFilterRequest();
        request.setShowSensitive(true);
        request.setPage(0);
        request.setSize(10);

        EntityResponse<List<Card>> response = cardsService.searchCards(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("1234567812345678", response.getPayload().get(0).getPan());
        assertEquals("123", response.getPayload().get(0).getCvv());
    }
}
