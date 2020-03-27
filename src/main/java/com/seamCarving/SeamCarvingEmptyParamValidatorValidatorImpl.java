package com.seamCarving;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SeamCarvingEmptyParamValidatorValidatorImpl implements SeamCarvingValidator {
    @Override
    public Optional<SeamCarvingValidationError> validate(SeamCarvingDto seamCarvingDto) {
        if (seamCarvingDto.getEnergyType() == null) {
            return Optional.of(SeamCarvingValidationError.ENERGY_TYPE_NOT_ENTERED);
        }
        if (seamCarvingDto.getOutputNumColumns() == null) {
            return Optional.of(SeamCarvingValidationError.OUTPUT_NUM_COLUMNS_NOT_ENTERED);
        }
        if (seamCarvingDto.getOutputNumRows() == null) {
            return Optional.of(SeamCarvingValidationError.OUTPUT_NUM_ROWS_NOT_ENTERED);
        }
        return Optional.empty();
    }
}
