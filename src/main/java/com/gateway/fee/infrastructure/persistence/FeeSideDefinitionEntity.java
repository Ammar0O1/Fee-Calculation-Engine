package com.gateway.fee.infrastructure.persistence;
import com.gateway.fee.domain.model.CalculationMode;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "fee_side_definition",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_rule_side",
                columnNames = {"rule_id", "side"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeSideDefinitionEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "definition_id")
        private UUID definitionId;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "rule_id", nullable = false)
        private FeeRuleEntity feeRule;

        @Column(name = "side", length = 8, nullable = false)
        private String side;  // "SENDER" or "RECEIVER"

        @Enumerated(EnumType.STRING)
        @Column(name = "calculation_mode", length = 20, nullable = false)
        private CalculationMode calculationMode;

        @Column(name = "flat_amount", precision = 19, scale = 4)
        private BigDecimal flatAmount;

        @Column(name = "percentage", precision = 10, scale = 6)
        private BigDecimal percentage;

        @Column(name = "min_cap", precision = 19, scale = 4)
        private BigDecimal minCap;

        @Column(name = "max_cap", precision = 19, scale = 4)
        private BigDecimal maxCap;

        @OneToMany(
                mappedBy = "sideDefinition",
                cascade = CascadeType.ALL,
                orphanRemoval = true,
                fetch = FetchType.LAZY
        )
        @OrderBy("bracketOrder ASC")
        private List<TierBracketEntity> tierBrackets = new ArrayList<>();
}