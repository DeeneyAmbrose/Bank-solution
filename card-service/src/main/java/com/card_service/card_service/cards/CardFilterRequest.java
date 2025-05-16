package com.card_service.card_service.cards;

import lombok.Data;

@Data
public class CardFilterRequest {
    private String cardAlias;
    private CardType type;
    private String pan;
    private Boolean showSensitive = false;

    private int page = 0;
    private int size = 10;
}
