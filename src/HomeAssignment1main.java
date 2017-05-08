import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * seam carving algorithm - content aware resizing
 * @param inputimagepath - Full path to the input image
 * @param outputnumcolumns - Number of columns of the resized output image
 * @param outputnumrows - Number of rows of the resized output image
 * @param energytype - An argument with three possible values, where '0' = regular
 * energy without entropy term, '1' = regular energy with entropy term and '2' = forward energy
 * @param outputimagepath - Full path to the output image (where your program 
 * will write the output image to)
 * @version 1.00
 * @author Dor Alt & Michael Ozeri
 */
public class HomeAssignment1main {
	
	public static void main(String[] args){
		
		//this is a boolean to decide if to remove a straight seam or general
		boolean straight_seam = false; 
		//this is a boolean to decide if to calculate interpolation when adding seam
		boolean add_interpolation = true;
		
		if(args.length != 5){
			System.out.println("ERROR: not enough arguments inserted");
			System.exit(-1); 
		}
		
		int originalnumofrows=0;
		int originalnumofcolumns=0;
		
		//Full path to the input image
		String inputimagepath = args[0];
		//Number of columns of the resized output image
		int outputnumcolumns = Integer.parseInt(args[1]);
		//Number of rows of the resized output image
		int outputnumrows = Integer.parseInt(args[2]);
		/*An argument with three possible values, where '0' = regular
		energy without entropy term, '1' = regular energy with entropy term and '2' = forward energy*/
		int energytype = Integer.parseInt(args[3]);
		/*Full path to the output image (where your
		program will write the output image to)*/
		String outputimagepath = args[4];
		
		try{
			//opening the photo and storing it into buffered image
			File inputimagefile = new File(inputimagepath);
			BufferedImage inputimagebuffer = ImageIO.read(inputimagefile);
			originalnumofrows =  inputimagebuffer.getHeight();
			originalnumofcolumns =  inputimagebuffer.getWidth();
						
			//check how much to resize vertically
			int resizenumber = originalnumofcolumns - outputnumcolumns;
			
			// if we need to reduce vertical seam's
			if (resizenumber > 0){
				if(straight_seam){
					for(;resizenumber>0;resizenumber--){
						inputimagebuffer = ImageUtils.remove_Straight_Seam(inputimagebuffer, energytype);
					}
						
				}
				else{
					for(;resizenumber>0;resizenumber--){
						inputimagebuffer =  ImageUtils.remove_General_seam(inputimagebuffer, energytype);
					}
				}
			}
			// need to add vertical seams's
			else if(resizenumber < 0){ 
				if(add_interpolation){
					inputimagebuffer = ImageUtils.add_single_seam_with_interpolation(inputimagebuffer, energytype, -resizenumber);
				}else{
					inputimagebuffer = ImageUtils.add_single_seam(inputimagebuffer, energytype, -resizenumber);
				}
			}
			
			
			inputimagebuffer = ImageUtils.transpose_Image(inputimagebuffer);
			
			//check how much to resize vertically
			resizenumber = originalnumofrows - outputnumrows;
			
			// if we need to reduce vertical seam's
			if (resizenumber > 0){
				if(straight_seam){
					for(;resizenumber>0;resizenumber--){
						inputimagebuffer = ImageUtils.remove_Straight_Seam(inputimagebuffer, energytype);
					}
				}
				else{
					for(;resizenumber>0;resizenumber--){
						inputimagebuffer =  ImageUtils.remove_General_seam(inputimagebuffer, energytype);
					}
				}
			}
			// need to add vertical seams's
			else if(resizenumber < 0){ 
				if(add_interpolation){
					inputimagebuffer = ImageUtils.add_single_seam_with_interpolation(inputimagebuffer, energytype, -resizenumber);
				}else{
					inputimagebuffer = ImageUtils.add_single_seam(inputimagebuffer, energytype, -resizenumber);
				}
			}
			
			//transpose image back
			inputimagebuffer = ImageUtils.transpose_Image(inputimagebuffer);
			
			File outputimagefile = new File(outputimagepath);
			ImageIO.write(inputimagebuffer, "jpg", outputimagefile); 
			
			System.out.println("Done!");
			
		}catch (IOException e){
			System.out.println("IOException: "+e.getMessage());
		}
		catch (Exception e){
			System.out.println("ERROR: "+e.getMessage());
		}
		
	}

}
