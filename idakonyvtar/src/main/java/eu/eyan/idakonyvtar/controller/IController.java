package eu.eyan.idakonyvtar.controller;

import java.awt.Component;

public interface IController<INPUT, OUTPUT>
{
    Component getView();

    String getTitle();

    void initData(INPUT input);

    void initBindings();

    OUTPUT getOutput();

    Component getComponentForFocus();
}