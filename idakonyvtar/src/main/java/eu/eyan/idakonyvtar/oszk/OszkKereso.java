package eu.eyan.idakonyvtar.oszk;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unix4j.Unix4j;

import eu.eyan.idakonyvtar.util.HttpHelper;

public class OszkKereso
{
    public static String isbnKeresOszkban(final String isbn) throws IOException
    {
        // Login1
        String session_id = findTextInUrl(
                "http://nektar1.oszk.hu/LVbin/LibriVision/lv_login.html",
                "USER_LOGIN=Nektar_LV_user&USER_PASSWORD=Nektar&LanguageCode=hu&CountryCode=hu&HtmlSetCode=default&lv_action=LV_Login&image3.x=17&image3.y=9",
                "SESSIO",
                "SESSION_ID=",
                "([0-9]*_[0-9]*)",
                "&");
        System.out.println("Session Id:" + session_id);

        // Login2
        HttpHelper.postUrl("http://nektar1.oszk.hu/LVbin/LibriVision/lv_search_form.html?SESSION_ID=" + session_id + "&lv_action=LV_Search_Form&HTML_SEARCH_TYPE=SIMPLE&DB_ID=2", "");

        // Keresés aztán Marc rövid formátum keresése
        String marcLink = findTextInUrl(
                "http://nektar1.oszk.hu/LVbin/LibriVision/lv_view_records.html",
                "SESSION_ID=" + session_id + "&lv_action=LV_Search&QUERY_ID=T_1391112123&ADD_QUERY=-1&SEARCH_TYPE=QUERY_SIMPLE&HTML_SEARCH_TYPE=SIMPLE&USE=BN&_QUERY=" + isbn + "&QUERY=" + isbn + "&sub_button=Keres%C3%A9s",
                "A record MARC form",
                "href=\"",
                ".*",
                "\">MARC form");
        System.out.println("marcLink:" + marcLink);

        // Marc rövid, marc hosszú keresése
        String fullMarcLink = findTextInUrl(
                "http://nektar1.oszk.hu/LVbin/LibriVision/" + marcLink,
                "",
                "Teljes megjelenítés",
                "href=\"",
                ".*",
                "\">Teljes");
        System.out.println("fullMarcLink:" + fullMarcLink);

        // Marc hosszú
        return HttpHelper.postUrl("http://nektar1.oszk.hu/LVbin/LibriVision/" + fullMarcLink, "");
    }

    private static String findTextInUrl(final String host, final String postParameter, final String lineGrep, final String regex_prefix, final String regex, final String regexPost) throws IOException
    {
        String postUrl = HttpHelper.postUrl(host, postParameter);
        String line = Unix4j.fromString(postUrl).grep(lineGrep).toStringResult().toString();
        Matcher matcher = Pattern.compile(regex_prefix + regex + regexPost).matcher(line);
        matcher.find();
        String group = matcher.group();
        String session_id = group.substring(regex_prefix.length(), group.length() - regexPost.length()).replaceAll("&amp;", "&");
        return session_id;
    }

    public static List<Marc> getMarcsToIsbn(String isbn) throws OszkKeresoException
    {
        List<Marc> marcs = newArrayList();
        try
        {
            String source = isbnKeresOszkban(isbn);
//			String source = FileUtils.readFileToString(new File(Resources.getResource("marc_test.html").getFile()));
//			System.out.println(source);
            source = source.replace("\r\n", "");

            Matcher marcTableMatcher = Pattern.compile("<table class=\"record\">.*?</table>").matcher(source);
            marcTableMatcher.find();
            String marcTable = marcTableMatcher.group();
//	        System.out.println(marcTable);

            Matcher marcSorMatcher = Pattern.compile("<tr.*?</tr").matcher(marcTable);
            String lastMarc1 = "", lastMarc2 = "", lastMarc3 = "";
            while (marcSorMatcher.find())
            {
                String sor = marcSorMatcher.group();
                String marc1 = sor.substring(50 - 1, 53 - 1).trim();
                String marc2 = sor.substring(103 - 1, 105 - 1).trim();
                String marc3 = sor.substring(155 - 1, 156 - 1).trim();
                String érték = sor.substring(206 - 1, sor.length() - 9).trim();
                if (!marc1.equals(lastMarc1) && !marc1.equals(""))
                {
                    lastMarc1 = marc1;
                    lastMarc2 = marc2;
                }
                else if (!marc2.equals(lastMarc2) && !marc2.equals(""))
                {
                    lastMarc2 = marc2;
                }
                lastMarc3 = marc3;

//				System.out.print(new StringBuilder()
//				.append("-")
//	        	.append(marc1)
//	        	.append(" ")
//	        	.append(marc2)
//	        	.append(" ")
//	        	.append(marc3)
//	        	.append(" ")
//	        	.append(érték)
//	        	);
//				System.out.println("+"+lastMarc1+" "+lastMarc2+" "+lastMarc3);
                marcs.add(new Marc(lastMarc1, lastMarc2, lastMarc3, érték));
            }

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new OszkKeresoException("Sikertelen.", e);
        }
        System.out.println("Siker");
        return marcs;
    }
}
