package eu.eyan.idakonyvtar.controller;

import java.awt.Component;
import java.awt.Dimension;

public interface IController<INPUT>
{
    public Component getView();

    public String getTitle();

    public Dimension getDefaultSize();

    public void initData(INPUT input);

    public void initDataBindings();
}
