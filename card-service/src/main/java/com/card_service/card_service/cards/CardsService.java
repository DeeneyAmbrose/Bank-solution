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

import java.time.LocalDate;
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

    public EntityResponse<Card> createCard(CardDto dto) {
        EntityResponse<Card> response = new EntityResponse<>();

        try {
            // 1. Validate account existence
            ResponseEntity<EntityResponse<AccountDto>> accountResponse =
                    restTemplate.exchange(
                            accountServiceUrl + "accounts?accountId=" + dto.getAccountId(),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );

            if (!accountResponse.getStatusCode().is2xxSuccessful() || accountResponse.getBody() == null) {
                response.setMessage("Account not found");
                response.setStatusCode(HttpStatus.NOT_FOUND.value());
                return response;
            }

            // 2. Validate account belongs to the provided customer
            AccountDto accountDto = accountResponse.getBody().getPayload();
            if (!accountDto.getCustomerId().equals(accountResponse.getBody().getPayload().getCustomerId())) {
                response.setMessage("Account does not belong to the provided customer");
                response.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return response;
            }

            // 3. Fetch cards for this account
            List<Card> existingCards = cardsRepository.findByAccountId(dto.getAccountId());

            // 4. Check max 2 cards rule
            if (existingCards.size() >= 2) {
                response.setMessage("Maximum of 2 cards allowed per account");
                response.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return response;
            }

            // 5. Ensure only one card per type
            boolean hasSameType = existingCards.stream()
                    .anyMatch(c -> c.getType().equals(dto.getType()));

            if (hasSameType) {
                response.setMessage("Card of this type already exists for the account");
                response.setStatusCode(HttpStatus.CONFLICT.value());
                return response;
            }

            // 6. Create new card
            Card card = new Card();
            card.setCardAlias(dto.getCardAlias());
            card.setAccountId(dto.getAccountId());
            card.setCardId(generateCardId());
            card.setType(dto.getType());
            card.setPan(generateCardId());
            card.setCvv(dto.getCvv());

            // Set primaryCardFlag based on whether it's the first card
            card.setPrimaryCardFlag(existingCards.isEmpty() ? "Y" : "N");

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

    public String generateCardId() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "C" + year;
        String lastId = cardsRepository.findLastCardIdForYear(prefix);

        int nextNumber = 1;
        if (lastId != null && lastId.startsWith(prefix)) {
            String numberPart = lastId.substring(prefix.length());
            nextNumber = Integer.parseInt(numberPart) + 1;
        }

        return prefix + String.format("%05d", nextNumber);
    }


    public EntityResponse<Card> fetchCardById(Long cardId, boolean showSensitive) {
        EntityResponse<Card> response = new EntityResponse<>();
        try {
            Optional<Card> optionalCard = cardsRepository.findById(cardId);
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
                if ("N".equalsIgnoreCase(card.getDeletedFlag())) {
                    if (!showSensitive) {
                        card.setPan(maskPan(card.getPan()));
                        card.setCvv("***");
                    }
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

    public EntityResponse<Card> editCard(Long cardId, CardDto cardDto) {
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

    public EntityResponse<Card> deleteCard(Long cardId) {
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

    public EntityResponse<List<Card>> searchCards(CardFilterRequest request) {
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
