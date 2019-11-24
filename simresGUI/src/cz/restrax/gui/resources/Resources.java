package cz.restrax.gui.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;

import cz.restrax.gui.SimresGUI;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.definitions.Utils;

public class Resources {
	private static final String RESOURCE_PATH = "/cz/restrax/gui/resources/"; 
	public static final String ICON16x16 = "16x16";
	public static final String ICON32x16 = "32x16"; 
	public static final String ICON32x32 = "32x32"; 
	
	public static URL getResource(String name) {
		URL url = Resources.class.getResource(RESOURCE_PATH+name); 
        return url;
	}
	
	public static ImageIcon getIcon(String size,String name) {
		ImageIcon ico = new ImageIcon(getResource("images/"+size+"/"+name)); 
		return ico;
	}
	
	public static ImageIcon getImage(String name) {
		ImageIcon ico = new ImageIcon(getResource("images/"+name)); 
		return ico;
	}
	
	public static String getText(String name) {
		String out="";
		String line="";
		try {
			// InputStream is = getClass().getResourceAsStream(s);
			InputStream is = SimresGUI.class.getResourceAsStream(RESOURCE_PATH+name);
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
	
	public static String getHelpText(String name) {
		URL url=SimresGUI.class.getResource("/cz/restrax/gui/help/"+name); 
		String s="error";
		File f;
		try {
			f = new File(url.toURI());
			s= Utils.readFileToString(f);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static ImageIcon getClassDataIcon(String size,ClassData cls) {
		ImageIcon ico=null;
		String name;		
		if (cls.getClassDef().isCommand()) {
			name="COMMAND.png";
		} else {
			name=cls.getClassDef().cid+".png";
		}
		String iconResource="images/"+size+"/"+name;
		URL url=getResource(iconResource);
		if (url == null) {
			iconResource="images/"+size+"/DEFAULT.png";
			url=getResource(iconResource);
		} 
		if (url!=null) ico = new ImageIcon(url); 
		return ico;
	}
	
}
