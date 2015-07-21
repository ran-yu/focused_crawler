package entityGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CIscore {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public static Map<String, Double> ReadCIScore() throws NumberFormatException, IOException{
		File seedsCI = new File("seeds_CI");
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(seedsCI));
		String CIline = null;
		
		Map<String, Double> CI_map = new HashMap<String, Double>();
		while( (CIline = reader.readLine()) != null){
			String[] tmp = CIline.split("\t");
			CI_map.put(tmp[0], Double.valueOf(tmp[1]));
		}
		reader.close();
		
		return CI_map;
	}

}
