package cz.restrax.graphs.primitives;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import cz.restrax.graphs.Viewport;

public class Plot1d {
	private static final int MAXPNTS=256;   // number of points for 1 curve
	
	private double[] X;
	private double[] Y;
	private double[] DY;
	private int COLOR,POINT,LINE;
	private double PSIZE;
	private int NP;
	private int ER;
	private String X_CAP,Y_CAP;
		   
	Graphics2D graph=null;
	Viewport port = null;
    public Plot1d(Graphics2D graph, Viewport port) {
    	super();
    	this.graph=graph;
    	this.port=port;
    	X = new double[MAXPNTS];
    	Y = new double[MAXPNTS];
    	DY = new double[MAXPNTS];
    }
    
    public void plot() {
    	graph.setColor(Utils.COLORS[COLOR]);
    	if (LINE>1) graph.setStroke(Utils.dashed);
    	GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, NP);    	
    	if (port.LOGSCALE[1]<=0.0) {
    		polyline.moveTo (X[0]-port.REF[0], Y[0]-port.REF[1]);
    		for (int i = 1; i < NP; i++) polyline.lineTo(X[i]-port.REF[0], Y[i]-port.REF[1]);
    		graph.draw(polyline);
    		if (ER>0) {
    			for (int i = 1; i < NP; i++) {
    				graph.draw(new Line2D.Double(X[i]-port.REF[0], Y[i]-port.REF[1]-DY[i],X[i]-port.REF[0], Y[i]+DY[i]-port.REF[1]));
    			}
    		}
    	} else {
    		double[] x=new double[NP];
    		double[] y=new double[NP];
    		for (int i = 0; i < NP; i++) {
    			x[i]=X[i]-port.REF[0];
    			y[i]=Math.log10(Math.max(Y[i]-port.REF[1],port.WC[2])/10.0);
    		}
    		polyline.moveTo (x[0], y[0]);
    		for (int i = 1; i < NP; i++) polyline.lineTo(x[i], y[i]);
    		graph.draw(polyline);    	
    		if (ER>0) {
    			double[] y1=new double[NP];
    			double[] y2=new double[NP];
    			for (int i = 0; i < NP; i++) {
        			y1[i]=Math.log10(Math.max(Y[i]-DY[i]-port.REF[1],port.WC[2])/10.0);
        			y2[i]=Math.log10(Math.max(Y[i]+DY[i]-port.REF[1],port.WC[2])/10.0);
        		}
    			for (int i = 0; i < NP; i++) {
    				graph.draw(new Line2D.Double(x[i], y1[i],x[i],y2[i]));
    			}
    		}
    	}    	
    }
   
}
