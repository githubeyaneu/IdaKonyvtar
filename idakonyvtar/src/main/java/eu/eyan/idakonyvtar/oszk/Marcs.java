package eu.eyan.idakonyvtar.oszk;

public enum Marcs
{
    CIM("245", "10", "a");

    private String marc1;
    private String marc2;
    private String marc3;

    Marcs(String marc1, String marc2, String marc3)
    {
        this.marc1 = marc1;
        this.marc2 = marc2;
        this.marc3 = marc3;

    }

    public String getMarc1()
    {
        return marc1;
    }

    public void setMarc1(String marc1)
    {
        this.marc1 = marc1;
    }

    public String getMarc2()
    {
        return marc2;
    }

    public void setMarc2(String marc2)
    {
        this.marc2 = marc2;
    }

    public String getMarc3()
    {
        return marc3;
    }

    public void setMarc3(String marc3)
    {
        this.marc3 = marc3;
    }
}