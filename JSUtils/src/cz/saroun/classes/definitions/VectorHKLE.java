package cz.saroun.classes.definitions;


/**
 * Definition of vector with components H, K, L and Energy
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:35 $</dt></dl>
 */
public class VectorHKLE {
	public double h;
	public double k;
	public double l;
	public double energy;

	public VectorHKLE() {
		h      = 0.0;  // primitive
		k      = 0.0;  // primitive
		l      = 0.0;  // primitive
		energy = 0.0;  // primitive
	}
	
	public void assign(VectorHKLE from) {
		this.h      = from.h;
		this.k      = from.k;
		this.l      = from.l;
		this.energy = from.energy;
	}
	
	public VectorHKLE duplicate() {
		VectorHKLE duplication;
		
		duplication = new VectorHKLE();
		duplication.assign(this);
		
		return duplication;
	}
	public String hklToString() {
		  String output=Utils.d2s(this.h)+" "+Utils.d2s(this.k)+" "+Utils.d2s(this.l);	  
		  return output;
	}

	public String toString() {
		  String output=Utils.d2s(this.h)+" "+Utils.d2s(this.k)+" "+Utils.d2s(this.l)+" "+Utils.d2s(this.energy);	  
		  return output;
	}
}