package cz.jstools.classes.editors;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.InputVerifier;
import javax.swing.JTextField;
import javax.swing.border.Border;


/**
 * This class provides a component for handling number inputs. Components
 * for handling double or integer values can be obtained by calling
 * appropriate factory method.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:34 $</dt></dl>
 */
public abstract class NumberField extends JTextField {
	private static final long serialVersionUID = 1L;
	private static Border           defaultBorder   = new JTextField().getBorder();
	private static Border           errorBorder     = javax.swing.BorderFactory.createLineBorder(Color.RED, 1);
	
	private boolean          isErrorBorderPainted = false;
	protected InputVerifier  verifier             = null;
	protected boolean        isEmptyFieldEnabled  = false;
	
	
	public NumberField() {
		super();
		super.setHorizontalAlignment(RIGHT);
		super.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				NumberField.this.verifier.verify(NumberField.this);
			}
		});
		super.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NumberField.this.verifier.verify(NumberField.this);
			}
		});
	}
	
	
	public boolean isEmptyFieldEnabled() {
		return isEmptyFieldEnabled;
	}

	public void setEmptyFieldEnabled(boolean isEmptyFieldEnabled) {
		this.isEmptyFieldEnabled = isEmptyFieldEnabled;
	}

	public boolean isEmpty() {
		return ((super.getText() == null) || (super.getText().trim().length() == 0));
	}

	
	protected void setErrorBorderPainted(boolean newValue) {
		if (isErrorBorderPainted != newValue) {
			isErrorBorderPainted = newValue;
			if (isErrorBorderPainted) {
				super.setBorder(errorBorder);
			} else {
				super.setBorder(defaultBorder);
			}
		}
	}
}