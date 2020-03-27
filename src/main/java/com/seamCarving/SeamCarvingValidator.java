package com.seamCarving;

import java.util.Optional;

public interface SeamCarvingValidator {

    Optional<SeamCarvingValidationError> validate(SeamCarvingDto seamCarvingDto);

}
