package com.card_service.card_service.cards;

import lombok.Data;

@Data
public class CardDto {
    private String cardAlias;
    private String accountId;
    private CardType type;
    private String cvv;


}
