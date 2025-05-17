package com.card_service.card_service.cards;

import lombok.Data;

@Data
public class CardDto {
    private String cardAlias;
    private Long accountId;
    private CardType type;
    private String pan;
    private String cvv;


}
