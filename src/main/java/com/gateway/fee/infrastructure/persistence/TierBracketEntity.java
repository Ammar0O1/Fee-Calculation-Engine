package com.gateway.fee.infrastructure.persistence;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tier_bracket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierBracketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bracket_id")
    private UUID bracketId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private FeeSideDefinitionEntity sideDefinition;

    @Column(name = "from_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal fromAmount;

    @Column(name = "to_amount", precision = 19, scale = 4)
    private BigDecimal toAmount;

    @Column(name = "rate", precision = 10, scale = 6, nullable = false)
    private BigDecimal rate;

    @Column(name = "bracket_order", nullable = false)
    private int bracketOrder;
}