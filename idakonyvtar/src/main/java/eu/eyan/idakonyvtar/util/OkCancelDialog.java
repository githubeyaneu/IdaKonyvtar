package eu.eyan.idakonyvtar.util;

import java.awt.Window;

import javax.swing.JDialog;

import lombok.Getter;

public class OkCancelDialog extends JDialog
{
    public OkCancelDialog(Window owner)
    {
        super(owner);
    }

    private static final long serialVersionUID = 1L;

    @Getter boolean ok = false;
}