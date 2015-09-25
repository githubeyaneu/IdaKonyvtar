package eu.eyan.idakonyvtar.oszk;

import com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unix4j.Unix4j;

import eu.eyan.idakonyvtar.util.HttpHelper;

/**
 * This is the code to acquire the hungarian book data -> this is why it is not translated.
 */
object OszkKereso {

  @throws(classOf[IOException])
  def isbnKeresOszkban(isbn: String): String = {

    // Login1
    val session_id = findTextInUrl(
      "http://nektar1.oszk.hu/LVbin/LibriVision/lv_login.html",
      "USER_LOGIN=Nektar_LV_user&USER_PASSWORD=Nektar&LanguageCode=hu&CountryCode=hu&HtmlSetCode=default&lv_action=LV_Login&image3.x=17&image3.y=9",
      "SESSIO", "SESSION_ID=", "([0-9]*_[0-9]*)", "&")
    // System.out.println("Session Id:" + session_id);

    // Login2
    HttpHelper
      .postUrl(
        "http://nektar1.oszk.hu/LVbin/LibriVision/lv_search_form.html?SESSION_ID="
          + session_id
          + "&lv_action=LV_Search_Form&HTML_SEARCH_TYPE=SIMPLE&DB_ID=2",
        "")

    // Keresés aztán Marc rövid formátum keresése
    val marcLink = findTextInUrl(
      "http://nektar1.oszk.hu/LVbin/LibriVision/lv_view_records.html",
      "SESSION_ID="
        + session_id
        + "&lv_action=LV_Search&QUERY_ID=T_1391112123&ADD_QUERY=-1&SEARCH_TYPE=QUERY_SIMPLE&HTML_SEARCH_TYPE=SIMPLE&USE=BN&_QUERY="
        + isbn + "&QUERY=" + isbn + "&sub_button=Keres%C3%A9s",
      "A record MARC form", "href=\"", ".*", "\">MARC form")
    // System.out.println("marcLink:" + marcLink);

    // Marc rövid, marc hosszú keresése
    val fullMarcLink = findTextInUrl(
      "http://nektar1.oszk.hu/LVbin/LibriVision/" + marcLink, "", "Teljes megjelenítés", "href=\"", ".*", "\">Teljes")
    // System.out.println("fullMarcLink:" + fullMarcLink);

    // Marc hosszú
    HttpHelper.postUrl("http://nektar1.oszk.hu/LVbin/LibriVision/" + fullMarcLink, "")
  }

  @throws(classOf[IOException])
  def findTextInUrl(host: String,
                    postParameter: String, lineGrep: String,
                    regex_prefix: String, regex: String,
                    regexPost: String): String = {

    val postUrl = HttpHelper.postUrl(host, postParameter)
    val line = Unix4j.fromString(postUrl).grep(lineGrep).toStringResult()
    val matcher = Pattern.compile(regex_prefix + regex + regexPost).matcher(line)
    matcher.find()
    val group = matcher.group()
    val sessionId = group.substring(regex_prefix.length(), group.length() - regexPost.length()).replaceAll("&amp;", "&")
    sessionId
  }

  @throws(classOf[OszkKeresoException])
  def getMarcsToIsbn(isbn: String) = {
    val marcs: java.util.List[Marc] = new java.util.ArrayList()
    try {
      var source = isbnKeresOszkban(isbn)
      // String source = FileUtils.readFileToString(new
      // File(Resources.getResource("marc_test.html").getFile()));
      // System.out.println(source);
      source = source.replace("\r\n", "")

      val marcTableMatcher = Pattern.compile("<table class=\"record\">.*?</table>").matcher(source)
      marcTableMatcher.find()
      val marcTable = marcTableMatcher.group()
      // System.out.println(marcTable);

      //      val pattern = "<tr.*?</tr".r
      //      pattern.findAllIn(marcTable)

      val marcSorMatcher = Pattern.compile("<tr.*?</tr").matcher(marcTable)
      var lastMarc1 = ""
      var lastMarc2 = ""
      var lastMarc3 = ""
      while (marcSorMatcher.find()) {
        val sor = marcSorMatcher.group()
        val marc1 = sor.substring(50 - 1, 53 - 1).trim()
        val marc2 = sor.substring(103 - 1, 105 - 1).trim()
        val marc3 = sor.substring(155 - 1, 156 - 1).trim()
        val érték = sor.substring(206 - 1, sor.length() - 9).trim()
        if (!marc1.equals(lastMarc1) && !marc1.equals("")) {
          lastMarc1 = marc1
          lastMarc2 = marc2
        } else if (!marc2.equals(lastMarc2) && !marc2.equals("")) {
          lastMarc2 = marc2
        }
        lastMarc3 = marc3

        // System.out.print(new StringBuilder()
        // .append("-")
        // .append(marc1)
        // .append(" ")
        // .append(marc2)
        // .append(" ")
        // .append(marc3)
        // .append(" ")
        // .append(érték)
        // );
        // System.out.println("+"+lastMarc1+" "+lastMarc2+" "+lastMarc3);
        marcs.add(new Marc(lastMarc1, lastMarc2, lastMarc3, érték));
      }

    } catch {
      case e: Throwable => {
        e.printStackTrace();
        throw new OszkKeresoException("Sikertelen.", e);
      }
    }
    // System.out.println("Siker");
    marcs
  }
}
