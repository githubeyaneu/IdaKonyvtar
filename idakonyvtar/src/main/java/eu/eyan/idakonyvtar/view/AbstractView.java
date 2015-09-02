package eu.eyan.idakonyvtar.view;

import java.awt.Component;

public abstract class AbstractView implements IView
{
    private Component view;

    @Override
    public final Component getComponent()
    {
        if (this.view == null)
        {
            this.view = createViewComponent();
        }
        return this.view;
    }

    protected abstract Component createViewComponent();
}