package cz.saroun.classes.definitions;


import java.util.zip.DataFormatException;


/**
 * Definition of spectrometer component shapes. Because number
 * representaion of shape is saved in file, its value must be
 * explicitly set in order to prevent problems if any constant
 * was removed.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:35 $</dt></dl>
 */
public enum Shape {
	ELLIPTIC    (0),
	RECTANGULAR (1),
	ELLIPSOIDAL (2),
	CYLINDRIC   (3),
	BOX         (4),
	DISC        (5);

	final int internalValue;


	Shape(int internalValue) {
		this.internalValue = internalValue;
	}


	public String toString() {
		switch (this) {
			case ELLIPTIC:
				return "Elliptic/Circular";
			case ELLIPSOIDAL:
				return "Ellipsoidal/Spheric";
			case CYLINDRIC:
				return "Cylindric";
			case RECTANGULAR:
				return "Rectangular";
			case DISC:
				return "Disc";
			case BOX:
				return "Box";
			default:
				return "__ERROR__";
		}
	}

	public int getInternalValue() {
		return internalValue;
	}

	public static Shape valueOf(int n) throws DataFormatException {
		switch(n) {
			case (0):
				return ELLIPTIC;
			case (1):
				return RECTANGULAR;
			case (2):
				return ELLIPSOIDAL;
			case (3):
				return CYLINDRIC;
			case (4):
				return BOX;
			case (5):
				return DISC;
			default:
				throw new DataFormatException("Cannot match type of Shape");
		}
	}
	
	/**
	 * Covert from the value used by Restrax kernel to GUI definition
	 * @param n
	 * @return
	 * @throws DataFormatException
	 */
	public static Shape valueOfRestrax(int n) throws DataFormatException {
		switch(n) {
			case (0):
				return ELLIPTIC;
			case (1):
				return CYLINDRIC;
			case (2):
				return DISC;
			case (3):
				return BOX;
			default:
				throw new DataFormatException("Cannot match type of Shape");
		}
	}
	
	public static Shape valueOfRestraxSource(int n) throws DataFormatException {
		switch(n) {
			case (0):
				return DISC;
			case (1):
				return RECTANGULAR;
			case (2):
				return DISC;
			case (3):
				return RECTANGULAR;
			default:
				throw new DataFormatException("Cannot match type of Shape");
		}
	}
	/**
	 * Get equivalent value used by Restrax kernel
	 * @param n
	 * @return
	 * @throws DataFormatException
	 */
	public static int toRestrax(Shape s) throws DataFormatException {
		switch(s) {
			case ELLIPTIC:
				return 0;
			case DISC:
				return 2;
			case CYLINDRIC:
				return 1;
			case RECTANGULAR:
			case BOX:
				return 3;
			default:
				throw new DataFormatException("Cannot match type of Shape");
		}
	}
}