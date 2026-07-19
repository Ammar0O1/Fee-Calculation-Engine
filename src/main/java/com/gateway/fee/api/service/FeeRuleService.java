package com.gateway.fee.api.service;

import com.gateway.fee.api.dto.request.CreateDefaultRuleRequest;
import com.gateway.fee.api.dto.request.CreateFeeRuleRequest;
import com.gateway.fee.api.dto.response.FeeRuleResponse;
import com.gateway.fee.api.mapper.FeeRuleApiMapper;
import com.gateway.fee.api.validation.FeeRuleValidator;
import com.gateway.fee.domain.model.Currency;
import com.gateway.fee.domain.model.TransactionType;
import com.gateway.fee.domain.model.UserType;
import com.gateway.fee.infrastructure.persistence.entity.FeeRuleEntity;
import com.gateway.fee.infrastructure.persistence.repository.FeeRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FeeRuleService {
    private final FeeRuleRepository feeRuleRepository;
    private final FeeRuleApiMapper feeRuleApiMapper;
    private final FeeRuleValidator feeRuleValidator;

    // serving CREATE default fee rule
    public FeeRuleResponse createDefaultRule(CreateDefaultRuleRequest request) {
        // converting enums
        TransactionType txType = feeRuleApiMapper.toTransactionType(request.getTransactionType());
        Currency srcCurrency = feeRuleApiMapper.toCurrency(request.getSourceCurrency());
        Currency dstCurrency = feeRuleApiMapper.toCurrency(request.getDestinationCurrency());

        // validation: 1. check if rule already exists, 2. check if the side definitions are valid
        feeRuleValidator.validateNotDuplicate(
                null, null, txType, srcCurrency, dstCurrency
        );
        feeRuleValidator.validateSide(request.getSenderFee(), request.getReceiverFee());

// creating rule entity
        FeeRuleEntity feeRuleEntity = FeeRuleEntity.builder().
                ruleId(UUID.randomUUID()).
                userId(null).
                userType(null).
                transactionType(txType).
                destinationCurrency(dstCurrency).
                sourceCurrency(srcCurrency).
                effectiveDate(LocalDateTime.now()).
                active(true).
                description(request.getDescription()).
                sideDefinitions(new ArrayList<>()).
                build();
        // Handling the sender fee side definition
        if (request.getSenderFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getSenderFee(), feeRuleEntity, "SENDER")
            );
        }

        if (request.getReceiverFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getReceiverFee(), feeRuleEntity, "RECEIVER")
            );
        }


        FeeRuleEntity savedEntity = feeRuleRepository.save(feeRuleEntity);

        // convert entity to response
        return feeRuleApiMapper.toRuleResponse(savedEntity);


    }

    // serving GET default fee rules
    public List<FeeRuleResponse> getDefaultRules() {
        // call repository to get the default fee rules
        return feeRuleRepository.findByUserIdIsNullAndUserTypeIsNullAndActiveTrue()
                // we are retrieving the data from DB, and converting it to a response form by the help of mapper
                .stream()
                .map(feeRuleApiMapper::toRuleResponse)
                .toList();

    }

    // serving DELETE default fee rules
    public void deleteDefaultRule(UUID feeRuleId) {
        // finding the rule
        FeeRuleEntity rule = feeRuleRepository.findById(feeRuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee rule not found with id: " + feeRuleId));
        rule.setActive(false); // soft deleting it (we will still have the row in database)
        feeRuleRepository.save(rule);
    }

    // serving PUT default fee rules
    public FeeRuleResponse updateDefaultRule(UUID feeRuleId, CreateDefaultRuleRequest request) {
        // converting enums
        TransactionType txType = feeRuleApiMapper.toTransactionType(request.getTransactionType());
        Currency srcCurrency = feeRuleApiMapper.toCurrency(request.getSourceCurrency());
        Currency dstCurrency = feeRuleApiMapper.toCurrency(request.getDestinationCurrency());

        // validation
        feeRuleValidator.validateSide(request.getSenderFee(), request.getReceiverFee());
        feeRuleValidator.validateNotDuplicateForUpdate(feeRuleId,null,null, txType, srcCurrency, dstCurrency);
        // finding the rule
        FeeRuleEntity feeRuleEntity = feeRuleRepository.findById(feeRuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee rule not found with id: " + feeRuleId));


        feeRuleEntity.setTransactionType(txType);
        feeRuleEntity.setDestinationCurrency(dstCurrency);
        feeRuleEntity.setSourceCurrency(srcCurrency);
        feeRuleEntity.setDescription(request.getDescription());

        feeRuleEntity.getSideDefinitions().clear(); // clear existing side definitions
        feeRuleRepository.saveAndFlush(feeRuleEntity);   // force the delete to hit the DB now

        if (request.getSenderFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getSenderFee(), feeRuleEntity, "SENDER")
            );
        }

        if (request.getReceiverFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getReceiverFee(), feeRuleEntity, "RECEIVER")
            );
        }
        FeeRuleEntity updatedRule = feeRuleRepository.save(feeRuleEntity);


        return feeRuleApiMapper.toRuleResponse(updatedRule);
    }

    // serving CREATE user-type rule


    public FeeRuleResponse createUserTypeRule(CreateFeeRuleRequest request) {
        // converting enums
        TransactionType txType = feeRuleApiMapper.toTransactionType(request.getTransactionType());
        Currency srcCurrency = feeRuleApiMapper.toCurrency(request.getSourceCurrency());
        Currency dstCurrency = feeRuleApiMapper.toCurrency(request.getDestinationCurrency());
        UserType userType = feeRuleApiMapper.toUserType(request.getUserType());

        // validation
        feeRuleValidator.validateSide(request.getSenderFee(), request.getReceiverFee());
        feeRuleValidator.validateNotDuplicate(null, userType,txType, srcCurrency, dstCurrency);
        // creating Rule Entity
        FeeRuleEntity feeRuleEntity = FeeRuleEntity.builder().
                ruleId(UUID.randomUUID()).
                userId(null).
                userType(userType).
                transactionType(txType).
                destinationCurrency(dstCurrency).
                sourceCurrency(srcCurrency).
                effectiveDate(LocalDateTime.now()).
                active(true).
                description(request.getDescription()).
                sideDefinitions(new ArrayList<>()).
                build();

        if (request.getSenderFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getSenderFee(), feeRuleEntity, "SENDER")
            );
        }
        if (request.getReceiverFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getReceiverFee(), feeRuleEntity, "RECEIVER")
            );
        }

        FeeRuleEntity savedEntity = feeRuleRepository.save(feeRuleEntity);

        return feeRuleApiMapper.toRuleResponse(savedEntity);
    }

    // serving GET any user-type fee rules

    public List<FeeRuleResponse> getUserTypeRules() {
        return feeRuleRepository.findByUserIdIsNullAndUserTypeIsNotNullAndActiveTrue()
                .stream()
                .map(feeRuleApiMapper::toRuleResponse)
                .toList();
    }

    // serving GET specific user-type fee rules

    public List<FeeRuleResponse> getUserTypeRulesByType(String userType) {
        return feeRuleRepository.findByUserIdIsNullAndUserTypeAndActiveTrue(feeRuleApiMapper.toUserType(userType))
                .stream()
                .map(feeRuleApiMapper::toRuleResponse)
                .toList();
    }

    // serving PUT for user-type fee rules
    public FeeRuleResponse updateUserTypeRule(UUID feeRuleId, CreateFeeRuleRequest request) {
        //convert enums
        TransactionType txType = feeRuleApiMapper.toTransactionType(request.getTransactionType());
        Currency srcCurrency = feeRuleApiMapper.toCurrency(request.getSourceCurrency());
        Currency dstCurrency = feeRuleApiMapper.toCurrency(request.getDestinationCurrency());
        UserType userType = feeRuleApiMapper.toUserType(request.getUserType());

        // validation
        feeRuleValidator.validateSide(request.getSenderFee(), request.getReceiverFee());
        feeRuleValidator.validateNotDuplicateForUpdate(feeRuleId,null, userType, txType, srcCurrency, dstCurrency);

        FeeRuleEntity feeRuleEntity = feeRuleRepository.findById(feeRuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee rule not found with id: " + feeRuleId));

        feeRuleEntity.setUserType(userType);
        feeRuleEntity.setTransactionType(txType);
        feeRuleEntity.setDestinationCurrency(dstCurrency);
        feeRuleEntity.setSourceCurrency(srcCurrency);
        feeRuleEntity.setDescription(request.getDescription());

        feeRuleEntity.getSideDefinitions().clear(); // clear existing side definitions
        feeRuleRepository.saveAndFlush(feeRuleEntity);   // force the delete to hit the DB now


        if (request.getSenderFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getSenderFee(), feeRuleEntity, "SENDER")
            );
        }

        if (request.getReceiverFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getReceiverFee(), feeRuleEntity, "RECEIVER")
            );
        }
        FeeRuleEntity updatedRule = feeRuleRepository.save(feeRuleEntity);
        return feeRuleApiMapper.toRuleResponse(updatedRule);
    }

    // serving Delete specific user-type fee rules

    public void deleteUserTypeRule(UUID feeRuleId) {
        FeeRuleEntity rule = feeRuleRepository.findById(feeRuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee rule not found with id: " + feeRuleId));
        rule.setActive(false); // soft deleting it (we will still have the row in database)
        feeRuleRepository.save(rule);
    }

    // serving CREATE user-specific rule

    public FeeRuleResponse createUserSpecificRule(String userId, CreateFeeRuleRequest request) {
        // converting enums
        TransactionType txType = feeRuleApiMapper.toTransactionType(request.getTransactionType());
        Currency srcCurrency = feeRuleApiMapper.toCurrency(request.getSourceCurrency());
        Currency dstCurrency = feeRuleApiMapper.toCurrency(request.getDestinationCurrency());
        UserType userType = feeRuleApiMapper.toUserType(request.getUserType());

        // validation
        feeRuleValidator.validateSide(request.getSenderFee(), request.getReceiverFee());
        feeRuleValidator.validateNotDuplicate(userId, userType, txType, srcCurrency, dstCurrency);

        // creating rule entity
        FeeRuleEntity feeRuleEntity = FeeRuleEntity.builder().
                ruleId(UUID.randomUUID()).
                userId(userId).
                userType(userType).
                transactionType(txType).
                destinationCurrency(dstCurrency).
                sourceCurrency(srcCurrency).
                effectiveDate(LocalDateTime.now()).
                active(true).
                description(request.getDescription()).
                sideDefinitions(new ArrayList<>()).
                build();

        // Handling the sender fee side definition
        if (request.getSenderFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getSenderFee(), feeRuleEntity, "SENDER")
            );
        }

        if (request.getReceiverFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getReceiverFee(), feeRuleEntity, "RECEIVER")
            );
        }


        FeeRuleEntity savedEntity = feeRuleRepository.save(feeRuleEntity);

        // convert entity to response
        return feeRuleApiMapper.toRuleResponse(savedEntity);
    }

    // serving GET  user-specific fee rules
    public List<FeeRuleResponse> getUserSpecificRules(String userId) {
        return feeRuleRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(feeRuleApiMapper::toRuleResponse)
                .toList();
    }

    // serving PUT user-specific fee rules
    public FeeRuleResponse updateUserSpecificRule(String userId, UUID feeRuleId, CreateFeeRuleRequest request) {
        // converting enums
        TransactionType txType = feeRuleApiMapper.toTransactionType(request.getTransactionType());
        Currency srcCurrency = feeRuleApiMapper.toCurrency(request.getSourceCurrency());
        Currency dstCurrency = feeRuleApiMapper.toCurrency(request.getDestinationCurrency());
        UserType userType = feeRuleApiMapper.toUserType(request.getUserType());

        // validation
        feeRuleValidator.validateSide(request.getSenderFee(), request.getReceiverFee());
        feeRuleValidator.validateNotDuplicateForUpdate(feeRuleId, userId, userType, txType, srcCurrency, dstCurrency);

        // creating Entity
        FeeRuleEntity feeRuleEntity = feeRuleRepository.findById(feeRuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee rule not found with id: " + feeRuleId));

        feeRuleEntity.setUserId(userId);
        feeRuleEntity.setUserType(userType);
        feeRuleEntity.setTransactionType(txType);
        feeRuleEntity.setDestinationCurrency(dstCurrency);
        feeRuleEntity.setSourceCurrency(srcCurrency);
        feeRuleEntity.setDescription(request.getDescription());

        feeRuleEntity.getSideDefinitions().clear();  // clear existing side definitions
        feeRuleRepository.saveAndFlush(feeRuleEntity);   // force the delete to hit the DB now

        if (request.getSenderFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getSenderFee(), feeRuleEntity, "SENDER")
            );
        }

        if (request.getReceiverFee() != null) {
            feeRuleEntity.getSideDefinitions().add(
                    feeRuleApiMapper.toSideEntity(request.getReceiverFee(), feeRuleEntity, "RECEIVER")
            );
        }

        FeeRuleEntity updatedEntity = feeRuleRepository.save(feeRuleEntity);
        return feeRuleApiMapper.toRuleResponse(updatedEntity);
    }

    // serving DELETE user-specific fee rules
    public void deleteUserSpecificRule(String userId, UUID feeRuleId) {
        FeeRuleEntity rule = feeRuleRepository.findById(feeRuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee rule not found with id: " + feeRuleId));
        rule.setActive(false); // soft deleting it (we will still have the row in database)
        feeRuleRepository.save(rule);
    }

}