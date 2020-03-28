package com.seamCarving;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class seamCarvingControllerTest {

    @InjectMocks
    private SeamCarvingController seamCarvingController;

    @Test
    public void whenRequestingDimensionsAndFileNotExists_ThenCorrectResponseReturned() {

        ResponseEntity<SeamCarvingResponse<Pair<String, String>>> result = seamCarvingController.getImageDimensions("badPath");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Can't read input file!", result.getBody().getErrorMessage());
        assertFalse(result.getBody().isSuccess());

    }


}