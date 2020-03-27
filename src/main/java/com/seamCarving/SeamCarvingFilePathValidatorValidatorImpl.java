package com.seamCarving;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class SeamCarvingFilePathValidatorValidatorImpl implements SeamCarvingValidator {

    @Override
    public Optional<SeamCarvingValidationError> validate(SeamCarvingDto seamCarvingDto) {
        if (seamCarvingDto.getInputImagePath() == null) {
            return Optional.of(SeamCarvingValidationError.INPUT_IMAGE_EMPTY);
        }
        File file = new File(seamCarvingDto.getInputImagePath());
        if (!file.exists()) {
            return Optional.of(SeamCarvingValidationError.INPUT_FILE_DOES_NOT_EXIST);
        }
        if (seamCarvingDto.getOutputImagePath() == null) {
            return Optional.of(SeamCarvingValidationError.OUTPUT_IMAGE_EMPTY);
        }
        return Optional.empty();
    }

}
