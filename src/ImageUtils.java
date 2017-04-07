import java.awt.image.BufferedImage;
import java.awt.Color;


public class ImageUtils {
	
	public static int[][] Calculate_Energy(BufferedImage image,int energytype){
		
		int m = image.getWidth();
		int n = image.getHeight();
		int currentcolor,tempcolor,energy_ij = 0,val_ij,valcount=0;;
		
		int[][] energymatrix = new int[m][n];
		
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				currentcolor = image.getRGB(i, j);
				//TODO: calculate distance from all around pixels using the "Calculate_distance private function
				energy_ij/=valcount;
				energymatrix[i][j] = energy_ij;
				energy_ij = 0;
				val_ij = 0;
				valcount = 0;
			}
		}
		
		if(energytype>=1){ //TODO: is this o.k for energy type of 2?
			Calculate_Hi(energymatrix,image);
		}
		
		return energymatrix;
	}
	
	public static BufferedImage Remove_seam(BufferedImage originalimage, int[][] energymat,int seamtype){
		
		//create new image
		BufferedImage newimage = new BufferedImage(originalimage.getWidth(), originalimage.getHeight(), originalimage.getType());
		
		return newimage;
	}
	
	private static void Calculate_Hi(int[][] matrix,BufferedImage image){
		//TODO: complete
	}
	
	private static int Calculate_distance(int orig,int second){
		Color one = new Color(orig);
		Color two = new Color(second);
		return (Math.abs(one.getRed()-two.getRed())+Math.abs(one.getBlue()-two.getBlue())+Math.abs(one.getGreen()-two.getGreen()))/3;
	}
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

}
