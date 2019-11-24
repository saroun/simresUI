package cz.restrax.sim;

public class Version {	
	public  static final String    VERSION           = "6.3.6";
	public  static final String    BUILD             = "$Date: 2019/06/12 17:58:11 $";
	// RESTRAX versions  compatible with this  GUI 
	public static final String[] RESVERSIONS =  {"6"};

	private String restraxVersion = "undetected";
	private String restraxBuild = "undetected";
	SimresCON program;
	
	public Version (SimresCON program) {
		this.program=program;
	}
	
	public String getRestraxVersion() {
		return restraxVersion;
	}

	public void setRestraxVersion(String restraxVersion) {
		this.restraxVersion = restraxVersion;
		boolean isCompatible = checkVersion(restraxVersion);
		if (! isCompatible) {
			String hdr = getVersionString();
			String msg = getVersionErrorString(SimresCON.PROGRAM_NAME);
			System.err.println(hdr+"\n"+msg);
		}	
	}
	
	public static boolean checkVersion(String version) {
		boolean isCompatible = false;
		for (int i=0;i<RESVERSIONS.length;i++) {
			isCompatible=(isCompatible || version.matches("^"+RESVERSIONS[i]+".*"));
		}
		return isCompatible;
	}
	
	public static String getVersionErrorString(String sourceName) {
		String msg = "This GUI and "+sourceName+" versions are not compatible.\n"+
        "Supported "+SimresCON.PROGRAM_NAME+" versions are: \n";
		for (int i=0;i<RESVERSIONS.length;i++) {
			msg += "<html><B>"+RESVERSIONS[i]+"</B></html>\n";
		}
		return msg;
	}
	
	public String getVersionString() {
		return SimresCON.PROGRAM_NAME+" "+restraxVersion;
	}
	
	public String getRestraxBuild() {
		return restraxBuild;
	}

	public void setRestraxBuild(String restraxBuild) {
		this.restraxBuild = restraxBuild;
	}

}
