package com.gateway.fee.infrastructure.persistence;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fee_transaction_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeTransactionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID logId;

    @Column(name = "transaction_id", length = 100, nullable = false)
    private String transactionId;

    @Column(name = "transaction_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal transactionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_currency", length = 3, nullable = false)
    private Currency sourceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_currency", length = 3, nullable = false)
    private Currency destinationCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 30, nullable = false)
    private TransactionType transactionType;

    // Sender
    @Column(name = "sender_user_id", length = 100, nullable = false)
    private String senderUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_user_type", length = 30, nullable = false)
    private UserType senderUserType;

    @Column(name = "sender_matched_rule_id")
    private UUID senderMatchedRuleId;

    @Column(name = "sender_raw_fee", precision = 19, scale = 4)
    private BigDecimal senderRawFee;

    @Column(name = "sender_final_fee", precision = 19, scale = 4)
    private BigDecimal senderFinalFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_fee_currency", length = 3)
    private Currency senderFeeCurrency;

    @Column(name = "sender_cap_applied", length = 20)
    private String senderCapApplied;

    @Column(name = "sender_waived", nullable = false)
    private boolean senderWaived;

    // Receiver
    @Column(name = "receiver_user_id", length = 100, nullable = false)
    private String receiverUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_user_type", length = 30, nullable = false)
    private UserType receiverUserType;

    @Column(name = "receiver_matched_rule_id")
    private UUID receiverMatchedRuleId;

    @Column(name = "receiver_raw_fee", precision = 19, scale = 4)
    private BigDecimal receiverRawFee;

    @Column(name = "receiver_final_fee", precision = 19, scale = 4)
    private BigDecimal receiverFinalFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_fee_currency", length = 3)
    private Currency receiverFeeCurrency;

    @Column(name = "receiver_cap_applied", length = 20)
    private String receiverCapApplied;

    @Column(name = "receiver_waived", nullable = false)
    private boolean receiverWaived;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}