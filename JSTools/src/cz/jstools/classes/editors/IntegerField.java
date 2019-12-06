package cz.jstools.classes.editors;


import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

import cz.jstools.classes.definitions.Utils;



/**
 * This class provides a component for handling number inputs. Components
 * for handling double or integer values can be obtained by calling
 * appropriate factory method.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2018/01/17 13:20:03 $</dt></dl>
 */
public class IntegerField extends NumberField {
	private static final long  serialVersionUID = 4703670178473801967L;
	
	private static IntegerVerifier  integerVerifier = new IntegerVerifier();
	
	
	public IntegerField() {
		super();
		super.verifier = integerVerifier;
	}


	public int getInt() throws ParseException {
		int retVal;
		
		try {
			retVal = Utils.getIntFromTextField(this);
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

	public void setInt(int val) {
		super.setErrorBorderPainted(false);
		super.setText(Utils.i2s(val));
	}
	
	
	private static class IntegerVerifier extends InputVerifier {
		public boolean verify(JComponent input) {
			try {
				((IntegerField)input).getInt();
			} catch (ParseException ex) {
				return false;
			}
			return true;
		}
	}
}