package cz.saroun.classes.definitions;


/**
 * Definition of plane by two vectors 'a' and 'b' in cartesian coordinates.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:53 $</dt></dl>
 */
public class Plane {
	public Cartesian a;
	public Cartesian b;


	public Plane() {
		a = new Cartesian();
		b = new Cartesian();
	}

	
	public void assign(Plane from) {
		this.a.assign(from.a);  // CLASS
		this.b.assign(from.b);  // CLASS
	}
	
	public Plane duplicate() {
		Plane duplication;
		
		duplication = new Plane();
		duplication.assign(this);
		
		return duplication;
	}
}