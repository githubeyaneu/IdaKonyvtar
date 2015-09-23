package eu.eyan.idakonyvtar.oszk;

public class MarcJ
{
    private String marc1;
    private String marc2;
    private String marc3;
    private String value;

    public MarcJ(String marc1, String marc2, String marc3,
            String value)
    {
        this.marc1 = marc1;
        this.marc2 = marc2;
        this.marc3 = marc3;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "Marc [marc1=" + marc1 + ", marc2=" + marc2 + ", marc3=" + marc3
                + ", value=" + value + "]";
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

    public String getValue()
    {
        return value;
    }
}