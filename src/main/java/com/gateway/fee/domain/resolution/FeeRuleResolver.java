package com.gateway.fee.domain.resolution;

import com.gateway.fee.domain.exception.NoMatchingRuleException;
import com.gateway.fee.domain.model.*;
import com.gateway.fee.domain.registry.FeeRuleRegistry;
import io.vavr.Tuple;
import io.vavr.collection.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FeeRuleResolver {
    private final FeeRuleRegistry feeRuleRegistry;

    public FeeRule resolve(String userId, UserType userType, TransactionType transactionType, Currency sourceCurrency, Currency destinationCurrency) {
        List<FeeRule> filtered = feeRuleRegistry.findAllActive()
                .filter(rule -> matches(rule, userId, userType, transactionType, sourceCurrency, destinationCurrency));

        // if list is empty throw exception
        if (filtered.isEmpty()) {
            throw new NoMatchingRuleException("No matching rule found for userId: " + userId);
        }

        return filtered.sortBy(rule -> -score(rule, sourceCurrency, destinationCurrency)).head(); // sort by score in descending order;
    }

    // check matching rules
    // this method checks if the rule matches the transaction details, it returns true if all non-null fields in the rule match the transaction details,
    // (if the condition after the forAll keyword fails for any pair(tuple) it will return false, if all pairs match it will return true)

    private boolean matches(FeeRule rule, String userId, UserType userType,
                            TransactionType transactionType, Currency sourceCurrency,
                            Currency destinationCurrency) {
        return List.of(
                Tuple.of(rule.getUserId(), userId),
                Tuple.of(rule.getUserType(), userType),
                Tuple.of(rule.getTransactionType(), transactionType),
                Tuple.of(rule.getSourceCurrency(), sourceCurrency),
                Tuple.of(rule.getDestinationCurrency(), destinationCurrency)
        ).forAll(pair -> pair._1 == null || pair._1.equals(pair._2));
    }

    private int score(FeeRule rule, Currency sourceCurrency, Currency destinationCurrency) { // a system to compare the rules by priority
        int score = 0;
        if (rule.getUserId() != null) score += 1000;
        if (rule.getTransactionType() != null) score += 100;
        if (rule.getUserType() != null) score += 1;
        score += currencyScore(rule, sourceCurrency, destinationCurrency) * 10;// multiplying by 10 to make the digit greater beacuse of the priority

        return score;
    }

    // this method is only for source/destination currency, we cant implement it in score method because of the 4 levels of matching (exact, partial source, partial destination, wildcard), while the rest are only binary values, 1 or 0,(match or no match).
    private int currencyScore(FeeRule rule, Currency sourceCurrency, Currency destinationCurrency) {
        CurrencyPair pair = new CurrencyPair(rule.getSourceCurrency(), rule.getDestinationCurrency());
        if (pair.isExactMatch(sourceCurrency, destinationCurrency)) {
            return 4;
        }
        if (pair.isPartialSourceMatch(sourceCurrency)) {
            return 3;
        }
        if (pair.isPartialDestinationMatch(destinationCurrency)) {
            return 2;
        }
        if (pair.isWildcard()) {
            return 1;
        }
        return 0;
    }
}
