import java.io.File;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.*;


public class HomeAssignment1main {
	
	public static void main(String[] args){
		
		//for submission types TODO: maybe remove this?
		boolean straight_seam = false;
		int tmpcntforresize=0;
		
		if(args.length != 5){
			System.out.println("ERROR: not enough arguments inserted");
			System.exit(0); //TODO: check if o.k to exit like this	
		}
		
		int originalnumofrows = 0;
		int originalnumofcolumns=0;
		
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
			
			// if we need to reduce vertical seam's
			if (resizenumber > 0){
				if(straight_seam){
					for(;resizenumber>0;resizenumber--){
						inputimagebuffer = ImageUtils.Remove_straight_seam(inputimagebuffer, energymatrix, resizenumber);
					}
				}else{
					for(;resizenumber>0;resizenumber--){
						inputimagebuffer =  ImageUtils.remove_General_seam(inputimagebuffer, energymatrix, 0);
						tmpcntforresize++;
					}
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
