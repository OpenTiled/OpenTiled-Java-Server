package IO;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {
	
	public static int saveFrequency = 40;
	public static String inputFile = "./map.tmx";
	public static String inputTileset = "./tileset.png";
	public static String snapshotDirectory = "./snapshots";
	private static int port = 9000;
	
	public static void main(String[] args) throws FileNotFoundException {
		if(args.length > 0) {
			for(int i = 0;  i <  args.length; i+=2) {
				switch(args[i]) {
					case "-p" : port = Integer.parseInt(args[i+1]); break;
					case "-inf" : inputFile = args[i+1]; break;
					case "-tsf" : inputTileset = args[i+1]; break; 
					case "-sdir" : snapshotDirectory = args[i+1]; break;
					case "-sfq" : saveFrequency = Integer.parseInt(args[i+1]); break;
				}
			}
		} else {
			//create gui
		}
		if(!new File(inputFile).exists()) { // should also check if it is a directory
			throw new FileNotFoundException("The input map file was not found. Make sure you have inputed the file directory correctly.");
		}
		if(!new File(snapshotDirectory).exists()) {
			new File(snapshotDirectory).mkdir();
		}
		OpenTileServer tileServer = new OpenTileServer(port);
	}
	

}
