import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.BufferedWriter; //TODO: remove these are for logger
import java.io.FileWriter; //TODO: remove these are for logger


public class ImageUtils {
	
	
	// a boolean deciding wheather to log or not
	public static boolean log = false;
	
	/*
	 * prints the mat into a log file for debug purposes
	 * */
	private static void PrintMat(double[][] mat,String filename){ //TODO: fix logger
		if(log){
			try{
				FileWriter f = new FileWriter(filename+".txt");
				BufferedWriter bf = new BufferedWriter(f);
				bf.write("******* the matrix *********\n");
				for (int i = 0; i < mat.length; i++) {
					for (int j = 0; j < mat[0].length; j++) {
					    if(mat[i][j] == Integer.MAX_VALUE){
					    	bf.write("XXX ");
					    }
						else if(mat[i][j] >99){
							bf.write(mat[i][j] + " ");
						}else if((mat[i][j] <100)&&(mat[i][j]>9)){
							bf.write(mat[i][j] + "  ");
						}else{
							bf.write(mat[i][j] + "   ");
						}
					}
					bf.write("\n");
				}
				bf.write("****** end of log *******\n");
				bf.close();
			}catch (Exception e){
				System.out.println("ERROR: "+e.getMessage());
			}
		}
	}
	/*
	 * this function calculates the energy matrix for the image given as 'image'
	 * @param energytype - means how to calculate the energy (with / without local entropy
	 * @return 'energymatrix' - the energy matrix of the image given
	 * */
	public static double[][] Calculate_Energy(BufferedImage image,int energytype){ 
		
		int m = image.getHeight();
		int n = image.getWidth();
		double energy_ij = 0,valcount=0;;
		int[][] rgbmat = rgbMatrix(image); //returns Matrix of RGB colors
		double[][] energymatrix = new double[m][n];
		
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				for(int k = Math.max(i-1, 0); k<Math.min(i+2, m);k++){
					for(int l = Math.max(j-1, 0); l<Math.min(j+2, n);l++){
						if(i != k || l!=j){
							valcount++;
							energy_ij += Calculate_distance(rgbmat[i][j], rgbmat[k][l]);	
						}
					}
				}
				energy_ij/=valcount; 
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
		int m = image.getHeight();
		int n = image.getWidth();
		int[][] rgbmat = new int[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				rgbmat[i][j] = image.getRGB(j, i);
			}
		}
		return rgbmat;
	}
	
	/*
	 * removes a general seam as described in the Assignment
	 * */
	public static BufferedImage remove_General_seam(BufferedImage originalimage,int energytype){
		
		int rows = originalimage.getHeight();
		int cols = originalimage.getWidth();
		
		double[][] energymat = Calculate_Energy(originalimage, energytype); 
		
		//calculate pixel attribute
		double[][] atrib = CalcPixelAttribute(energymat);
		PrintMat(atrib, "atributefirst");
		
		//calculate minimal seam path - vector representing indexes
		int[] seam = CalculateGeneralSeam(atrib);
		
		//create new image with one less column (width-1)
		BufferedImage newImage = new BufferedImage(originalimage.getWidth()-1, originalimage.getHeight(), originalimage.getType());
		
		int k=0; //i of new mat
		int l=0; //j of new mat
		
		//copy only wanted pixels to new picture
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if(j == seam[rows-1-i]){
					continue;
				}
				newImage.setRGB(l, k, originalimage.getRGB(j, i));
				l++;
			}
			l=0;
			k++;
		}
		
		//return new image
		return newImage;
	}
	
	/*
	 * calculates the pixel attribute before calculating seam path
	 * */
	private static double[][] CalcPixelAttribute(double[][] energymatrix){
		
		int m = energymatrix.length; //m=rows
		int n = energymatrix[0].length;
		double tmpleft = Integer.MAX_VALUE;
		double tmpmid = Integer.MAX_VALUE;
		double tmpright = Integer.MAX_VALUE;;
		double[][] atrib = new double[m][n];
		for(int i=1;i<m;i++){
			for (int j = 0; j < n; j++) {
				//calculate energy values
				if(j!=0){tmpleft = energymatrix[i-1][j-1];}
				if(j!=n-1){tmpright = energymatrix[i-1][j+1];}
				tmpmid = energymatrix[i-1][j];
				//store attribute in matrix
				atrib[i][j] = energymatrix[i][j]+ Math.min(Math.min(tmpleft, tmpright),tmpmid);
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
	private static int[] CalculateGeneralSeam(double[][] atrib){
		
		int rows = atrib.length; //m=rows
		int cols = atrib[0].length;
		double min = Integer.MAX_VALUE;
		int minIndex=0;
		int[] seam = new int[rows];
		//find first min val
		for (int j = 0; j < cols; j++) {
			if(atrib[rows-1][j]<min){
				min = atrib[rows-1][j];
				minIndex = j;
			}
		}
		atrib[rows-1][minIndex] = Integer.MAX_VALUE; //TODO: remove this
		PrintMat(atrib,"log1"); //TODO: remove
		seam[0] = minIndex;
		double tmpleft = Integer.MAX_VALUE;
		double tmpmid = Integer.MAX_VALUE;
		double tmpright = Integer.MAX_VALUE;
		for (int i = 1; i < seam.length; i++){
			if(seam[i-1]!=0){
				tmpleft = atrib[rows-1-i][seam[i-1]-1];
			}
			if(seam[i-1]!=cols-1){
				tmpright = atrib[rows-1-i][seam[i-1]+1];
			}
			tmpmid = atrib[rows-1-i][seam[i-1]];
			min = Math.min(tmpleft, Math.min(tmpright, tmpmid));
			if(min == tmpmid){
				seam[i] = seam[i-1];
				atrib[rows-1-i][seam[i-1]] = Integer.MAX_VALUE; //TODO: remove these three
			}
			else if(min == tmpleft){
				seam[i] = seam[i-1]-1;
				atrib[rows-1-i][seam[i-1]-1] = Integer.MAX_VALUE; //TODO: remove these three
			}
			else{
				seam[i] = seam[i-1]+1;
				atrib[rows-1-i][seam[i-1]+1] = Integer.MAX_VALUE; //TODO: remove these three
			}
			PrintMat(atrib,"log1"); //TODO: remove
			//reset tmp values
			tmpleft = Integer.MAX_VALUE;
			tmpmid = Integer.MAX_VALUE;
			tmpright = Integer.MAX_VALUE;
		}
		
		return seam;
	}
	
	/*
	 * removes a straight seam from the image for the 'straight_seam' implementation
	 * */
	public static BufferedImage Remove_straight_seam(BufferedImage originalimage, int energytype){
		int m = originalimage.getWidth();
		int n = originalimage.getHeight();
		
		double[][] energymat = Calculate_Energy(originalimage, energytype); //TODO: in case of transpose calc energy of horizontal mat

		double[] seam = calcuateStraightSeam(energymat);
		
		
		int minindex = 0;
		double min = seam[0];
		for(int j =1; j<m;j++){
			double temp = energymat[n-1][j];
			if(temp<min){
				minindex = j;
			}
		}
		BufferedImage newImage = new BufferedImage(originalimage.getWidth()-1, originalimage.getHeight(), originalimage.getType());
		
		for(int i=0; i<n; i++){
			int c = 0;
            for(int j=0; j<m; j++){
            	if(j == minindex){
            		continue;
            	}
            	
                newImage.setRGB(c,i,originalimage.getRGB(j,i));
                c++;
            }
         }
		return newImage;
	}
	public static double[] calcuateStraightSeam(double [][] energy){
		double[] straightsSeam = new double[energy[0].length];
		for (int i = 0; i < energy.length; i++)//go through every row
            for (int j = 0; j < energy[0].length; j++)//add the value of energy above
            	straightsSeam[j] += energy[i][j];
        return straightsSeam;
		
	}

	/*
	 * this function is called in case we want to add the local entropy to the energy function
	 * @return the energy matrix with local entropy
	 * */
	private static void Calculate_Hi(double[][] matrix,int[][] rgbmat,int n,int m){
		double[][] pmnMat = Calculate_pmn(rgbmat,n, m);
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				double hi = 0;
				for(int k = Math.max(i-4, 0); k<Math.min(i+5, m);k++){
					for(int l = Math.max(j-4, 0); l<Math.min(j+5, n);l++){
						if(i != k || l!=j){
							hi += (pmnMat[k][l] * Math.log(pmnMat[k][l]));	
						}
					}
				}
				matrix[i][j] -= hi;
			}
		}	
	}
	
	/*
	 * gets an RGB matrix and evaluates the pmn for each pixel
	 * */
	private static double[][] Calculate_pmn(int[][] rgbmat,int n,int m){
		int[][] greyscaleMat = grayScale(rgbmat,n, m);
		double[][] pmnMat = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				int temppmn = 0;
				for(int k = Math.max(i-4, 0); k<Math.min(i+5, m);k++){
					for(int l = Math.max(j-4, 0); l<Math.min(j+5, n);l++){
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
	 * this function calculates min seam for straight seam curving function
	 * */
	public static int[][] calcuateMinSeam(int [][] energy){
		int[][] minSeam = new int[energy[0].length][energy.length];
		for (int i = 1; i < energy.length; i++)//go through every row
            for (int j = 0; j < energy[0].length; j++){//add the value of energy above
            	if(j==0){
            		minSeam[i][j] += Math.min(energy[i-1][j],energy[i-1][j+1]);
            	}
            	else if(j==energy[0].length-1){
            		minSeam[i][j] += Math.min(energy[i-1][j],energy[i-1][j-1]);
            	}
            	else
            		minSeam[i][j] += Math.min(energy[i-1][j],Math.min(energy[i-1][j-1],energy[i-1][j+1]));
            }
        return minSeam;
		
	}
	

	
	/*
	 * this function gets an RGB image and evaluates the grey scale matrix
	 * */
	private static int[][] grayScale(int[][] rgbmat,int n, int m) {
		int[][] greyscaleMat = new int[m][n];
		for(int i=0; i<m; i++){
	    	for(int j=0; j<n; j++){
	    		Color c = new Color(rgbmat[i][j]);
	            int red = (int)(c.getRed() * 0.299);
	            int green = (int)(c.getGreen() * 0.587);
	            int blue = (int)(c.getBlue() * 0.114);
	            greyscaleMat[i][j] = red+green+blue;
	        }
	    }
		return greyscaleMat; 
	}
	
	public static BufferedImage add_single_seam_with_cuver(BufferedImage originalimage,double[][] energymat, int colToadd){
		int m = originalimage.getWidth();
		int n = originalimage.getHeight();
		double mindubVal = Double.MAX_VALUE+1;
		for(int i = 0; i<colToadd;i++){
			double min = energymat[n-1][0];
			for(int j =1; j<m;j++){
				double temp = energymat[n-1][j];
				if(temp > mindubVal && temp<min){
					for(int r = 0; r<n; r++){
						energymat[r][j] = mindubVal;
					}
				}
			}
		}
		
		BufferedImage newImage = new BufferedImage(n, m+colToadd, originalimage.getType());
		for(int i=0; i<n; i++){
	        int c =0;
            for(int j=0; j<m; j++){
				newImage.setRGB(c,i,originalimage.getRGB(j,i));
            	if(energymat[i][j] == mindubVal){
					c++;
					double rgbval = originalimage.getRGB(j,i);
					if(j != n-1){
						rgbval = 0.5*rgbval + 0.5*originalimage.getRGB(j+1,1);
					}
            		newImage.setRGB(c,i,(int)rgbval); //TODO: check this is working
					
            	}
                c++;
            }
         }
		return newImage;
	}
	

	public static double[][] calcuateMinForwardSeam(BufferedImage image,double [][] energy){
	    int[][] rgbmat = rgbMatrix(image);
		double[][] minSeam = new double[energy[0].length][energy.length];
		for (int i = 1; i < energy.length; i++)//go through every row
            for (int j = 0; j < energy[0].length; j++){//add the value of energy above
			    double cl = 0;
				double cr = 0;
				double cu = 0;
				if((i!= 0) && (j!= 0 || j!=energy[0].length-1)){
					cl = Math.abs(rgbmat[i][j-1]- rgbmat[i][j+1]) + Math.abs(rgbmat[i-1][j] - rgbmat[i][j-1]);
					cr = Math.abs(rgbmat[i][j-1]- rgbmat[i][j+1]) + Math.abs(rgbmat[i-1][j] - rgbmat[i][j+1]);
				}
				if((j!= 0 || j!=energy[0].length-1)){
					cu = Math.abs(rgbmat[i][j-1]- rgbmat[i][j+1]);
				}
            	if(j==0){
            		minSeam[i][j] += Math.min(energy[i-1][j]+cu,energy[i-1][j+1]+cr);
            	}
            	else if(j==energy[0].length-1){
            		minSeam[i][j] += Math.min(energy[i-1][j]+cu,energy[i-1][j-1]+cl);
            	}
            	else
            		minSeam[i][j] += Math.min(energy[i-1][j]+cu,Math.min(energy[i-1][j-1]+cl,energy[i-1][j+1])+cr);
            }
        return minSeam;
		
	}

public static BufferedImage add_single_seam(BufferedImage originalimage,double[][] energymat, int colToadd){
	int m = originalimage.getWidth();
	int n = originalimage.getHeight();
	double mindubVal = Double.MAX_VALUE+1;
	for(int i = 0; i<colToadd;i++){
		double min = energymat[n-1][0];
		for(int j =1; j<m;j++){
			double temp = energymat[n-1][j];
			if(temp > mindubVal && temp<min){
				for(int r = 0; r<n; r++){
					energymat[r][j] = mindubVal;
				}
			}
		}
	}
	
	BufferedImage newImage = new BufferedImage(n, m+colToadd, originalimage.getType());
	for(int i=0; i<n; i++){
        int c=0;
        for(int j=0; j<m; j++){
			newImage.setRGB(c,i,originalimage.getRGB(j,i));
        	if(energymat[i][j] == mindubVal){
				c++;
        		newImage.setRGB(c,i,originalimage.getRGB(j,i));
				
        	}
            c++;
        }
     }
	return newImage;
}

/*
 * transpose's the image
 * */
	public static BufferedImage TransposeImage(BufferedImage img){
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
	
	
}


