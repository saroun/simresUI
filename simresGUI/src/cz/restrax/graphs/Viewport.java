package cz.restrax.graphs;

public class Viewport {
	private static final int MIMAX=256;	// Max. & default dimension of image arrays
	private static final int MAXGDEV=32;
// defined values of VIEWPORT%TYP
	private static final int grtyp_none=0;
	private static final int grtyp_1D=1;
	private static final int grtyp_2D=2;
	private static final int grtyp_ell=3;
	private static final int grtyp_txt=4;

// Degine space for graph data:
	
	private static final int MAXPIX=256;    // pixel resolution
	
	private static final int MAXPNTS=256;   // number of points for 1 curve
	
	
////////////////////////////////////////////////////	
	
	private static final int VPORT_MAXGRF=255; // max. number of graphs allocated to the viewport
    private double[][] RL2PORT;
    // transformation matrix rec. lattice -> viewport coordinates
    private double[][] PORT2RL;  // inverse to RL2PORT
    public double[] REF;           // reference point of the viewport coordinate system
    private int IX,IY;          //indices to QE(4) vector (for resol. function projections)
    private int[] IGRF;  // indices of the graphs allocated to the viewport
    private int[] TGRF;  // types of the graphs allocated to the viewport
    private int[] NGRF ;          // number of actually allocated graphs
    private double [] DC;             // Device coordinates
    public double [] WC;             // World coordinates
    public double [] LOGSCALE;       // base for log-scale (0=linear)
    private double CHSIZE;            // character size
    private int STYLE;          // frame style: default(0),cross(1)
    private int LWIDTH;         // line width
    private int COLOR;          // color index
    private String XTIT,YTIT,HEAD;
    private String COMMENT;
    private String LEGEND;

    public Viewport() {
    	super();
    	RL2PORT = new double[4][4];
    	PORT2RL = new double[4][4];
    	REF = new double[4];
    	IGRF = new int[VPORT_MAXGRF];
    	TGRF = new int[VPORT_MAXGRF];
    	DC = new double[4];
    	WC = new double[4];
    	LOGSCALE = new double[3];
    	
    	
    }
    
}
