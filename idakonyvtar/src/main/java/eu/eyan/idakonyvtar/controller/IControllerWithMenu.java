package eu.eyan.idakonyvtar.controller;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;

public interface IControllerWithMenu<INPUT, OUTPUT> extends IController<INPUT, OUTPUT>
{
    JMenuBar getMenuBar();

    JToolBar getToolBar();
}
