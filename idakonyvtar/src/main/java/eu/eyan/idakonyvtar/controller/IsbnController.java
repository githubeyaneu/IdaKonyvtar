package eu.eyan.idakonyvtar.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.SwingUtilities;

import com.google.common.base.Joiner;

import eu.eyan.idakonyvtar.oszk.Marc;
import eu.eyan.idakonyvtar.oszk.MarcCodes;
import eu.eyan.idakonyvtar.oszk.MarcHelper;
import eu.eyan.idakonyvtar.oszk.OszkKereso;
import eu.eyan.idakonyvtar.oszk.OszkKeresoException;
import eu.eyan.idakonyvtar.view.IsbnDialogView;

public class IsbnController implements IDialogController<Void, Void>, ActionListener
{
    private IsbnDialogView view = new IsbnDialogView();

    public Component getView()
    {
        return view.getComponent();
    }

    @Override
    public String getTitle()
    {
        return "Isbn alapú keresés";
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == view.isbnText)
        {
            System.out.println("ISBN Action: " + view.isbnText.getText());
            view.isbnText.selectAll();
            view.result.setText("Keresés...");
            // TODO Asynchron
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    List<Marc> marcsToIsbn;
                    try
                    {
                        marcsToIsbn = OszkKereso.getMarcsToIsbn(view.isbnText.getText().replaceAll("ö", "0"));
                        String cim = MarcHelper.findMarc(marcsToIsbn, MarcCodes.CIM);
                        view.result.setText("<html>Cím: " + cim);
                        for (Marc marc : marcsToIsbn)
                        {
                            System.out.println(marc.getValue());
                            view.result.setText(view.result.getText() + "<br>\r\n"
                                    + Joiner.on(" - ").join(marc.getMarc1(), marc.getMarc2(), marc.getMarc3(), marc.getValue()).toString());
                        }
                        System.out.println("Talált: " + cim);
                    }
                    catch (OszkKeresoException e)
                    {
                        e.printStackTrace();
                        view.result.setText("Hiba: " + e.getCause().getMessage());
                    }
                }
            });

        }

    }

    @Override
    public Dimension getDefaultSize()
    {
        return new Dimension(300, 200);
    }

    @Override
    public void initData(Void model)
    {
    }

    @Override
    public void initDataBindings()
    {
        view.isbnText.addActionListener(this);
    }

    @Override
    public void onOk()
    {
    }

    @Override
    public void onCancel()
    {
    }

    @Override
    public Void getOutput()
    {
        return null;
    }
}
