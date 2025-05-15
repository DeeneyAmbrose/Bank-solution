package com.card_service.card_service.cards;

import com.card_service.card_service.utilities.EntityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardsService {
    private final CardsRepository cardsRepository;
    private final RestTemplate restTemplate;

    @Value("${account-service.url}")
    private String accountServiceUrl;

    public EntityResponse<?> createCard(CardDto dto) {
        EntityResponse<Card> response = new EntityResponse<>();

        try {
            ResponseEntity<EntityResponse<AccountDto>> customerResponse =
                    restTemplate.exchange(
                            accountServiceUrl + "accounts?accountId=" + dto.getAccountId(),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            if (!customerResponse.getStatusCode().is2xxSuccessful() || customerResponse.getBody() == null) {
                response.setMessage("Customer not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                return response;
            }

            List<Card> existingCards = cardsRepository.findAll().stream()
                    .filter(c -> c.getAccountId().equals(dto.getAccountId()) )
                    .toList();

            boolean hasSameType = existingCards.stream()
                    .anyMatch(c -> c.getType().equals(dto.getType()));

            if (existingCards.size() >= 2 || hasSameType) {
                response.setMessage("Card limit reached or card type already exists for this account");
                response.setStatusCode(HttpStatus.CONFLICT.value());
                return response;
            }

            Card card = new Card();
            card.setCardAlias(dto.getCardAlias());
            card.setAccountId(dto.getAccountId());
            card.setType(dto.getType());
            card.setPan(dto.getPan());
            card.setCvv(dto.getCvv());

            cardsRepository.save(card);

            response.setPayload(card);
            response.setMessage("Card created successfully");
            response.setStatusCode(HttpStatus.CREATED.value());
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

}
