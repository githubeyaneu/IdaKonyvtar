package eu.eyan.idakonyvtar.controller;

import java.awt.Window;

public interface IDialogController<INPUT, OUTPUT> extends IController<INPUT, OUTPUT>
{
    void onOk();

    void onCancel();

    void addResizeListener(Window window);
}
