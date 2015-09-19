package eu.eyan.idakonyvtar.util;

import java.awt.Window;

import javax.swing.JDialog;

public class OkCancelDialog extends JDialog {
	public OkCancelDialog(Window owner) {
		super(owner);
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	private static final long serialVersionUID = 1L;

	private boolean ok = false;
}