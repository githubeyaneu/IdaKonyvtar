package eu.eyan.idakonyvtar.oszk;

public class Marc
{

    private String marc1;
    private String marc2;
    private String marc3;
    private String value;

    public Marc(String marc1, String marc2, String marc3,
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

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

}
