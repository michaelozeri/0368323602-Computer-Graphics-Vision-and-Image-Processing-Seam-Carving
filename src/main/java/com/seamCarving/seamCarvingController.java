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

@Controller
@RequestMapping("/api/SeamCarving")
public class seamCarvingController {

    private Logger logger = LoggerFactory.getLogger(seamCarvingController.class);

    @Resource
    private SeamCarvingService seamCarvingService;

//    {
//        "inputImagePath": "/home/michaelo/Downloads/testImage.jpg",
//            "outputNumColumns": 1300,
//            "outputNumRows": 1100,
//            "energyType": "REGULAR_NO_ENTROPY",
//            "outputImagePath": "/home/michaelo/Downloads/testImageResult.jpg",
//            "straightSeam": true,
//            "addInterpolation": true
//    }

    @ResponseBody
    @RequestMapping(path = "/carveSeam", method = RequestMethod.POST)
    public ResponseEntity<SeamCarvingDto> getEventById(@RequestBody SeamCarvingDto seamCarvingDto) {
        try {
            seamCarvingService.carveSeam(seamCarvingDto);
            return new ResponseEntity<>(seamCarvingDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while carving seam", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/Dimensions", method = RequestMethod.GET)
    public Pair<String, String> getImageDimensions(@PathParam("inputImagePath") String inputImagePath) {
        try {
            File inputImageFile = new File(inputImagePath);
            BufferedImage inputImageBuffer = ImageIO.read(inputImageFile);
            return Pair.of("Height: " + inputImageBuffer.getHeight(), "Width: " + inputImageBuffer.getWidth());
        } catch (Exception e) {
            logger.error("Error while getting dimensions for image {}", inputImagePath, e);
            return null;
        }
    }


}
