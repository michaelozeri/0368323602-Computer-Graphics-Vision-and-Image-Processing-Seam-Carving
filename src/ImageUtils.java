import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.Color;
import java.io.BufferedWriter; //TODO: remove these are for logger
import java.io.FileWriter; //TODO: remove these are for logger


public class ImageUtils {
	
	public static boolean log = false;
	/*
	 * prints the mat into a log file for debug purposes
	 * */
	private static void PrintMat(int[][] mat,String filename){
		if(log){
			try{
				FileWriter f = new FileWriter("C:\\Users\\mozeri\\Downloads\\"+filename+".txt");
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
	public static int[][] Calculate_Energy(BufferedImage image,int energytype){ 
		int m = image.getHeight();
		int n = image.getWidth();
		int energy_ij = 0,valcount=0;;
		int[][] rgbmat = rgbMatrix(image);//returns Matrix of RGB colors
		int[][] energymatrix = new int[m][n];
		
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
	public static BufferedImage remove_General_seam(BufferedImage originalimage,int seamtype){
		
		int rows = originalimage.getHeight();
		int cols = originalimage.getWidth();
		
		int[][] energymat = Calculate_Energy(originalimage, 0);
		
		//calculate pixel attribute
		int[][] atrib = CalcPixelAttribute(energymat);
		PrintMat(atrib, "atributefirst");
		
		//calculate minimal seam path - vector representing indexes
		int[] seam = CalculateGeneralSeam(atrib);
		
		//create new image with one less column (width-1)
		BufferedImage newImage = new BufferedImage(originalimage.getWidth()-1, originalimage.getHeight(), originalimage.getType());
		
		int k=0; //i of new mat
		int l=0; //j of new mat
		
		/*
		for (int z = 0; z < seam.length; z++) {
			System.out.print(z + ":[" + seam[z]+"] ");
		}
		System.out.println("");*/
		
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
	private static int[][] CalcPixelAttribute(int[][] energymatrix){
		
		int m = energymatrix.length; //m=rows
		int n = energymatrix[0].length;
		int tmpleft = Integer.MAX_VALUE;
		int tmpmid = Integer.MAX_VALUE;
		int tmpright = Integer.MAX_VALUE;;
		int[][] atrib = new int[m][n];
		for(int i=1;i<m;i++){
			for (int j = 0; j < n; j++) {
				//calculate energy values
				if(j!=0){tmpleft = energymatrix[i-1][j-1];}
				if(j!=n-1){tmpright = energymatrix[i-1][j+1];}
				tmpmid = energymatrix[i-1][j];
				//store attribute in matrix
				atrib[i][j] = energymatrix[i][j]+ (int)Math.min((int)Math.min(tmpleft, tmpright),tmpmid);
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
	private static int[] CalculateGeneralSeam(int[][] atrib){
		
		int rows = atrib.length; //m=rows
		int cols = atrib[0].length;
		int min = Integer.MAX_VALUE;
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
		int tmpleft = Integer.MAX_VALUE;
		int tmpmid = Integer.MAX_VALUE;
		int tmpright = Integer.MAX_VALUE;
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
               newImage.setRGB(i,j,originalimage.getRGB(i,j));
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
				for(int k = Math.max(i-4, 0); k<Math.min(i+5, m);k++){
					for(int l = Math.max(j-4, 0); l<Math.min(j+5, n);l++){
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
	 * transposes the matrix given
	 * */
	public static int[][] transposeMatrix(int [][] mat){
        int[][] transposed = new int[mat[0].length][mat.length];
        for (int i = 0; i < mat.length; i++)
            for (int j = 0; j < mat[0].length; j++)
            	transposed[j][i] = mat[i][j];
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
