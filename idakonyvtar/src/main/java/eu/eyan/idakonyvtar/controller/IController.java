package eu.eyan.idakonyvtar.controller;

import java.awt.Component;
import java.awt.Dimension;

public interface IController<MODEL>
{
    public Component getView();

    public String getTitle();

    public Dimension getDefaultSize();

    public void initData(MODEL model);

    public void initDataBindings();
}
