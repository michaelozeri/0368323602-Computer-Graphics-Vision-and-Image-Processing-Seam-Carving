import java.awt.image.BufferedImage;
import java.awt.Color;


public class ImageUtils {
	/*
	 * this function calculates the energy matrix for the image given as 'image'
	 * @param energytype - means how to calculate the energy (with / without local entropy
	 * @return 'energymatrix' - the energy matrix of the image given
	 * */
	public static int[][] Calculate_Energy(BufferedImage image,int energytype){ 
		int m = image.getWidth();
		int n = image.getHeight();
		int energy_ij = 0,valcount=0;;
		int[][] rgbmat = rgbMatrix(image);//returns Matrix of RGB colors
		int[][] energymatrix = new int[m][n];
		
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				for(int k = Math.max(i-1, 0); k<Math.min(i+1, m);k++){
					for(int l = Math.max(j-1, 0); l<Math.min(j+1, n);l++){
						if(i != k || l!=j){
							valcount++;
							energy_ij += Calculate_distance(rgbmat[i][j], rgbmat[k][l]);	
						}
					}
				}
				energy_ij/=valcount; //TODO: Dor to fix this... - there is a devide by 0 happening here at first iteration
				energymatrix[i][j] = energy_ij;
				energy_ij = 0;
				valcount = 0;
			}
		}
		
		if(energytype>=1){ //TODO: is this o.k for energy type of 2?
			Calculate_Hi(energymatrix,rgbmat, n, m);
		}
		
		return energymatrix;
	}
	
	/*
	 * gets an image and evaluates the RGB matrix
	 * */
	private static int[][] rgbMatrix(BufferedImage image){
		int m = image.getWidth();
		int n = image.getHeight();
		int[][] rgbmat = new int[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				rgbmat[i][j] = image.getRGB(i, j);
			}
		}
		return rgbmat;
	}
	
	/*
	 * removes a general seam as described in the Assignment
	 * */
	public static BufferedImage Remove_seam(BufferedImage originalimage, int[][] energymat,int seamtype){
		
		//create new image
		BufferedImage newimage = new BufferedImage(originalimage.getWidth(), originalimage.getHeight(), originalimage.getType());
		
		return newimage;
	}
	
	/*
	 * removes a straight seam from the image for the 'straight_seam' implementation
	 * */
	public static BufferedImage Remove_straight_seam(BufferedImage originalimage,int[][] energymat, int colToReduce){
		int m = originalimage.getWidth();
		int n = originalimage.getHeight();
		int minIntVal = Integer.MAX_VALUE + 1;
		for(int i = 0; i<colToReduce;i++){
			int min = energymat[n-1][0];
			for(int j =1; j<m;j++){
				int temp = energymat[n-1][j];
				if(temp > minIntVal && temp<min){
					for(int r = 0; r<n; r++){
						energymat[r][j] = minIntVal;
					}
				}
			}
		}
		BufferedImage newImage = new BufferedImage(n, m-colToReduce, originalimage.getType());
		for(int i=0; i<n; i++){
	         
            for(int j=0; j<m; j++){
            	if(energymat[i][j] == minIntVal){
            		continue;
            	}
               newImage.setRGB(j,i,originalimage.getRGB(j,i));
            }
         }
		return newImage;
	}

	/*
	 * this function is called in case we want to add the local entropy to the energy function
	 * @return the energy matrix with local entropy
	 * */
	private static void Calculate_Hi(int[][] matrix,int[][] rgbmat,int n,int m){
		int[][] pmnMat = Calculate_pmn(rgbmat,n, m);
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				double hi = 0;
				for(int k = Math.max(i-4, 0); k<Math.min(i+4, m);k++){
					for(int l = Math.max(j-4, 0); l<Math.min(j+4, n);l++){
						if(i != k || l!=j){
							hi += (pmnMat[k][l] * Math.log(pmnMat[k][l]));	
						}
					}
				}
				matrix[i][j] -= (int)hi;
			}
		}	
	}
	
	/*
	 * gets an RGB matrix and evaluates the pmn for each pixel
	 * */
	private static int[][] Calculate_pmn(int[][] rgbmat,int n,int m){
		int[][] greyscaleMat = grayScale(rgbmat,n, m);
		int[][] pmnMat = new int[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				int temppmn = 0;
				for(int k = Math.max(i-4, 0); k<Math.min(i+4, m);k++){
					for(int l = Math.max(j-4, 0); l<Math.min(j+4, n);l++){
						if(i != k || l!=j){
							temppmn += Calculate_distance(rgbmat[i][j], rgbmat[k][l]);	
						}
					}
				}
				pmnMat[i][j] = greyscaleMat[i][j]/temppmn;
			}
		}
		return pmnMat;
	}
	
	/*
	 * this function calculates the distance between two RGB points (which are int's)
	 * as described in the Assignment
	 * @return int - the distance between the two points
	 * */
	private static int Calculate_distance(int orig,int second){
		Color one = new Color(orig);
		Color two = new Color(second);
		return (Math.abs(one.getRed()-two.getRed())+Math.abs(one.getBlue()-two.getBlue())+Math.abs(one.getGreen()-two.getGreen()))/3;
	}
	
	/*
	 * 
	 * */
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
	
	/*
	 * this function gets an RGB image and evaluates the grey scale matrix
	 * */
	private static int[][] grayScale(int[][] rgbmat,int n, int m) {
		int[][] greyscaleMat = new int[n][m];
		for(int i=0; i<n; i++){
	    	for(int j=0; j<m; j++){
	    		Color c = new Color(rgbmat[i][j]);
	            int red = (int)(c.getRed() * 0.299);
	            int green = (int)(c.getGreen() * 0.587);
	            int blue = (int)(c.getBlue() *0.114);
	            greyscaleMat[n][m] = red+green+blue;
	        }
	    }
		return greyscaleMat; 
	}
}
