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
import java.util.Optional;
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

    public EntityResponse<?> fetchCardById(Long cardId) {
        EntityResponse<Card> response = new EntityResponse<>();
        try {
            Optional<Card> optionalCard = cardsRepository.findById(cardId);
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
                if ("N".equalsIgnoreCase(card.getDeletedFlag())) {
                    response.setPayload(card);
                    response.setMessage("Card fetched successfully");
                    response.setStatusCode(HttpStatus.OK.value());
                } else {
                    response.setMessage("Card is marked as deleted");
                    response.setStatusCode(HttpStatus.GONE.value()); // 410 Gone
                }
            } else {
                response.setMessage("Card not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public EntityResponse<?> editCard(Long cardId, CardDto cardDto) {
        EntityResponse<Card> response = new EntityResponse<>();
        try {
            Optional<Card> optionalCard = cardsRepository.findById(cardId);
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
                if ("N".equalsIgnoreCase(card.getDeletedFlag())) {
                    card.setCardAlias(cardDto.getCardAlias());

                    cardsRepository.save(card);

                    response.setPayload(card);
                    response.setMessage("Card alias updated successfully");
                    response.setStatusCode(HttpStatus.OK.value());
                } else {
                    response.setMessage("Card is marked as deleted");
                    response.setStatusCode(HttpStatus.GONE.value()); // 410 Gone
                }
            } else {
                response.setMessage("Card not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public EntityResponse<?> deleteCard(Long cardId) {
        EntityResponse<Card> response = new EntityResponse<>();
        try {
            Optional<Card> optionalCard = cardsRepository.findById(cardId);
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
                if ("N".equalsIgnoreCase(card.getDeletedFlag())) {
                    card.setDeletedFlag("Y");
                    cardsRepository.save(card);

                    response.setPayload(card);
                    response.setMessage("Card deleted successfully (soft delete)");
                    response.setStatusCode(HttpStatus.OK.value());
                }
            } else {
                response.setMessage("Card not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return response;
    }

    public EntityResponse<?> searchCards(CardFilterRequest request) {
        EntityResponse<List<Card>> response = new EntityResponse<>();
        try {
            int offset = request.getPage() * request.getSize();

            List<Card> cards = cardsRepository.searchCardsNative(
                    request.getCardAlias(),
                    request.getType() != null ? request.getType().name() : null,
                    request.getPan(),
                    request.getSize(),
                    offset
            );

            List<Card> result = cards.stream().map(card -> {
                if (!Boolean.TRUE.equals(request.getShowSensitive())) {
                    card.setPan(maskPan(card.getPan()));
                    card.setCvv("***");
                }
                return card;
            }).collect(Collectors.toList());

            response.setPayload(result);
            response.setMessage("Cards fetched successfully");
            response.setStatusCode(HttpStatus.OK.value());
        } catch (Exception e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        return "**** **** **** " + pan.substring(pan.length() - 4);
    }


}
