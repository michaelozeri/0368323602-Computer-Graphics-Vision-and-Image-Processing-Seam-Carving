package com.seamCarving;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.websocket.server.PathParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;

@Controller
@RequestMapping("/api/SeamCarving")
public class SeamCarvingController {

    private Logger logger = LoggerFactory.getLogger(SeamCarvingController.class);

    @Resource
    private SeamCarvingService seamCarvingService;

    @ResponseBody
    @RequestMapping(path = "/carveSeam", method = RequestMethod.POST)
    public ResponseEntity<SeamCarvingResponse<SeamCarvingDto>> getEventById(@RequestBody SeamCarvingDto seamCarvingDto) {
        try {
            seamCarvingService.carveSeam(seamCarvingDto);
            return new ResponseEntity<>(new SeamCarvingResponse<>(true, "", seamCarvingDto), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Bad input inserted", e);
            return new ResponseEntity<>(new SeamCarvingResponse<>(false, e.getMessage(), seamCarvingDto), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error while carving seam", e);
            return new ResponseEntity<>(new SeamCarvingResponse<>(false, e.getMessage(), seamCarvingDto), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/Dimensions", method = RequestMethod.GET)
    public ResponseEntity<SeamCarvingResponse<Pair<String, String>>> getImageDimensions(@PathParam("inputImagePath") String inputImagePath) {
        try {
            File inputImageFile = new File(inputImagePath);
            BufferedImage inputImageBuffer = ImageIO.read(inputImageFile);
            Pair<String, String> resultPair = Pair.of("Height: " + inputImageBuffer.getHeight(), "Width: " + inputImageBuffer.getWidth());
            return new ResponseEntity<>(new SeamCarvingResponse<>(true, "", resultPair), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while getting dimensions for image {}", inputImagePath, e);
            return new ResponseEntity<>(new SeamCarvingResponse<>(false, e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
