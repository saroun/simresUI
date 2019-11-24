package cz.restrax.gui;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;


/**
 * Vytvori barvy pro "Metal Look and Feel". 
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2012/01/20 17:43:19 $</dt></dl>
 */
public class RestraxTheme extends DefaultMetalTheme {
	public static final int THEME_RESTRAX=0;
	public static final int THEME_BLUE=1;
	public static final int THEME_BORDEAUX=2;
	private final ColorUIResource  primary1;  
	private final ColorUIResource  primary2; 
	private final ColorUIResource  primary3;
	private final ColorUIResource  secondary1;
	private final ColorUIResource  secondary2;
	private final ColorUIResource  secondary3;
	
	/*
	 * BLACK & WHITE
	 */
	private final ColorUIResource  black               = new ColorUIResource(  0,   0,   0);
	private final ColorUIResource  white               = new ColorUIResource(255, 255, 255);

	public RestraxTheme(int theme) {
		secondary1 = new ColorUIResource(181, 168, 153);  
		secondary2 = new ColorUIResource(204, 193, 178);  
		secondary3 = new ColorUIResource(229, 219, 204); 
		if (theme == THEME_BORDEAUX) {
			primary1   = new ColorUIResource(175,   0,  61); 
			primary2   = new ColorUIResource(229,  86, 109);  
			primary3   = new ColorUIResource(244, 210, 201); 
		} else if (theme == THEME_BLUE) {
			primary1   = new ColorUIResource(59,  64,  90);  
			primary2   = new ColorUIResource(123,  128, 154);  
			primary3   = new ColorUIResource(206, 208, 217);  
		} else  {
			primary1   = new ColorUIResource(102, 137, 204); 
			primary2   = new ColorUIResource(153, 186, 221);  
			primary3   = new ColorUIResource(191, 209, 229); 
		}
	}
	
	/**
	 * Return index value of required theme. Ensures valid result.<BR>
	 * Default=RestraxTheme.THEME_BORDEAUX.
	 * @param ThemeName (BLUE|BORDEAUX|RESTRAX)
	 * @return
	 */
	public static int getThemeFromName(String ThemeName) {
		int res=RestraxTheme.THEME_BORDEAUX;
		if (ThemeName != null) {
			if (ThemeName.equalsIgnoreCase("BLUE")){
				res=RestraxTheme.THEME_BLUE;
		    } else if (ThemeName.equalsIgnoreCase("RESTRAX")){
		    	res=RestraxTheme.THEME_RESTRAX;
		    } else if (ThemeName.equalsIgnoreCase("BORDEAUX")){
		    	res=RestraxTheme.THEME_BORDEAUX;
		    } 
		}
		return res;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	protected ColorUIResource  getBlack()      {return black;}
	protected ColorUIResource  getWhite()      {return white;}
	protected ColorUIResource  getPrimary1()   {return primary1;}
	protected ColorUIResource  getPrimary2()   {return primary2;}
	protected ColorUIResource  getPrimary3()   {return primary3;}
	protected ColorUIResource  getSecondary1() {return secondary1;}
	protected ColorUIResource  getSecondary2() {return secondary2;}
	protected ColorUIResource  getSecondary3() {return secondary3;}
	public String              getName()       {return "RestraxGUI";}
}