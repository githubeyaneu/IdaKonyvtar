package eu.eyan.idakonyvtar.controller;

public interface IDialogController<INPUT, OUTPUT> extends IController<INPUT, OUTPUT>
{
    void onOk();

    void onCancel();
}
