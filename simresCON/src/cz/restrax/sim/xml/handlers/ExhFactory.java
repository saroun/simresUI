package cz.restrax.sim.xml.handlers;

import cz.jstools.classes.xml.ErrorExh;
import cz.jstools.classes.xml.FValueExh;
import cz.jstools.classes.xml.InfoExh;
import cz.jstools.classes.xml.MatrixExh;
import cz.jstools.classes.xml.ValueExh;
import cz.jstools.classes.xml.WarningExh;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.tables.TablesDumpExh;
import cz.restrax.sim.xml.reader.InstrumentExh;

/**
 * Generates call-back xml handlers for various messages received from 
 * Restrax kernel (main parser is RsxDumpExh)
 * @see cz.restrax.sim.xml.handlers.RsxdumpExh
 */
public class ExhFactory {
	public static CallBackInterface createExh(SimresCON program,XmlUtils xml,String name) {
		CallBackInterface exh=null;
		if (name.equals(MatrixExh.ENAME)) {
			exh=new MatrixExh(xml, program.getResultsLog());
		}
		else if (name.equals(ValueExh.ENAME)) {
			exh=new ValueExh(xml, program.getResultsLog());
		}
		else if (name.equals(FValueExh.ENAME)) {
			exh=new FValueExh(xml, program.getResultsLog());
		}
		else if (name.equals(ErrorExh.ENAME)) {
			exh=new ErrorExh(xml, program.getMessages());
		}
		else if (name.equals(WarningExh.ENAME)) {
			exh=new WarningExh(xml, program.getMessages());
		}
		else if (name.equals(InfoExh.ENAME)) {
			exh=new InfoExh(xml, program.getMessages());
		}
		else if (name.equals("SIMRES")) {
			exh=new InstrumentExh(xml,program);
		}
		else if (name.equals(GrflistExh.ENAME)) {
			exh=new GrflistExh(xml, program);
		}
		else if (name.equals(VersioninfoExh.ENAME)) {
			exh=new VersioninfoExh(xml, program);
		}
		else if (name.equals(FMeritExh.ENAME)) {
			exh=new FMeritExh(xml, program);
		}
		else if (name.equals(NessExh.ENAME)) {
			exh=new NessExh(xml, program);
		}
		else if (name.equals(McRunExh.ENAME)) {
			exh=new McRunExh(xml, program);
		}
		else if (name.equals(ProgressExh.ENAME)) {
			exh=new ProgressExh(xml, program);
		}
		// dump of lookup tables lists
		else if (name.equals(program.getMirorTables().getTAGDUMP())) {
			exh=new TablesDumpExh(xml, program.getMirorTables(),program.getExecutor());
		}
		else if (name.equals(program.getStrainTables().getTAGDUMP())) {
			exh=new TablesDumpExh(xml, program.getStrainTables(),program.getExecutor());
		}
	// ignore NSTORES block
		else if (name.equals("NSTORES")) {
			exh=new DefaultExh(xml, name);
		}
		else if (name.equals("BATCHMODE")) {
			exh=new DefaultExh(xml, name);
		}
		return exh;
	}
}
