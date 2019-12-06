package cz.restrax.sim;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.FloatData;
import cz.jstools.classes.definitions.Utils;

public class InstrumentControlData  {
    private double ki,kf,q,en,lambda, ttheta, psi;
    private int ss, fix, kfmode,inpmode;
    private static final double HSQOV2M  = 2.072124655;
    
	
    public void consolidate() throws Exception {
// inpmode=0: set ki or kf, EN, Q     	
    	if (inpmode==0) {
    		if (fix==0) {
    			kf=Math.sqrt(Math.pow(ki,2)-en/HSQOV2M);
    		} else {
    			ki=Math.sqrt(Math.pow(kf,2)+en/HSQOV2M);
    		}    		
    		lambda = 2*Math.PI/ki;
    		double cos2t=(Math.pow(ki, 2)+Math.pow(kf, 2)-Math.pow(q, 2))/(2*ki*kf);
    		if (Math.abs(cos2t)<1.0) {
    			double sin2t=ss*Math.sqrt(1-Math.pow(cos2t, 2));
    			ttheta=Math.atan2(sin2t, cos2t);
    		} else {
    			throw new Exception("Scattering triangle does not close.");
    		}
// inpmode=1: set lambda and scattering angle    	
    	} else if (inpmode==1) {
    		ki=2*Math.PI/lambda;
    		kf=Math.sqrt(Math.pow(ki,2)-en/HSQOV2M);
    		double q2=Math.pow(ki, 2)+Math.pow(kf, 2)-2*ki*kf*Math.cos(ttheta);
    		q=Math.sqrt(q2);
    	}
    }
    
	public void readClassData(ClassData cls) throws Exception {
		ki=cls.getDouble("KI");
		kf=cls.getDouble("KF");
		q=cls.getDouble("Q0");
		en=cls.getDouble("EN");
		lambda=cls.getDouble("LAMBDA");
		ttheta=cls.getDouble("THETA");
		psi=cls.getDouble("PSI");
		ss=cls.getInteger("SS");
		fix=cls.getInteger("FIX");
		kfmode=cls.getInteger("KFMODE");
		//inpmode=cls.getInteger("INPMODE");
		
	}

	public void writeClassData(ClassData cls) throws Exception {
		cls.setData("KI",Utils.d2s(ki));
		cls.setData("KF",Utils.d2s(kf));
		cls.setData("Q0",Utils.d2s(q));
		cls.setData("EN",Utils.d2s(en));
		cls.setData("LAMBDA",Utils.d2s(lambda));
		cls.setData("THETA",Utils.d2s(ttheta));
		cls.setData("PSI",Utils.d2s(psi));
		cls.setData("SS",Utils.i2s(ss));
		cls.setData("FIX",Utils.i2s(fix));
		cls.setData("KFMODE",Utils.i2s(kfmode));
		//cls.setData("INPMODE",Utils.i2s(inpmode));		
	}
	
	
}
