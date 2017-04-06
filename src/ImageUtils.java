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

}
