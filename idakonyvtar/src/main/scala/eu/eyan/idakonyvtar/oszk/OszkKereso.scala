package eu.eyan.idakonyvtar.oszk;

import java.io.IOException

import org.unix4j.Unix4j

import com.google.common.collect.Lists

import eu.eyan.log.Log
import eu.eyan.util.http.HttpPlus
import eu.eyan.util.string.StringPlus.StringPlusImplicit

/*
 * This is the code to acquire the hungarian book data -> that is why it is not translated.
 */

class OszkKeresoException(message: String, cause: Throwable) extends Exception(message, cause)
class Marc(val marc1: String, val marc2: String, val marc3: String, val value: String) {
  override def toString() = "Marc [marc1=" + marc1 + ", marc2=" + marc2 + ", marc3=" + marc3 + ", value=" + value + "]"
}

object OszkKereso {

  @throws(classOf[IOException])
  def isbnKeresOszkban(isbn: String): String = {

    // Login1
    val session_id = findTextInUrl(
      "http://nektar1.oszk.hu/LVbin/LibriVision/lv_login.html",
      "USER_LOGIN=Nektar_LV_user&USER_PASSWORD=Nektar&LanguageCode=hu&CountryCode=hu&HtmlSetCode=default&lv_action=LV_Login&image3.x=17&image3.y=9",
      "SESSIO", "SESSION_ID=", "([0-9]*_[0-9]*)", "&")
    Log.debug("Session Id:" + session_id);

    // Login2
    HttpPlus.sendPost(
        "http://nektar1.oszk.hu/LVbin/LibriVision/lv_search_form.html?SESSION_ID="
          + session_id
          + "&lv_action=LV_Search_Form&HTML_SEARCH_TYPE=SIMPLE&DB_ID=2",
        "")

    // Keresés aztán Marc rövid formátum keresése
    val marcLink = findTextInUrl(
      "http://nektar1.oszk.hu/LVbin/LibriVision/lv_view_records.html",
      "SESSION_ID=" + session_id
        + "&lv_action=LV_Search&QUERY_ID=T_1391112123&ADD_QUERY=-1&SEARCH_TYPE=QUERY_SIMPLE&HTML_SEARCH_TYPE=SIMPLE&USE=BN&_QUERY=" + isbn
        + "&QUERY=" + isbn
        + "&sub_button=Keres%C3%A9s",
      "A record MARC form",
      "href=\"",
      ".*",
      "\">MARC form")
    Log.debug("marcLink:" + marcLink);

    // Marc rövid, marc hosszú keresése
    val fullMarcLink = findTextInUrl(
      "http://nektar1.oszk.hu/LVbin/LibriVision/" + marcLink, "", "Teljes megjelen", "href=\"", ".*", "\">Teljes")
    Log.debug("fullMarcLink:" + fullMarcLink);

    // Marc hosszú
    s"http://nektar1.oszk.hu/LVbin/LibriVision/$fullMarcLink".asUrlPost("").mkString
  }

  @throws(classOf[IOException])
  def findTextInUrl(host: String,
    postParameter: String,
    lineGrep: String,
    regex_prefix: String,
    regex: String,
    regexPost: String): String = {

    val postUrl = host.asUrlPost(postParameter).mkString
		Log.info("postUrl: "+ postUrl)
//    val line = Unix4j.fromString(postUrl).grep(lineGrep).toStringResult()
    val line = postUrl.lines.filter(_.contains(lineGrep)).toList(0)
    Log.info("Line: "+ line)
    val firstMatch = (regex_prefix + regex + regexPost).r.findFirstIn(line).get

    firstMatch.substring(regex_prefix.length(), firstMatch.length() - regexPost.length()).replaceAll("&amp;", "&")
  }

  @throws(classOf[OszkKeresoException])
  def getMarcsToIsbn(isbn: String): List[Marc] = {
    try {
      var source = isbnKeresOszkban(isbn).replaceAll("[\r\n]", "")
      val marcTable = "<table class=\"record\">.*?</table>".r.findFirstIn(source).get

      var lastMarc1 = ""
      var lastMarc2 = ""
      var lastMarc3 = ""

      val sorok = "<tr.*?</tr".r.findAllIn(marcTable)
      val marcs = (for { sor <- sorok } yield {
        val marc1 = sor.substring(50 - 1, 53 - 1).trim()
        val marc2 = sor.substring(103 - 1, 105 - 1).trim()
        val marc3 = sor.substring(155 - 1, 156 - 1).trim()
        val value = sor.substring(206 - 1, sor.length() - 9).trim()
        if (marc1 != lastMarc1 && marc1 != "") {
          lastMarc1 = marc1
          lastMarc2 = marc2
        }
        else if (marc2 != lastMarc2 && marc2 != "") {
          lastMarc2 = marc2
        }
        lastMarc3 = marc3

        val m = new Marc(lastMarc1, lastMarc2, lastMarc3, value)
        Log.info(m.toString())
        m
      }).toList

      marcs.foreach(m => Log.debug(m.toString()))
      marcs
    }
    catch {
      case e: Throwable => {
        e.printStackTrace();
        throw new OszkKeresoException("Sikertelen.", e)
      }
    }
  }
}