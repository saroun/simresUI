package cz.saroun.classes.definitions;


/**
 *  Definition of point in spheric coordinates.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:53 $</dt></dl>
 */
public class Spheric {
	public double alpha;
	public double beta;
	public double dist;

	
	public Spheric() {
		alpha = 0.0;
		beta  = 0.0;
		dist  = 0.0;
	}
	

	public void assign(Spheric from) {
		this.alpha = from.alpha;  // primitive
		this.beta  = from.beta;   // primitive
		this.dist  = from.dist;   // primitive
	}
	
	public Spheric duplicate() {
		Spheric duplication;
		
		duplication = new Spheric();
		duplication.assign(this);
		
		return duplication;
	}
}
