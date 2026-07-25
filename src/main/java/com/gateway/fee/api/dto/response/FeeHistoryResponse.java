package com.gateway.fee.api.dto.response;

import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
// whenever a user requests history we wont give raw data from database we give them this record instead
// so they will see our data in this order and we wont be sharing anything that is not needed
public record FeeHistoryResponse(String transactionId, BigDecimal transactionAmount,
                                 Currency sourceCurrency, Currency destinationCurrency,
                                 TransactionType transactionType,LocalDateTime calculatedAt,
                                 String senderId,String receiverId,
                                 Currency senderFeeCurrency,boolean senderWaived,
                                 BigDecimal senderFinalFee,BigDecimal receiverFinalFee,
                                 Currency receiverFeeCurrency,boolean receiverWaived) {
}
