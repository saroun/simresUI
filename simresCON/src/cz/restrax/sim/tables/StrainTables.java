package cz.restrax.sim.tables;


public class StrainTables extends Tables {	
	
	public StrainTables() {
		super();
	}
	
	@Override
	public void defineProperties() {
		TITLE="strain tables";
		FILE="strain_tables.xml";
		COMMAND="STTAB";
		ACTION="updStrainTab";
		TAGLIST="STABLE";
		TAGITEM="ITEM";
		TAGDUMP="STRAINLIST";
		KEYID="id";
	}	
	
	@Override
	public String validateKey(String key) {
		int f = Integer.parseInt(key);
		if (f<0) {
			return "0";			
		} else {
			return String.format("%d",f);
		}
	}
	
}
