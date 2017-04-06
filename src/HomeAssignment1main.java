
import javax.imageio.ImageIO;

public class HomeAssignment1main {
	public static void main(String[] args){
		
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
		
		System.out.println("is it working?");

		
		System.out.println("at main function of HomeAssignment1");
		
		
	}

}
