import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * seam carving algorithm - content aware resizing
 *
 * @param inputimagepath   - Full path to the input image
 * @param outputnumcolumns - Number of columns of the resized output image
 * @param outputnumrows    - Number of rows of the resized output image
 * @param energytype       - An argument with three possible values, where '0' = regular
 *                         energy without entropy term, '1' = regular energy with entropy term and '2' = forward energy
 * @param outputimagepath  - Full path to the output image (where your program
 *                         will write the output image to)
 * @author Dor Alt & Michael Ozeri
 * @version 1.00
 */
public class HomeAssignment1main {

    public static void main(String[] args) {

        //this is a boolean to decide if to remove a straight seam or general
        boolean straightSeam = false;
        //this is a boolean to decide if to calculate interpolation when adding seam
        boolean addInterpolation = true;

        if (args.length != 5) {
            System.out.println("ERROR: not enough arguments inserted");
            System.exit(-1);
        }

        int originalNumOfRows = 0;
        int originalNumOfColumns = 0;

        //Full path to the input image
        String inputImagePath = args[0];
        //Number of columns of the resized output image
        int outputNumColumns = Integer.parseInt(args[1]);
        //Number of rows of the resized output image
        int outputNumRows = Integer.parseInt(args[2]);
		/*An argument with three possible values, where '0' = regular
		energy without entropy term, '1' = regular energy with entropy term and '2' = forward energy*/
        int energyType = Integer.parseInt(args[3]);
		/*Full path to the output image (where your
		program will write the output image to)*/
        String outputImagePath = args[4];

        try {
            //opening the photo and storing it into buffered image
            File inputImageFile = new File(inputImagePath);
            BufferedImage inputImageBuffer = ImageIO.read(inputImageFile);
            originalNumOfRows = inputImageBuffer.getHeight();
            originalNumOfColumns = inputImageBuffer.getWidth();

            //check how much to resize vertically
            int resizeNumberVertically = originalNumOfColumns - outputNumColumns;

            // if we need to reduce vertical seam's
            if (resizeNumberVertically > 0) {
                if (straightSeam) {
                    for (; resizeNumberVertically > 0; resizeNumberVertically--) {
                        inputImageBuffer = ImageUtils.remove_Straight_Seam(inputImageBuffer, energyType);
                    }

                } else {
                    for (; resizeNumberVertically > 0; resizeNumberVertically--) {
                        inputImageBuffer = ImageUtils.remove_General_seam(inputImageBuffer, energyType);
                    }
                }
            }
            // need to add vertical seams's
            else if (resizeNumberVertically < 0) {
                if (addInterpolation) {
                    inputImageBuffer = ImageUtils.add_single_seam_with_interpolation(inputImageBuffer, energyType, -resizeNumberVertically);
                } else {
                    inputImageBuffer = ImageUtils.add_single_seam(inputImageBuffer, energyType, -resizeNumberVertically);
                }
            }

            inputImageBuffer = ImageUtils.transpose_Image(inputImageBuffer);

            //check how much to resize vertically
            resizeNumberVertically = originalNumOfRows - outputNumRows;

            // if we need to reduce vertical seam's
            if (resizeNumberVertically > 0) {
                if (straightSeam) {
                    for (; resizeNumberVertically > 0; resizeNumberVertically--) {
                        inputImageBuffer = ImageUtils.remove_Straight_Seam(inputImageBuffer, energyType);
                    }
                } else {
                    for (; resizeNumberVertically > 0; resizeNumberVertically--) {
                        inputImageBuffer = ImageUtils.remove_General_seam(inputImageBuffer, energyType);
                    }
                }
            }
            // need to add vertical seams's
            else if (resizeNumberVertically < 0) {
                if (addInterpolation) {
                    inputImageBuffer = ImageUtils.add_single_seam_with_interpolation(inputImageBuffer, energyType, -resizeNumberVertically);
                } else {
                    inputImageBuffer = ImageUtils.add_single_seam(inputImageBuffer, energyType, -resizeNumberVertically);
                }
            }

            //transpose image back
            inputImageBuffer = ImageUtils.transpose_Image(inputImageBuffer);

            File outputimagefile = new File(outputImagePath);
            ImageIO.write(inputImageBuffer, "jpg", outputimagefile);

            System.out.println("Done!");

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

    }

}
