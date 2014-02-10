package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;

public class Könyv
{
    private enum KönyvOszlop
    {
        SZERZŐ,
        SZERZŐMEGJEGYZÉS,
        CÍM,
        KIADÁS,
        MEGJELENÉSHELYE,
        KIADÓ,
        ÉV,
        TERJEDELEM,
        ÁR,
        SOROZAT,
        TÉMA,
        MEGJEGYZÉS,
        ISBN;

        final static ArrayList<KönyvOszlop> lista = newArrayList(values());

        public static KönyvOszlop getOszlopByIndex(final int columnIndex)
        {
            return lista.get(columnIndex);
        }
    }

    String szerző, szerzőmegjegyzés, cim, kiadás, megjelenéshelye, kiadó, év, terjedelem, ár, sorozat, téma, megjegyzés, isbn;

    public String getSzerző()
    {
        return szerző;
    }

    public void setSzerző(final String szerző)
    {
        this.szerző = szerző;
    }

    public String getSzerzőmegjegyzés()
    {
        return szerzőmegjegyzés;
    }

    public void setSzerzőmegjegyzés(final String szerzőmegjegyzés)
    {
        this.szerzőmegjegyzés = szerzőmegjegyzés;
    }

    public String getCim()
    {
        return cim;
    }

    public void setCim(final String cim)
    {
        this.cim = cim;
    }

    public String getKiadás()
    {
        return kiadás;
    }

    public void setKiadás(final String kiadás)
    {
        this.kiadás = kiadás;
    }

    public String getMegjelenéshelye()
    {
        return megjelenéshelye;
    }

    public void setMegjelenéshelye(final String megjelenéshelye)
    {
        this.megjelenéshelye = megjelenéshelye;
    }

    public String getKiadó()
    {
        return kiadó;
    }

    public void setKiadó(final String kiadó)
    {
        // FIXME: AutoCompleteDecorator Problem: Disgusting Hack but works...
        if (kiadó != null)
        {
            this.kiadó = kiadó;
        }
    }

    public String getÉv()
    {
        return év;
    }

    public void setÉv(final String év)
    {
        this.év = év;
    }

    public String getTerjedelem()
    {
        return terjedelem;
    }

    public void setTerjedelem(final String terjedelem)
    {
        this.terjedelem = terjedelem;
    }

    public String getÁr()
    {
        return ár;
    }

    public void setÁr(final String ár)
    {
        this.ár = ár;
    }

    public String getSorozat()
    {
        return sorozat;
    }

    public void setSorozat(final String sorozat)
    {
        this.sorozat = sorozat;
    }

    public String getTéma()
    {
        return téma;
    }

    public void setTéma(final String téma)
    {
        this.téma = téma;
    }

    public String getMegjegyzés()
    {
        return megjegyzés;
    }

    public void setMegjegyzés(final String megjegyzés)
    {
        this.megjegyzés = megjegyzés;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public void setIsbn(final String isbn)
    {
        this.isbn = isbn;
    }

    public String getOszlop(final int oszlopIndex)
    {
        switch (KönyvOszlop.getOszlopByIndex(oszlopIndex))
        {
            case SZERZŐ:
                return getSzerző();
            case CÍM:
                return getCim();
            case ISBN:
                return getIsbn();
            case KIADÁS:
                return getKiadás();
            case KIADÓ:
                return getKiadó();
            case MEGJEGYZÉS:
                return getMegjegyzés();
            case MEGJELENÉSHELYE:
                return getMegjelenéshelye();
            case SOROZAT:
                return getSorozat();
            case SZERZŐMEGJEGYZÉS:
                return getSzerzőmegjegyzés();
            case TERJEDELEM:
                return getTerjedelem();
            case TÉMA:
                return getTéma();
            case ÁR:
                return getÁr();
            case ÉV:
                return getÉv();
            default:
                return "Nincs a Könyvoszlopok között";
        }
    }

    public void setOszlop(final int oszlop, final String szöveg) throws Exception
    {
        switch (KönyvOszlop.getOszlopByIndex(oszlop))
        {
            case SZERZŐ:
                setSzerző(szöveg);
                break;
            case CÍM:
                setCim(szöveg);
                break;
            case ISBN:
                setIsbn(szöveg);
                break;
            case KIADÁS:
                setKiadás(szöveg);
                break;
            case KIADÓ:
                setKiadó(szöveg);
                break;
            case MEGJEGYZÉS:
                setMegjegyzés(szöveg);
                break;
            case MEGJELENÉSHELYE:
                setMegjelenéshelye(szöveg);
                break;
            case SOROZAT:
                setSorozat(szöveg);
                break;
            case SZERZŐMEGJEGYZÉS:
                setSzerzőmegjegyzés(szöveg);
                break;
            case TERJEDELEM:
                setTerjedelem(szöveg);
                break;
            case TÉMA:
                setTéma(szöveg);
                break;
            case ÁR:
                setÁr(szöveg);
                break;
            case ÉV:
                setÉv(szöveg);
                break;
            default:
                throw new Exception("Nincs a Könyvoszlopok között " + oszlop);
        }
    }
}
