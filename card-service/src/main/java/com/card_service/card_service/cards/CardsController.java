package com.card_service.card_service.cards;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
