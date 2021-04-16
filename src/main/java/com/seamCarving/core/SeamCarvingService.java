package com.seamCarving.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeamCarvingService {

    private final Logger logger = LoggerFactory.getLogger(SeamCarvingService.class);

    @Autowired
    private List<SeamCarvingValidator> validatorList;

    @Resource
    private ImageUtilsService imageUtilsService;

    public void carveSeam(SeamCarvingDto seamCarvingDto) {
        boolean straightSeam = false;
        boolean addInterpolation = true;
        validateInput(seamCarvingDto);
        EnergyType energyType = seamCarvingDto.getEnergyType();
        try {
            logger.info("Started seam carving for image: {}", seamCarvingDto.getInputImagePath());
            File inputImageFile = new File(seamCarvingDto.getInputImagePath());
            BufferedImage inputImageBuffer = ImageIO.read(inputImageFile);
            int originalNumOfRows = inputImageBuffer.getHeight();
            final int resizeNumberHorizontally = inputImageBuffer.getWidth() - seamCarvingDto.getOutputNumColumns();
            final int resizeNumberVertically = originalNumOfRows - seamCarvingDto.getOutputNumRows();
            logger.info("Started horizontal seam carving");
            inputImageBuffer = addOrReduceSeams(straightSeam, addInterpolation, energyType, inputImageBuffer, resizeNumberHorizontally);
            logger.info("Finished horizontal seam carving");
            inputImageBuffer = imageUtilsService.transposeImage(inputImageBuffer);
            logger.info("Started vertical seam carving");
            inputImageBuffer = addOrReduceSeams(straightSeam, addInterpolation, energyType, inputImageBuffer, resizeNumberVertically);
            logger.info("Finished vertical seam carving");
            inputImageBuffer = imageUtilsService.transposeImage(inputImageBuffer);
            File outputimagefile = new File(seamCarvingDto.getOutputImagePath());
            ImageIO.write(inputImageBuffer, "jpg", outputimagefile);
            logger.info("Done!");
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        } catch (Exception e) {
            logger.error("ERROR: " + e.getMessage());
        }
    }

    private BufferedImage addOrReduceSeams(boolean straightSeam, boolean addInterpolation, EnergyType energyType, BufferedImage inputImageBuffer, int resizeNumberOfSeams) {
        inputImageBuffer = resizeNumberOfSeams > 0 ?
                reduceSeams(straightSeam, energyType, inputImageBuffer, resizeNumberOfSeams) :
                addSeams(addInterpolation, energyType, inputImageBuffer, resizeNumberOfSeams);
        return inputImageBuffer;
    }

    private BufferedImage addSeams(boolean addInterpolation, EnergyType energyType, BufferedImage inputImageBuffer, int resizeNumberOfSeams) {
        if (addInterpolation) {
            inputImageBuffer = imageUtilsService.addSingleSeamWithInterpolation(inputImageBuffer, energyType, -resizeNumberOfSeams);
        } else {
            inputImageBuffer = imageUtilsService.addSingleSeam(inputImageBuffer, energyType, -resizeNumberOfSeams);
        }
        return inputImageBuffer;
    }

    private BufferedImage reduceSeams(boolean straightSeam, EnergyType energyType, BufferedImage inputImageBuffer, int resizeNumberOfSeams) {
        if (straightSeam) {
            for (int i = 0; i < resizeNumberOfSeams; i++) {
                inputImageBuffer = imageUtilsService.removeStraightSeam(inputImageBuffer, energyType);
            }
        } else {
            for (int i = 0; i < resizeNumberOfSeams; i++) {
                inputImageBuffer = imageUtilsService.removeGeneralSeam(inputImageBuffer, energyType);
            }
        }
        return inputImageBuffer;
    }

    private void validateInput(SeamCarvingDto seamCarvingDto) {
        List<SeamCarvingValidationError> seamCarvingValidationErrors = validatorList.stream()
                .map(val -> val.validate(seamCarvingDto))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!seamCarvingValidationErrors.isEmpty()) {
            throw new IllegalArgumentException("The following errors have occurred: " + seamCarvingValidationErrors);
        }
    }

}
