package eu.eyan.idakonyvtar.controller;

import java.awt.Component;
import java.awt.Dimension;

public interface IController
{
    public Component getView();

    public String getTitle();

    public Dimension getSize();
}
