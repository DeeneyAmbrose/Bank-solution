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
        dto.setAccountId(1L);
        dto.setType(CardType.PHYSICAL);
        dto.setPan("1234567812345678");
        dto.setCvv("123");
        dto.setCardAlias("My Card");

        AccountDto accountDto = new AccountDto();
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

        when(cardsRepository.findAll()).thenReturn(Collections.emptyList());
        when(cardsRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EntityResponse<Card> response = cardsService.createCard(dto);

        assertEquals(201, response.getStatusCode());
        assertEquals("Card created successfully", response.getMessage());
        assertNotNull(response.getPayload());
    }

    @Test
    void fetchCardById_shouldReturnCardIfExistsAndNotDeleted() {
        Card card = new Card();
        card.setDeletedFlag("N");

        when(cardsRepository.findById(1L)).thenReturn(Optional.of(card));

        EntityResponse<Card> response = cardsService.fetchCardById(1L);

        assertEquals(200, response.getStatusCode());
        assertEquals("Card fetched successfully", response.getMessage());
        assertEquals(card, response.getPayload());
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
    void searchCards_shouldMaskPanAndCvv_WhenShowSensitiveIsFalse() {
        Card card = new Card();
        card.setPan("1234567812345678");
        card.setCvv("123");
        card.setCardAlias("Test Card");

        when(cardsRepository.searchCardsNative(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(card));

        CardFilterRequest request = new CardFilterRequest();
        request.setCardAlias("Test");
        request.setType(CardType.PHYSICAL);
        request.setPan("1234");
        request.setShowSensitive(false); // 👈 Important for masking
        request.setPage(0);
        request.setSize(10);

        EntityResponse<List<Card>> response = cardsService.searchCards(request);

        assertEquals(200, response.getStatusCode());
        assertEquals("Cards fetched successfully", response.getMessage());

        List<Card> result = response.getPayload();
        assertEquals(1, result.size());
        assertEquals("**** **** **** 5678", result.get(0).getPan());
        assertEquals("***", result.get(0).getCvv());
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
