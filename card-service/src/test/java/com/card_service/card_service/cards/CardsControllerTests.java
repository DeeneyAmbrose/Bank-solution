package com.card_service.card_service.cards;
import com.card_service.card_service.utilities.EntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CardsControllerTests {

    @Mock
    private CardsService cardsService;

    @InjectMocks
    private CardsController cardsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_shouldReturnResponseFromService() {
        CardDto cardDto = new CardDto();
        Card sampleCard = new Card();

        EntityResponse<Card> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(201);
        responseFromService.setMessage("Card created successfully");
        responseFromService.setPayload(sampleCard);

        when(cardsService.createCard(cardDto)).thenReturn(responseFromService);

        ResponseEntity<?> response = cardsController.createCard(cardDto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());

        verify(cardsService).createCard(cardDto);
    }

    @Test
    void fetchCardById_shouldReturnResponseFromService() {
        Long cardId = 1L;
        Card sampleCard = new Card();

        EntityResponse<Card> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Card found");
        responseFromService.setPayload(sampleCard);

        when(cardsService.fetchCardById(cardId)).thenReturn(responseFromService);

        ResponseEntity<?> response = cardsController.fetchCardById(cardId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());

        verify(cardsService).fetchCardById(cardId);
    }

    @Test
    void editCard_shouldReturnResponseFromService() {
        Long cardId = 1L;
        CardDto cardDto = new CardDto();
        Card updatedCard = new Card();

        EntityResponse<Card> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Card updated");
        responseFromService.setPayload(updatedCard);

        when(cardsService.editCard(cardId, cardDto)).thenReturn(responseFromService);

        ResponseEntity<?> response = cardsController.editCard(cardId, cardDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());

        verify(cardsService).editCard(cardId, cardDto);
    }

    @Test
    void deleteCard_shouldReturnResponseFromService() {
        Long cardId = 1L;

        EntityResponse<Card> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(204);
        responseFromService.setMessage("Card deleted");
        responseFromService.setPayload(null);

        when(cardsService.deleteCard(cardId)).thenReturn(responseFromService);

        ResponseEntity<?> response = cardsController.deleteCard(cardId);

        assertEquals(204, response.getStatusCodeValue());
        assertEquals(responseFromService, response.getBody());

        verify(cardsService).deleteCard(cardId);
    }

    @Test
    void searchCards_shouldReturnMaskedCardDetailsWhenShowSensitiveIsFalse() {
        String cardAlias = "myCard";
        CardType type = CardType.PHYSICAL;
        String pan = "1234567890123456";
        boolean showSensitive = false;
        int page = 0;
        int size = 5;

        // Prepare card with sensitive data
        Card rawCard = new Card();
        rawCard.setPan(pan);
        rawCard.setCvv("123");
        rawCard.setCardAlias(cardAlias);

        // Simulate masked card as returned by the service (what the controller would receive)
        Card maskedCard = new Card();
        maskedCard.setPan("**** **** **** 3456");
        maskedCard.setCvv("***");
        maskedCard.setCardAlias(cardAlias);

        List<Card> maskedCardList = new ArrayList<>();
        maskedCardList.add(maskedCard);

        EntityResponse<List<Card>> responseFromService = new EntityResponse<>();
        responseFromService.setStatusCode(200);
        responseFromService.setMessage("Masked results");
        responseFromService.setPayload(maskedCardList);

        when(cardsService.searchCards(any(CardFilterRequest.class))).thenReturn(responseFromService);

        // Call controller
        ResponseEntity<?> response = cardsController.searchCards(cardAlias, type, pan, showSensitive, page, size);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof EntityResponse);

        EntityResponse<?> responseBody = (EntityResponse<?>) response.getBody();
        assertEquals("Masked results", responseBody.getMessage());

        @SuppressWarnings("unchecked")
        List<Card> returnedCards = (List<Card>) responseBody.getPayload();

        assertNotNull(returnedCards);
        assertEquals(1, returnedCards.size());

        Card returnedCard = returnedCards.get(0);
        assertEquals("**** **** **** 3456", returnedCard.getPan());
        assertEquals("***", returnedCard.getCvv());
        assertEquals(cardAlias, returnedCard.getCardAlias());

        verify(cardsService).searchCards(any(CardFilterRequest.class));
    }

}
