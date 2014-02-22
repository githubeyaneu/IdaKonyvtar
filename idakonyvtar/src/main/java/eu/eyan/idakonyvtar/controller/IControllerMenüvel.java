package eu.eyan.idakonyvtar.controller;

import javax.swing.JMenuBar;

public interface IControllerMenüvel<INPUT, OUTPUT> extends IController<INPUT, OUTPUT>
{
    JMenuBar getMenuBar();
}
