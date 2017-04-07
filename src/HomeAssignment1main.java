import java.io.File;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.*;


public class HomeAssignment1main {
	public static int[][] transposeMatrix(int [][] image){
        int[][] transposed = new int[image[0].length][image.length];
        for (int i = 0; i < image.length; i++)
            for (int j = 0; j < image[0].length; j++)
            	transposed[j][i] = image[i][j];
        return transposed;
    }
	public static int[][] calcuateMinSeam(int [][] energy){
		int[][] minSeam = new int[energy[0].length][energy.length];
		for (int i = 1; i < energy.length; i++)//go through every row
            for (int j = 0; j < energy[0].length; j++){//add the value of energy above
            	if(j==0){
            		minSeam[i][j] += Math.min(energy[i-1][j],energy[i-1][j-1]);
            	}
            	else if(j==energy[0].length-1){
            		minSeam[i][j] += Math.min(energy[i-1][j],energy[i-1][j-1]);
            	}
            	else
            		minSeam[i][j] += Math.min(energy[i-1][j],Math.min(energy[i-1][j-1],energy[i-1][j+1]));
            }
        return minSeam;
		
	}
	public static int[][] calcuateStraightSeam(int [][] energy){
		int[][] straightsSeam = new int[energy[0].length][energy.length];
		for (int i = 1; i < energy.length; i++)//go through every row
            for (int j = 0; j < energy[0].length; j++)//add the value of energy above
            	straightsSeam[i][j] += energy[i-1][j];
        return straightsSeam;
		
	}
	public static void main(String[] args){
		
		if(args.length != 5){
			System.out.println("ERROR: not enough arguments inserted");
			System.exit(0); //TODO: check if o.k to exit like this	
		}
		
		int originalnumofrows = 0,originalnumofcolumns=0;
		
		//Full path to the input image
		String inputimagepath = args[0];
		//Number of columns of the resized output image
		int outputnumcolumns = Integer.parseInt(args[1]);
		//Number of rows of the resized output image
		int outputnumrows = Integer.parseInt(args[2]);
		/*An argument with three possible values, where '0' = regular
		energy without entropy term, '1' = regular energy with entropy term and '2' =
		forward energy*/
		int energytype = Integer.parseInt(args[3]);
		/*Full path to the output image (where your
		program will write the output image to)*/
		String outputimagepath = args[4];
		
		//opening the photo and storing it into bufferedimage
		try{
			File inputimagefile = new File(inputimagepath);
			BufferedImage inputimagebuffer = ImageIO.read(inputimagefile);
			originalnumofrows =  inputimagebuffer.getWidth();
			originalnumofcolumns =  inputimagebuffer.getHeight();
			
			//compute energy of image
			int[][] energymatrix  = ImageUtils.Calculate_Energy(inputimagebuffer, energytype);
			
			//check how much to resize vertically
			int resizenumber = originalnumofcolumns - outputnumcolumns;
			if (resizenumber > 0){ // if we need to reduce vertical seam's
				for(;resizenumber>0;resizenumber--){
					inputimagebuffer =  ImageUtils.Remove_seam(inputimagebuffer, energymatrix, 0);
				}
			}
			else if(resizenumber < 0){ // need to add vertical seams's
				//TODO: part 2, need to implement add function	
			}
			
			//TODO: part 2 - flip, remove / add seam's
			
			File outputimagefile = new File(outputimagepath);
			ImageIO.write(inputimagebuffer, "png", outputimagefile); //TODO: check return value
			
		}catch (IOException e){
			System.out.println("IOException: "+e.getMessage());
		}
		catch (Exception e){
			System.out.println("ERROR: "+e.getMessage());
		}
		
	}

}
