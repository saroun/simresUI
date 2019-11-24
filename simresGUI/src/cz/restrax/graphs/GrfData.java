package cz.restrax.graphs;

import java.awt.Graphics2D;

import cz.restrax.graphs.primitives.GrfLabel;
import cz.restrax.graphs.primitives.Plot1d;
import cz.restrax.graphs.primitives.Plot2d;
import cz.restrax.graphs.primitives.PlotEll;

public class GrfData {
	private static final int MAXPORTS=16;   // number of viewports
	private static final int MAX2D=4;       // number of 2D plots
	private static final int MAX1D=32;      // number of curves
	private static final int MAXELL=256;    // number of ellipsoids
	private static final int MAXTXT=256 ;   // number of text rows
		
	// viewports for standard graphs in RESTRAX:
	Viewport[] GRFPORT;
	
	// define variables for graph data:
		  // 1D plot
	int NGRF1D;
	Plot1d[] GRF1D;
		  // 2D maps
	int NGRF2D;
	Plot2d[] GRF2D;
		  // ELLIPSES
	int NGRFELL;
	PlotEll[] GRFELL;
		  // text
	int NGRFTXT;
	GrfLabel[]  GRFTXT;
	
	public GrfData() {
		super();
		GRFPORT = new Viewport[MAXPORTS];
		GRF1D=new Plot1d[MAX1D];
		GRF2D=new Plot2d[MAX2D];
		GRFELL=new PlotEll[MAXELL];
		GRFTXT=new GrfLabel[MAXTXT];
			
	}
	
	public void clearAllGraphs() {
		GRFPORT = new Viewport[MAXPORTS];
		GRF1D=new Plot1d[MAX1D];
		GRF2D=new Plot2d[MAX2D];
		GRFELL=new PlotEll[MAXELL];
		GRFTXT=new GrfLabel[MAXTXT];
		NGRF1D=0;
	    NGRF2D=0;
	    NGRFELL=0;
	    NGRFTXT=0;
	}

	
}
