package cz.saroun.classes.editors;


import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

import cz.saroun.classes.definitions.Utils;



/**
 * This class provides a component for handling number inputs. Components
 * for handling double or integer values can be obtained by calling
 * appropriate factory method.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:34 $</dt></dl>
 */
public class DoubleField extends NumberField {
	private static final long  serialVersionUID = 4703670178473801967L;

	protected static DoubleVerifier   doubleVerifier  = new DoubleVerifier();
	
	
	public DoubleField() {
		super();
		super.verifier = doubleVerifier;
	}
	
	
	public double getDouble() throws ParseException {
		double retVal;
		
		try {
			retVal = Utils.getDoubleFromTextField(this);
		} catch (ParseException ex) {
			if ((super.isEmptyFieldEnabled == false) ||
				(super.isEmpty() == false)) {
				super.setErrorBorderPainted(true);
			} else {
				/*
				 * Kdyz bych zadal chybnou hodnotu a pak prazdnou, zustal by chybovy ramecek
				 * proto to musim zde nulovat
				 */
				super.setErrorBorderPainted(false);
			}
			throw ex;
		}
		super.setErrorBorderPainted(false);
		return retVal;
	}

	public void setDouble(double val) {
		super.setErrorBorderPainted(false);
		super.setText(Utils.d2s(val));
	}
	
	private static class DoubleVerifier extends InputVerifier {
		public boolean verify(JComponent input) {
			try {
				((DoubleField)input).getDouble();
			} catch (ParseException ex) {
				return false;
			}
			return true;
		}
	}
}