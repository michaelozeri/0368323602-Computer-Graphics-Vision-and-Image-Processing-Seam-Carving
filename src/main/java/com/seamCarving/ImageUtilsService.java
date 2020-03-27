package com.seamCarving;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;

@Service("imageUtilsService")
public class ImageUtilsService {


    // a boolean deciding if to log or not
    private static final boolean M_LOG = false;

    /**
     * prints the matrix into a log file for debug purposes
     */
    private void print_Mat_To_Logfile(double[][] mat, String filename) {
        if (M_LOG) {
            try {
                FileWriter f = new FileWriter(filename + ".txt");
                BufferedWriter bf = new BufferedWriter(f);
                bf.write("******* the matrix *********\n");
                for (int i = 0; i < mat.length; i++) {
                    for (int j = 0; j < mat[0].length; j++) {
                        if (mat[i][j] == Integer.MAX_VALUE) {
                            bf.write("XXX ");
                        } else if (mat[i][j] > 99) {
                            bf.write(mat[i][j] + " ");
                        } else if ((mat[i][j] < 100) && (mat[i][j] > 9)) {
                            bf.write(mat[i][j] + "  ");
                        } else {
                            bf.write(mat[i][j] + "   ");
                        }
                    }
                    bf.write("\n");
                }
                bf.write("****** end of log *******\n");
                bf.close();
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * this function calculates the energy matrix for the image given as 'image'
     *
     * @param energytype - means how to calculate the energy (with / without local entropy
     * @return energymatrix - the energy matrix of the image given
     */
    public double[][] calculateEnergy(BufferedImage image, EnergyType energytype) {

        int m = image.getHeight();
        int n = image.getWidth();
        double energy_ij = 0, valcount = 0;
        int[][] rgbMatrix = rgbMatrix(image); //returns Matrix of RGB colors
        double[][] energyMatrix = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = Math.max(i - 1, 0); k < Math.min(i + 2, m); k++) {
                    for (int l = Math.max(j - 1, 0); l < Math.min(j + 2, n); l++) {
                        if (i != k || l != j) {
                            valcount++;
                            energy_ij += calculateDistance(rgbMatrix[i][j], rgbMatrix[k][l]);
                        }
                    }
                }
                energy_ij /= valcount;
                energyMatrix[i][j] = energy_ij;
                energy_ij = 0;
                valcount = 0;
            }
        }

        if (EnergyType.REGULAR_WITH_ENTROPY.equals(energytype)) {
            addLocalEntropy(energyMatrix, rgbMatrix);
        }

        return energyMatrix;
    }

    /**
     * gets an image and evaluates the RGB matrix
     *
     * @return an int[][] matrix representing the image
     */
    private int[][] rgbMatrix(BufferedImage image) {
        int m = image.getHeight();
        int n = image.getWidth();
        int[][] rgbmat = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                rgbmat[i][j] = image.getRGB(j, i);
            }
        }
        return rgbmat;
    }

    /**
     * removes a general seam as described in the Assignment
     * this function removes one vertical seam from the image
     *
     * @return newImage - the image with the seam removed
     */
    public BufferedImage removeGeneralSeam(BufferedImage originalimage, EnergyType energytype) {

        int rows = originalimage.getHeight();
        int cols = originalimage.getWidth();

        double[][] energymat = calculateEnergy(originalimage, energytype);
        double[][] dynamicEnergyMat;

        if (!EnergyType.FORWARD_ENERGY.equals(energytype)) {
            //calculate pixel attribute
            dynamicEnergyMat = calculatePixelAttribute(energymat);
            print_Mat_To_Logfile(dynamicEnergyMat, "atributefirst");
        } else {
            dynamicEnergyMat = calculateForwardAttribute(originalimage, energytype, energymat);
        }

        //calculate minimal seam path - vector representing indexes
        int[] seam = calculate_General_Seam(dynamicEnergyMat);

        //create new image with one less column (width-1)
        BufferedImage newImage = new BufferedImage(originalimage.getWidth() - 1, originalimage.getHeight(), originalimage.getType());

        int k = 0; //i of new mat
        int l = 0; //j of new mat

        //copy only wanted pixels to new picture
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j == seam[rows - 1 - i]) {
                    continue;
                }
                newImage.setRGB(l, k, originalimage.getRGB(j, i));
                l++;
            }
            l = 0;
            k++;
        }
        return newImage;
    }

    /**
     * calculates the pixel attribute before calculating seam path
     *
     * @param energymatrix - the matrix on whom to calculate the pixel attribute before calculating the minimal
     *                     seam
     * @return atrib - the matrix energymatrix after the pixel attribute calculation
     */
    private double[][] calculatePixelAttribute(double[][] energymatrix) {
        int m = energymatrix.length; //m=rows
        int n = energymatrix[0].length;
        double tmpleft = Integer.MAX_VALUE;
        double tmpmid = Integer.MAX_VALUE;
        double tmpright = Integer.MAX_VALUE;
        ;
        double[][] atrib = new double[m][n];
        for (int i = 1; i < m; i++) {
            for (int j = 0; j < n; j++) {
                //calculate energy values
                if (j != 0) {
                    tmpleft = energymatrix[i - 1][j - 1];
                }
                if (j != n - 1) {
                    tmpright = energymatrix[i - 1][j + 1];
                }
                tmpmid = energymatrix[i - 1][j];
                //store attribute in matrix
                atrib[i][j] = energymatrix[i][j] + Math.min(Math.min(tmpleft, tmpright), tmpmid);
                //reset values of tmp
                tmpleft = Integer.MAX_VALUE;
                tmpmid = Integer.MAX_VALUE;
                tmpright = Integer.MAX_VALUE;
            }
        }
        return atrib;
    }

    /*
     * this function calculates the minimal seam to remove and returns it as a vector
     * */
    private int[] calculate_General_Seam(double[][] atrib) {

        int rows = atrib.length; //m=rows
        int cols = atrib[0].length;
        double min = Integer.MAX_VALUE;
        int minIndex = 0;
        int[] seam = new int[rows];
        //find first min val
        for (int j = 0; j < cols; j++) {
            if (atrib[rows - 1][j] < min) {
                min = atrib[rows - 1][j];
                minIndex = j;
            }
        }
        atrib[rows - 1][minIndex] = Integer.MAX_VALUE;
        //this was a logger function that prints the matrix into a logfile
        //print_Mat_To_Logfile(atrib,"log1");
        seam[0] = minIndex;
        double tmpleft = Integer.MAX_VALUE;
        double tmpmid = Integer.MAX_VALUE;
        double tmpright = Integer.MAX_VALUE;
        for (int i = 1; i < seam.length; i++) {
            if (seam[i - 1] != 0) {
                tmpleft = atrib[rows - 1 - i][seam[i - 1] - 1];
            }
            if (seam[i - 1] != cols - 1) {
                tmpright = atrib[rows - 1 - i][seam[i - 1] + 1];
            }
            tmpmid = atrib[rows - 1 - i][seam[i - 1]];
            min = Math.min(tmpleft, Math.min(tmpright, tmpmid));
            if (min == tmpmid) {
                seam[i] = seam[i - 1];
                atrib[rows - 1 - i][seam[i - 1]] = Integer.MAX_VALUE;
            } else if (min == tmpleft) {
                seam[i] = seam[i - 1] - 1;
                atrib[rows - 1 - i][seam[i - 1] - 1] = Integer.MAX_VALUE;
            } else {
                seam[i] = seam[i - 1] + 1;
                atrib[rows - 1 - i][seam[i - 1] + 1] = Integer.MAX_VALUE;
            }
            //this was a logger function that prints the matrix into a logfile
            //print_Mat_To_Logfile(atrib,"log1");
            //reset tmp values
            tmpleft = Integer.MAX_VALUE;
            tmpmid = Integer.MAX_VALUE;
            tmpright = Integer.MAX_VALUE;
        }
        return seam;
    }


    /**
     * removes straight seams from the image for the 'straight_seam' implementation
     * need's to calculate energy only once
     *
     * @param originalImage - the image from which to remove the seams
     * @param energyType    - the energy type calculation of the energy matrix
     *                      colsToRemove - how many seams to remove
     * @return the new picture without the seams
     */
    public BufferedImage removeStraightSeam(BufferedImage originalImage, EnergyType energyType) {
        int m = originalImage.getWidth();
        int n = originalImage.getHeight();
        //calculate the energy matrix
        double[][] energyMat = calculateEnergy(originalImage, energyType);
        //calculate a vector that each index holds the column seam value
        double[] seamVector = calculateStraightSeam(energyMat);
        //choose the minimum seam to remove from the seam vector
        double maxDub = Double.MAX_VALUE;
			/*for(int i = 0; i<ColToRemove; i++){//remove all seams with one energy calculation
				int minindex = 0;
				double min = seamVector[0];
				for(int j =1; j<m;j++){
					double temp = seamVector[j];
					if(temp<min){
						minindex = j;
						min = seamVector[j];
					}
				}
				seamVector[minindex] = maxdub;
			}*/
        int minindex = 0;
        double min = seamVector[0];
        for (int j = 1; j < m; j++) {
            double temp = seamVector[j];
            if (temp < min) {
                minindex = j;
                min = seamVector[j];
            }
        }
        seamVector[minindex] = maxDub;
        BufferedImage newImage = new BufferedImage(originalImage.getWidth() - 1, originalImage.getHeight(), originalImage.getType());

        for (int i = 0; i < n; i++) {
            int c = 0;
            for (int j = 0; j < m; j++) {
                if (seamVector[j] == maxDub) {
                    continue;
                }
                newImage.setRGB(c, i, originalImage.getRGB(j, i));
                c++;
            }
        }
        return newImage;
    }

    /**
     * this function calculates the straight seam to be removed by summing
     * each column to a double value
     *
     * @return straightsSeam - a double array which each index represents the column energy
     */
    public double[] calculateStraightSeam(double[][] energymatrix) {
        //value per each column
        double[] straightsSeam = new double[energymatrix[0].length];
        for (int i = 0; i < energymatrix.length; i++)//go through every row
            for (int j = 0; j < energymatrix[0].length; j++)//add the value of energy above
                straightsSeam[j] += energymatrix[i][j];
        return straightsSeam;
    }

    /**
     * this function is called in case we want to add the local entropy to the energy function
     *
     * @return the energy matrix with local entropy
     */
    private void addLocalEntropy(double[][] matrix, int[][] rgbmat) {
        int m = matrix.length;
        int n = matrix[0].length;
        double[][] pmnMat = calculate_pmn(rgbmat);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double hi = 0;
                for (int k = Math.max(i - 4, 0); k < Math.min(i + 5, m); k++) {
                    for (int l = Math.max(j - 4, 0); l < Math.min(j + 5, n); l++) {
                        if (i != k || l != j) {
                            hi += (pmnMat[k][l] * Math.log(pmnMat[k][l]));
                        }
                    }
                }
                matrix[i][j] -= hi;
            }
        }
    }

    /**
     * gets an RGB matrix and evaluates the p(m,n) value for each pixel
     *
     * @param rgbmat - the matrix from to evaluate the pmn value
     * @return pmnMat - the matrix with the P(m,n) values already in it
     */
    private double[][] calculate_pmn(int[][] rgbmat) {
        int m = rgbmat.length;
        int n = rgbmat[0].length;
        double[][] greyscaleMat = grayScale(rgbmat);
        double[][] pmnMat = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double temppmn = 0;
                for (int k = Math.max(i - 4, 0); k < Math.min(i + 5, m); k++) {
                    for (int l = Math.max(j - 4, 0); l < Math.min(j + 5, n); l++) {
                        if (i != k || l != j) {
                            temppmn += greyscaleMat[k][l];
                        }
                    }
                }
                pmnMat[i][j] = greyscaleMat[i][j] / temppmn;
            }
        }
        return pmnMat;
    }

    /**
     * this function calculates the distance between two RGB points (which are int's)
     * as described in the Assignment
     *
     * @return int - the distance between the two points
     */
    private int calculateDistance(int orig, int second) {
        Color one = new Color(orig);
        Color two = new Color(second);
        return (Math.abs(one.getRed() - two.getRed()) + Math.abs(one.getBlue() - two.getBlue()) + Math.abs(one.getGreen() - two.getGreen())) / 3;
    }

    /**
     * this function calculates the minimal seam for straight seam curving function
     *
     * @param energy - the energy matrix to calculate from it the minimal seam
     */
    public int[][] calculateMinSeam(double[][] energy, int colToadd, EnergyType energytype, BufferedImage originalImage) {
        double[][] attribute;
        if (!EnergyType.FORWARD_ENERGY.equals(energytype)) {
            attribute = calculatePixelAttribute(energy);
        } else {
            attribute = calculateForwardAttribute(originalImage, energytype, energy);
        }
        int m = energy[0].length;
        int n = energy.length;
        int[][] minSeam = new int[n][m];//counter matrix, will tell us how many times t duplicate each pixel
        double maxdubVal;
        int loops = 0; //with loops we will be able to know how many times we multiplied the picture
        for (int i = 0; i < colToadd; i++) {
            double min = attribute[n - 1][0];
            int minindex = 0;
            while (minSeam[n - 1][minindex] != loops && minindex < m) {//find the first index we havent chosen
                minindex++;
            }
            if (minindex == m) {//find the first pixel we havent chosen
                loops++;
                minindex = 0;
            }
            maxdubVal = attribute[n - 1][0];//max val
            min = attribute[n - 1][minindex];//minimum value will start from the first index we haven chosen
            for (int j = minindex + 1; j < m; j++) {
                double temp = attribute[n - 1][j];
                if (temp < min && minSeam[n - 1][j] == loops) {//get min val(one we havent chosen yet
                    minindex = j;
                    min = temp;
                } else if (temp > maxdubVal) {//get max val
                    maxdubVal = temp;
                }
            }
            attribute[n - 1][minindex] = maxdubVal;
            energy[n - 1][minindex] += (maxdubVal + 1000); //add energy to the min pixel
            minSeam[n - 1][minindex]++; //sign that we chosed the pixel
            for (int r = n - 1; r > 0; r--) {//find the minimal path
                if (minindex == 0) {
                    int minloop = Math.min(minSeam[r - 1][minindex], minSeam[r - 1][minindex + 1]);
                    if (attribute[r - 1][minindex] > attribute[r - 1][minindex + 1] && minSeam[r - 1][minindex + 1] == minloop)
                        minindex = minindex + 1;
                } else if (minindex == m - 1) {
                    int minloop = Math.min(minSeam[r - 1][minindex], minSeam[r - 1][minindex - 1]);
                    if (attribute[r - 1][minindex] > attribute[r - 1][minindex - 1] && minSeam[r - 1][minindex - 1] == minloop)
                        minindex = minindex - 1;
                } else {
                    int minloop = Math.min(minSeam[r - 1][minindex - 1], minSeam[r - 1][minindex + 1]);
                    minloop = Math.min(minSeam[r - 1][minindex], minloop);
                    if (minSeam[r - 1][minindex - 1] == minloop && minSeam[r - 1][minindex] == minloop && minSeam[r - 1][minindex + 1] == minloop) {//we would prefer not to pass go through the same seam more than one so we check if there's a way without a pixel we've doubled
                        min = Math.min(attribute[r - 1][minindex - 1], attribute[r - 1][minindex]);
                        min = Math.min(attribute[r - 1][minindex + 1], min);
                    } else if (minSeam[r - 1][minindex - 1] == minloop && minSeam[r - 1][minindex] == minloop) {//only the one above and to the left havent been chosen already
                        min = Math.min(attribute[r - 1][minindex - 1], attribute[r - 1][minindex]);
                    } else if (minSeam[r - 1][minindex + 1] == minloop && minSeam[r - 1][minindex] == minloop) {//only the one above and to the right havent been chosen already
                        min = Math.min(attribute[r - 1][minindex + 1], attribute[r - 1][minindex]);
                    } else if (minSeam[r - 1][minindex + 1] == minloop && minSeam[r - 1][minindex - 1] == minloop) {//only the one to the left and to the one right havent been chosen already
                        min = Math.min(attribute[r - 1][minindex + 1], attribute[r - 1][minindex - 1]);
                    } else if (minSeam[r - 1][minindex - 1] == minloop) {//only the one to the left havent been chosen already
                        min = attribute[r - 1][minindex - 1];
                    } else if (minSeam[r - 1][minindex + 1] == minloop) {//only the one to the right havent been chosen already
                        min = attribute[r - 1][minindex + 1];
                    }
                    if (min == attribute[r - 1][minindex + 1]) {//we check which one compares to min
                        minindex = minindex + 1;
                    } else if (min == attribute[r - 1][minindex - 1])//we check which one compares to min
                        minindex = minindex - 1;
                }
                minSeam[r - 1][minindex]++;//sign that we chosed the pixel
                energy[r - 1][minindex] += (maxdubVal + 1000);//add energy to the chosen pixel
            }
            if (!EnergyType.FORWARD_ENERGY.equals(energytype)) {//calculate attribute again
                attribute = calculatePixelAttribute(energy);
            } else {
                attribute = calculateForwardAttribute(originalImage, energytype, energy);
            }
        }
        return minSeam;

    }

    /**
     * this function gets an RGB image and evaluates the grey scale matrix
     *
     * @param rgbmat - the matrix representing the image which to calculate the gray scale values from
     * @return greyscaleMat - a matrix representing the gray scale value of the color matrix image
     */
    private double[][] grayScale(int[][] rgbmat) {
        int m = rgbmat.length;
        int n = rgbmat[0].length;
        double[][] greyscaleMat = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Color c = new Color(rgbmat[i][j]);
                int red = (int) (c.getRed() * 0.299);//the numbers were found on the web
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                greyscaleMat[i][j] = red + green + blue;
            }
        }
        return greyscaleMat;
    }

    /**
     * this function adds a single seam to the picture and Blends the added seam by interpolation with its neighbors
     *
     * @param originalimage - the original image to add the seam to
     * @param energytype    - the energy type of the picture
     * @param colToadd      - number of cols to add
     * @return newImage - the new image with the added seam
     */
    public BufferedImage addSingleSeamWithInterpolation(BufferedImage originalimage, EnergyType energytype, int colToadd) {
        int m = originalimage.getWidth();
        int n = originalimage.getHeight();
        double[][] energyMatrix = calculateEnergy(originalimage, energytype);
        //calculate min sim attribute
        int[][] seam = calculateMinSeam(energyMatrix, colToadd, energytype, originalimage);
        BufferedImage newImage = new BufferedImage(m + colToadd, n, originalimage.getType());
        for (int i = 0; i < n; i++) {
            int c = 0;
            for (int j = 0; j < m; j++) {
                newImage.setRGB(c, i, originalimage.getRGB(j, i));
                if (seam[i][j] > 0) {
                    int rgbval = originalimage.getRGB(j, i);

                    for (int k = 0; k < seam[i][j]; k++) {
                        c++;
                        if (j != m - 1) {
                            rgbval = calculate_avrage_color(rgbval, originalimage.getRGB(j + 1, i));
                        }
                        newImage.setRGB(c, i, rgbval);
                    }
                }
                c++;
            }
        }
        return newImage;
    }

    /**
     * this function calculates the forward energy before calculating the seam.
     *
     * @param image      - the image to calculate on it the forward energy
     * @param energytype - the energy type asked in the exercise
     * @return minSeam - the minimal seam to remove from the picture calculated by the forwarding method
     */
    public double[][] calculateForwardAttribute(BufferedImage image, EnergyType energytype, double[][] energy) {
        //double[][] energy = calculate_Energy(image, energytype);
        int[][] rgbmat = rgbMatrix(image);
        double[][] minSeam = new double[energy.length][energy[0].length];
        for (int i = 1; i < energy.length; i++)//go through every row
            for (int j = 0; j < energy[0].length; j++) {//add the value of energy above
                double cl = 0;
                double cr = 0;
                double cu = 0;
                if ((j != 0 && j != (energy[0].length - 1))) {//i will never be 0
                    cl = calculateDistance(rgbmat[i][j - 1], rgbmat[i][j + 1]) + calculateDistance(rgbmat[i - 1][j], rgbmat[i][j - 1]);
                    cr = calculateDistance(rgbmat[i][j - 1], rgbmat[i][j + 1]) + calculateDistance(rgbmat[i - 1][j], rgbmat[i][j + 1]);
                    cu = calculateDistance(rgbmat[i][j - 1], rgbmat[i][j + 1]);
                }
                if (j == 0) {
                    minSeam[i][j] += Math.min(energy[i - 1][j] + cu, energy[i - 1][j + 1] + cr);
                } else if (j == (energy[0].length - 1)) {
                    minSeam[i][j] += Math.min(energy[i - 1][j] + cu, energy[i - 1][j - 1] + cl);
                } else {
                    minSeam[i][j] += Math.min(energy[i - 1][j] + cu, Math.min(energy[i - 1][j - 1] + cl, energy[i - 1][j + 1] + cr));
                }
            }
        return minSeam;
    }

    public BufferedImage addSingleSeam(BufferedImage originalimage, EnergyType energytype, int colToadd) {
        int m = originalimage.getWidth();
        int n = originalimage.getHeight();
        double[][] energymat = calculateEnergy(originalimage, energytype);

        //calculate min sim attribute
        int[][] seam = calculateMinSeam(energymat, colToadd, energytype, originalimage);

        BufferedImage newImage = new BufferedImage(m + colToadd, n, originalimage.getType());
        for (int i = 0; i < n; i++) {
            int c = 0;
            for (int j = 0; j < m; j++) {
                newImage.setRGB(c, i, originalimage.getRGB(j, i));
                if (seam[i][j] > 0) {
                    int rgbval = originalimage.getRGB(j, i);
                    for (int k = 0; k < seam[i][j]; k++) {
                        c++;
                        newImage.setRGB(c, i, rgbval);
                    }
                }
                c++;
            }
        }
        return newImage;
    }

    /**
     * transpose's the image
     *
     * @param img - the image to transpose
     * @return retimg - the image transposed
     */
    public BufferedImage transposeImage(BufferedImage img) {
        int m = img.getHeight();
        int n = img.getWidth();
        BufferedImage retimg = new BufferedImage(m, n, img.getType());
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                retimg.setRGB(i, j, img.getRGB(j, i));
            }
        }
        return retimg;
    }

    /*
     * removes a general seam as described in the Assignment
     * */
    public BufferedImage remove_General_seam_Energy_Once(BufferedImage originalimage, EnergyType energytype, int resizeNum) {

        double[][] energyMatrix = calculateEnergy(originalimage, energytype);

        //calculate pixel attribute
        double[][] atrib = calculatePixelAttribute(energyMatrix);
        print_Mat_To_Logfile(atrib, "atributefirst");

        for (; resizeNum > 0; resizeNum--) {
            //calculate minimal seam path - vector representing indexes
            atrib = calculate_General_Seam_static(atrib);
            //create new image with one less column (width-1)
            originalimage = calculate_new_image(originalimage, atrib);
            atrib = calculate_new_atribute(atrib);
        }

        //return new image
        return originalimage;
    }

    /**
     * calculate's the avrage color between two colors given
     *
     * @param clr1 - an int representing a RGB color by its bit's
     * @param clr2 - an int representing a RGB color by its bit's
     * @return the avrage color represented by an int
     */
    private int calculate_avrage_color(int clr1, int clr2) {
        Color cx = new Color(clr1);
        Color cy = new Color(clr2);
        int red = (int) ((cx.getRed() + cy.getRed()) / 2);
        int green = (int) ((cx.getGreen() + cx.getGreen()) / 2);
        int blue = (int) ((cy.getBlue() + cy.getBlue()) / 2);
        Color c = new Color(red, green, blue);
        return c.getRGB();
    }

    /**
     * this function calculates the minimal seam to remove and returns it as a vector
     *
     * @param atrib - the energy matrix which to calculate the general seam from
     * @return - double[][] the matrix which the seam is written inside
     */
    private double[][] calculate_General_Seam_static(double[][] atrib) {

        int rows = atrib.length; //m=rows
        int cols = atrib[0].length;
        double min = Integer.MAX_VALUE;
        int minIndex = 0;
        int[] seam = new int[rows];
        //find first min val
        for (int j = 0; j < cols; j++) {
            if (atrib[rows - 1][j] < min) {
                min = atrib[rows - 1][j];
                minIndex = j;
            }
        }
        atrib[rows - 1][minIndex] = Integer.MAX_VALUE;
        //this was a logger to print the matrix to a logfile
        //print_Mat_To_Logfile(atrib,"log1");
        seam[0] = minIndex;
        double tmpleft = Integer.MAX_VALUE;
        double tmpmid = Integer.MAX_VALUE;
        double tmpright = Integer.MAX_VALUE;
        for (int i = 1; i < seam.length; i++) {
            if (seam[i - 1] != 0) {
                tmpleft = atrib[rows - 1 - i][seam[i - 1] - 1];
            }
            if (seam[i - 1] != cols - 1) {
                tmpright = atrib[rows - 1 - i][seam[i - 1] + 1];
            }
            tmpmid = atrib[rows - 1 - i][seam[i - 1]];
            min = Math.min(tmpleft, Math.min(tmpright, tmpmid));
            if (min == tmpmid) {
                seam[i] = seam[i - 1];
                atrib[rows - 1 - i][seam[i - 1]] = Integer.MAX_VALUE;
            } else if (min == tmpleft) {
                seam[i] = seam[i - 1] - 1;
                atrib[rows - 1 - i][seam[i - 1] - 1] = Integer.MAX_VALUE;
            } else {
                seam[i] = seam[i - 1] + 1;
                atrib[rows - 1 - i][seam[i - 1] + 1] = Integer.MAX_VALUE;
            }
            //this was a logger to print the matrix to a logfile
            //print_Mat_To_Logfile(atrib,"log1");
            //reset tmp values
            tmpleft = Integer.MAX_VALUE;
            tmpmid = Integer.MAX_VALUE;
            tmpright = Integer.MAX_VALUE;
        }
        return atrib;
    }

    /**
     * this function removes a seam from the energy matrix
     *
     * @param atrib
     * @return
     */
    private double[][] calculate_new_atribute(double[][] atrib) {

        double[][] atribnew = new double[atrib.length][atrib[0].length - 1];

        int k = 0; //i of new atrib
        int l = 0; //j of new atrib

        //copy only wanted pixels to new picture
        for (int i = 0; i < atrib.length; i++) {
            for (int j = 0; j < atrib[0].length; j++) {
                if (atrib[i][j] == Integer.MAX_VALUE) {
                    continue;
                }
                atribnew[k][l] = atrib[i][j];
                l++;
            }
            l = 0;
            k++;
        }
        return atribnew;
    }

    /**
     * this function removes a seam decided in atrib from the image img
     *
     * @param img
     * @param atrib
     * @return
     */
    private BufferedImage calculate_new_image(BufferedImage img, double[][] atrib) {

        int rows = img.getHeight();
        int cols = img.getWidth();

        //create new image with one less column (width-1)
        BufferedImage newImage = new BufferedImage(img.getWidth() - 1, img.getHeight(), img.getType());

        int k = 0; //i of new mat
        int l = 0; //j of new mat

        //copy only wanted pixels to new picture
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (atrib[i][j] == Integer.MAX_VALUE) {
                    continue;
                }
                newImage.setRGB(l, k, img.getRGB(j, i));
                l++;
            }
            l = 0;
            k++;
        }
        return newImage;
    }
}




