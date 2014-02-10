package eu.eyan.idakonyvtar.controller;

import javax.swing.JMenuBar;

public interface IControllerMenüvel<INPUT> extends IController<INPUT>
{
    JMenuBar getMenuBar();
}
