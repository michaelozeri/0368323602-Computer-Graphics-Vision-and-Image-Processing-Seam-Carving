import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.BufferedWriter; //TODO: remove these are for logger
import java.io.FileWriter; //TODO: remove these are for logger


public class ImageUtils {
	
	
	// a boolean deciding if to log or not
	public static boolean m_log = false;
	
	/**
	 * prints the matrix into a log file for debug purposes
	 * @author michael
	 * */
	private static void print_Mat_To_Logfile(double[][] mat,String filename){ 
		if(m_log){
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
	
	/**
	 * this function calculates the energy matrix for the image given as 'image'
	 * @param energytype - means how to calculate the energy (with / without local entropy
	 * @return energymatrix - the energy matrix of the image given
	 * */
	public static double[][] calculate_Energy(BufferedImage image,int energytype){ 
		
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
							energy_ij += calculate_Distance(rgbmat[i][j], rgbmat[k][l]);	
						}
					}
				}
				energy_ij/=valcount; 
				energymatrix[i][j] = energy_ij;
				energy_ij = 0;
				valcount = 0;
			}
		}
		
		if(energytype==1){ 
			calculate_Hi(energymatrix,rgbmat);
		}
		
		return energymatrix;
	}
	
	/**
	 * gets an image and evaluates the RGB matrix
	 * @return an int[][] matrix representing the image
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
	
	/**
	 * removes a general seam as described in the Assignment
	 * this function removes one vertical seam from the image
	 * @return newImage - the image with the seam removed
	 * */
	public static BufferedImage remove_General_seam(BufferedImage originalimage,int energytype){
		
		int rows = originalimage.getHeight();
		int cols = originalimage.getWidth();
		
		double[][] energymat = calculate_Energy(originalimage, energytype); 
		
		//calculate pixel attribute
		double[][] atrib = calculate_Pixel_Attribute(energymat);
		print_Mat_To_Logfile(atrib, "atributefirst");
		
		//calculate minimal seam path - vector representing indexes
		int[] seam = calculate_General_Seam(atrib);
		
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
		return newImage;
	}
	
	/**
	 * calculates the pixel attribute before calculating seam path
	 * @param energymatrix - the matrix on whom to calculate the pixel attribute before calculating the minimal
	 * seam
	 * @return atrib - the matrix energymatrix after the pixel attribute calculation
	 * */
	private static double[][] calculate_Pixel_Attribute(double[][] energymatrix){
		
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
	private static int[] calculate_General_Seam(double[][] atrib){
		
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
		print_Mat_To_Logfile(atrib,"log1"); //TODO: remove
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
			print_Mat_To_Logfile(atrib,"log1"); //TODO: remove
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
	public static BufferedImage remove_Straight_Seam(BufferedImage originalimage, int energytype){
		
		int m = originalimage.getWidth();
		int n = originalimage.getHeight();
		
		//calculate the energy matrix
		double[][] energymat = calculate_Energy(originalimage, energytype);

		//calculate a vector that each index holds the column seam value
		double[] seamVector = calcuate_Straight_Seam(energymat);
		
		//choose the minimum seam to remove from the seam vector
		int minindex = 0;
		double min = seamVector[0];
		for(int j =1; j<m;j++){
			double temp = seamVector[j];
			if(temp<min){
				minindex = j;
				min = seamVector[j];
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
	
	/**
	 * this function calculates the straight seam to be removed by summing
	 * each column to a double value
	 * @return straightsSeam - a double array which each index represents the column energy
	 * */
	public static double[] calcuate_Straight_Seam(double [][] energymatrix){
		//value per each column
		double[] straightsSeam = new double[energymatrix[0].length];
		for (int i = 0; i < energymatrix.length; i++)//go through every row
            for (int j = 0; j < energymatrix[0].length; j++)//add the value of energy above
            	straightsSeam[j] += energymatrix[i][j];
        return straightsSeam;
	}

	/**
	 * this function is called in case we want to add the local entropy to the energy function
	 * @return the energy matrix with local entropy
	 * */
	private static void calculate_Hi(double[][] matrix,int[][] rgbmat){
		int m = matrix.length;
		int n = matrix[0].length;
		double[][] pmnMat = calculate_pmn(rgbmat);
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
	
	/**
	 * gets an RGB matrix and evaluates the p(m,n) value for each pixel
	 * @param rgbmat - the matrix from to evaluate the pmn value
	 * @return pmnMat - the matrix with the P(m,n) values already in it
	 * */
	private static double[][] calculate_pmn(int[][] rgbmat){
		int m = rgbmat.length;
		int n = rgbmat[0].length;
		double[][] greyscaleMat = grayScale(rgbmat);
		double[][] pmnMat = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				double temppmn = 0;
				for(int k = Math.max(i-4, 0); k<Math.min(i+5, m);k++){
					for(int l = Math.max(j-4, 0); l<Math.min(j+5, n);l++){
						if(i != k || l!=j){
							temppmn += greyscaleMat[k][l];	
						}
					}
				}
				pmnMat[i][j] = greyscaleMat[i][j]/temppmn;
			}
		}
		return pmnMat;
	}
	
	/**
	 * this function calculates the distance between two RGB points (which are int's)
	 * as described in the Assignment
	 * @return int - the distance between the two points
	 * */
	private static int calculate_Distance(int orig,int second){
		Color one = new Color(orig);
		Color two = new Color(second);
		return (Math.abs(one.getRed()-two.getRed())+Math.abs(one.getBlue()-two.getBlue())+Math.abs(one.getGreen()-two.getGreen()))/3;
	}
		
	/**
	 * this function calculates the minimal seam for straight seam curving function
	 * @param energy - the energy matrix to calculate from it the minimal seam
	 * */
	public static int[][] calculate_Min_Seam(double [][] energy, int colToadd){
		double[][] attribute = calculate_Pixel_Attribute(energy);
		int m = energy[0].length;
		int n = energy.length;
		int[][] minSeam = new int[n][m];
		double mindubVal = Double.MIN_VALUE;
		for(int i = 0; i<colToadd;i++){
			double min = attribute[n-1][0];
			int minindex = 0;
			while(minindex<m && attribute[n-1][minindex] == mindubVal){
				minindex++;
			}
			if(minindex==m)
				minindex--;
			min = attribute[n-1][minindex];
			for(int j =minindex+1; j<m;j++){
				double temp = attribute[n-1][j];
				if(temp > mindubVal && temp<min){
					minindex = j;
					min = temp;
				}
			}
			attribute[n-1][minindex] =mindubVal;

			minSeam[n-1][minindex]++;
			for(int r = n-1; r>0;r--){
				if(minindex == 0){
					if(attribute[r-1][minindex]> attribute[r-1][minindex+1])
						minindex = minindex+1;
				}
				else if(minindex == m-1){
					if(attribute[r-1][minindex]> attribute[r-1][minindex-1])
						minindex = minindex-1;
				}
				else{
					min = Math.min(attribute[r-1][minindex-1], attribute[r-1][minindex]);
					min = Math.min(attribute[r-1][minindex+1], min);
					if(min == attribute[r-1][minindex+1])
						minindex = minindex+1;
					else if(min == attribute[r-1][minindex-1])
							minindex = minindex-1;
				}
				minSeam[r-1][minindex]++;
			}
		}
        return minSeam;
		
	}
	
	/**
	 * this function gets an RGB image and evaluates the grey scale matrix
	 * @param rgbmat - the matrix representing the image which to calculate the gray scale values from
	 * @return greyscaleMat - a matrix representing the gray scale value of the color matrix image
	 * */
	private static double[][] grayScale(int[][] rgbmat) {
		int m = rgbmat.length;
		int n = rgbmat[0].length;
		double[][] greyscaleMat = new double[m][n];
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
	
	/**
	 * this function adds a single seam to the picture and Blends the added seam by interpolation with its neighbors
	 * @param originalimage - the original image to add the seam to
	 * @param energytype - the energy type of the picture
	 * @param colToadd - number of cols to add
	 * @return newImage - the new image with the added seam
	 */
	public static BufferedImage add_single_seam_with_interpolation(BufferedImage originalimage,int energytype, int colToadd){
		int m = originalimage.getWidth();
		int n = originalimage.getHeight();
		
		double[][] energymat = calculate_Energy(originalimage, energytype); 
		
		//calculate min sim attribute
		int[][] seam = calculate_Min_Seam(energymat, colToadd);

		BufferedImage newImage = new BufferedImage(m+colToadd, n, originalimage.getType());
		
		for(int i=0; i<n; i++){
	        int c =0;
            for(int j=0; j<m; j++){
				newImage.setRGB(c,i,originalimage.getRGB(j,i));
            	if(seam[i][j] > 0){
            		int rgbval = originalimage.getRGB(j,i);
            		
	            	for(int k = 0; k<seam[i][j]; k++){
						c++;
						if(j != m-1){		
							rgbval = calculate_avrage_color(rgbval, originalimage.getRGB(j+1,i));
						}
	            		newImage.setRGB(c,i,rgbval); //TODO: check this is working
            		}
            	}
                c++;
            }
         }
		return newImage;
	}
	
	/**
	 * 
	 * @author dor
	 * @param image - the image to calculate on it the forward energy
	 * @param energytype - the energy type asked in the exercise
	 * @return minSeam - the minimal seam to remove from the picture calculated by the forwarding method
	 */
	public static double[][] calcuateMinForwardSeam(BufferedImage image,int energytype){
		double[][] energy = calculate_Energy(image, energytype); 
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
            		minSeam[i][j] += Math.min(energy[i-1][j]+cu,Math.min(energy[i-1][j-1]+cl,energy[i-1][j+1]+cr));
            }
        return minSeam;
		
	}

	public static BufferedImage add_single_seam(BufferedImage originalimage,int energytype, int colToadd){
		int m = originalimage.getWidth();
		int n = originalimage.getHeight();
		double[][] energymat = calculate_Energy(originalimage, energytype); 
		
		//calculate min sim attribute
		int[][] seam = calculate_Min_Seam(energymat, colToadd);
	
		BufferedImage newImage = new BufferedImage(m+colToadd, n, originalimage.getType());
		for(int i=0; i<n; i++){
	        int c =0;
	        for(int j=0; j<m; j++){
				newImage.setRGB(c,i,originalimage.getRGB(j,i));
				if(seam[i][j] > 0){
					int rgbval = originalimage.getRGB(j,i);
	            	for(int k = 0; k<seam[i][j]; k++){
						c++;
	            		newImage.setRGB(c,i,rgbval); //TODO: check this is working
	        		}
	        	}
	            c++;
	        }
	     }
		return newImage;
	}

	/**
	 * transpose's the image
	 * @param img - the image to transpose
	 * @return retimg - the image transposed
	 * */
	public static BufferedImage transpose_Image(BufferedImage img){
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
	public static BufferedImage remove_General_seam_Energy_Once(BufferedImage originalimage,int energytype,int resizeNum){
		
		
		double[][] energymat = calculate_Energy(originalimage, energytype); 
		
		//calculate pixel attribute
		double[][] atrib = calculate_Pixel_Attribute(energymat);
		print_Mat_To_Logfile(atrib, "atributefirst");
		
		for(;resizeNum>0;resizeNum--){
			
			//calculate minimal seam path - vector representing indexes
			atrib = calculate_General_Seam_static(atrib);
			
			//create new image with one less column (width-1)
			originalimage = calculate_new_image(originalimage,atrib);
			
			atrib = calculate_new_atribute(atrib);
			
		}
		
		//return new image
		return originalimage;
	}
	
	/**
	 * calculate's the avrage color between two colors given
	 * @param clr1 - an int representing a RGB color by its bit's
	 * @param clr2 - an int representing a RGB color by its bit's
	 * @return the avrage color represented by an int
	 * @author - dor
	 */
	private static int calculate_avrage_color(int clr1, int clr2){
		Color cx = new Color(clr1);
		Color cy = new Color(clr2);
        int red = (int)((cx.getRed()+cy.getRed())/2);
        int green = (int)((cx.getGreen() + cx.getGreen())/2);
        int blue = (int)((cy.getBlue() + cy.getBlue())/2);
        Color c = new Color(red, green, blue);
        return c.getRGB();
	}
	
	/*
	 * this function calculates the minimal seam to remove and returns it as a vector
	 * */
	private static double[][] calculate_General_Seam_static(double[][] atrib){
		
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
		atrib[rows-1][minIndex] = Integer.MAX_VALUE; 
		print_Mat_To_Logfile(atrib,"log1"); //TODO: remove
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
			print_Mat_To_Logfile(atrib,"log1"); //TODO: remove
			//reset tmp values
			tmpleft = Integer.MAX_VALUE;
			tmpmid = Integer.MAX_VALUE;
			tmpright = Integer.MAX_VALUE;
		}
		return atrib;
	}
	
	private static double[][] calculate_new_atribute(double[][] atrib){
		
		double[][] atribnew = new double[atrib.length][atrib[0].length-1];
		
		int k=0; //i of new atrib
		int l=0; //j of new atrib
		
		//copy only wanted pixels to new picture
		for (int i = 0; i < atrib.length; i++) {
			for (int j = 0; j < atrib[0].length; j++) {
				if(atrib[i][j] == Integer.MAX_VALUE){
					continue;
				}
				atribnew[k][l] = atrib[i][j];
				l++;
			}
			l=0;
			k++;
		}
		return atribnew;
	}
	
	private static BufferedImage calculate_new_image(BufferedImage img,double[][] atrib){
		
		int rows = img.getHeight();
		int cols = img.getWidth();
			
		//create new image with one less column (width-1)
		BufferedImage newImage = new BufferedImage(img.getWidth()-1, img.getHeight(), img.getType());
		
		int k=0; //i of new mat
		int l=0; //j of new mat
		
		//copy only wanted pixels to new picture
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if(atrib[i][j] == Integer.MAX_VALUE){
					continue;
				}
				newImage.setRGB(l, k, img.getRGB(j, i));
				l++;
			}
			l=0;
			k++;
		}
		return newImage;
	}
}




