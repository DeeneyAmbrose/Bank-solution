package com.card_service.card_service.cards;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardsController {
    private  final CardsService cardsService;
    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardDto cardDto) {
        var response= cardsService.createCard(cardDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> fetchCardById(@RequestParam Long cardId) {
        var response= cardsService.fetchCardById(cardId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping
    public ResponseEntity<?> editCard(@RequestParam Long cardId, @RequestBody  CardDto cardDto) {
        var response= cardsService.editCard(cardId, cardDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @DeleteMapping
    public ResponseEntity<?> deleteCard(@RequestParam Long cardId) {
        var response= cardsService.deleteCard(cardId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCards(
            @RequestParam(required = false) String cardAlias,
            @RequestParam(required = false) CardType type,
            @RequestParam(required = false) String pan,
            @RequestParam(defaultValue = "false") boolean showSensitive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CardFilterRequest request = new CardFilterRequest();
        request.setCardAlias(cardAlias);
        request.setType(type);
        request.setPan(pan);
        request.setShowSensitive(showSensitive);
        request.setPage(page);
        request.setSize(size);

        var response = cardsService.searchCards(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
