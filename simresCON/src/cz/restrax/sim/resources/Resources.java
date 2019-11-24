package cz.restrax.sim.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import cz.restrax.sim.SimresCON;

public class Resources {
	private static final String RESOURCE_PATH = "/cz/restrax/sim/resources/"; 
	public static final String ICON16x16 = "16x16";
	public static final String ICON32x16 = "32x16"; 
	public static final String ICON32x32 = "32x32"; 
	
	public static URL getResource(String name) {
		URL url = Resources.class.getResource(RESOURCE_PATH+name); 
        return url;
	}
		
	
	public static String getText(String name) {
		String out="";
		String line="";
		try {
			//InputStream is = SimresCON.class.getResourceAsStream(RESOURCE_PATH+name);
			InputStream is = Resources.class.getResourceAsStream(RESOURCE_PATH+name);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {  
				out += line+"\n";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}		
	
}
