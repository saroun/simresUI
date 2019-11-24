package cz.restrax.sim.tables;


public class MirrorTables extends Tables {	
	
	public MirrorTables() {
		super();
		// define alternative mirror tables
		tablists.add(new TablesList("mirror_tables.xml", "Default tables"));
		tablists.add(new TablesList("mirror_tables_mcstas.xml", "McStas reflectivities with constant slope"));
		tablists.add(new TablesList("mirror_tables_mcstas_def.xml", "McStas default parametrization"));
		tablists.add(new TablesList("mirror_tables_niti.xml", "Fits to SNAG average reflectivities"));
		tablists.add(new TablesList("mirror_tables_niti_c.xml", "For testing only (not validated): SNAG + coherent scattering"));
	}
	
	@Override
	public void defineProperties() {
		TITLE="mirror tables";
		FILE="mirror_tables.xml";
		COMMAND="MRTAB";
		ACTION="updMirrors";
		TAGLIST="MTABLE";
		TAGITEM="MIRROR";
		TAGDUMP="MIRRORLIST";
		KEYID="m";
	}	
	
	/**
	 * Convert m-value to a key string
	 */
	@Override
	public String validateKey(String key) {
		float f = Float.parseFloat(key);
		if (f<0.0 || f >= 10.0) {
			return "0.0";			
		} else {
			return String.format("%.1f",f);
		}
	}
	
}
