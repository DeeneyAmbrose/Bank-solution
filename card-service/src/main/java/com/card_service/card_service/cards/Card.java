package com.card_service.card_service.cards;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Table(name = "cards")
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cardAlias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @Column(nullable = false, unique = true, length = 16)
    private String pan;

    @Column(nullable = false, length = 3)
    private String cvv;

    @Column(nullable = false)
    private String deletedFlag = "N";

    @Column(nullable = false)
    private Long accountId;

}
