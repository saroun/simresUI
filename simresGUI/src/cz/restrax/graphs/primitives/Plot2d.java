package cz.restrax.graphs.primitives;

public class Plot2d {
	private static final int MAXPIX=256;    // pixel resolution
	private double[][] Z;
	private int NX,NY;
	private double[] SCALE;
    public Plot2d() {
    	super();
    	Z = new double[MAXPIX][MAXPIX];
    	SCALE = new double[4];
    }

}
