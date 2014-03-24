package eu.eyan.idakonyvtar.controller;

import java.awt.Component;

public interface IController<INPUT, OUTPUT>
{
    public Component getView();

    public String getTitle();

    public void initData(INPUT input);

    public void initBindings();

    public OUTPUT getOutput();

    public Component getComponentForFocus();
}
