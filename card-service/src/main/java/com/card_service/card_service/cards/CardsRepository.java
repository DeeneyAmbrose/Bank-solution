package com.card_service.card_service.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardsRepository extends JpaRepository<Card, Long> {

    @Query(value = """
    SELECT * FROM cards c
    WHERE c.deleted_flag = 'N'
      AND (:cardAlias IS NULL OR LOWER(c.card_alias) LIKE LOWER(CONCAT('%', :cardAlias, '%')))
      AND (:type IS NULL OR c.type = :type)
      AND (:pan IS NULL OR c.pan LIKE CONCAT('%', :pan, '%'))
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Card> searchCardsNative(
            @Param("cardAlias") String cardAlias,
            @Param("type") String type,
            @Param("pan") String pan,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

}
