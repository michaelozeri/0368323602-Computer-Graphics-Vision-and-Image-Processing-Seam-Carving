package com.seamCarving.core;

import java.util.Optional;

public interface SeamCarvingValidator {

    Optional<SeamCarvingValidationError> validate(SeamCarvingDto seamCarvingDto);

}
