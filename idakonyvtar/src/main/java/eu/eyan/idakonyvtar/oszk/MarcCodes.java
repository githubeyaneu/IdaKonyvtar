package eu.eyan.idakonyvtar.oszk;

public enum MarcCodes
{
    CIM("245", "10", "a");

    private String marc1;
    private String marc2;
    private String marc3;

    MarcCodes(String marc1, String marc2, String marc3)
    {
        this.marc1 = marc1;
        this.marc2 = marc2;
        this.marc3 = marc3;
    }

    public String getMarc1()
    {
        return marc1;
    }

    public String getMarc2()
    {
        return marc2;
    }

    public String getMarc3()
    {
        return marc3;
    }
}