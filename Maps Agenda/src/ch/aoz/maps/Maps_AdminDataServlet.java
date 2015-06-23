package ch.aoz.maps;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.*;

import org.json.*;

import ch.aoz.maps.Event;
import ch.aoz.maps.Language;
import ch.aoz.maps.Phrase;
import ch.aoz.maps.Phrases;

@SuppressWarnings("serial")
public class Maps_AdminDataServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      String response = null;
      switch (req.getParameter("type")) {
        case "languages":
          response = getLanguages();
          break;
        case "events":
          response = getEvents(req);
          break;
        case "phrases":
          response = getPhrases(req);
          break;
        case "subscribers":
          response = getSubscribers();
          break;
        case "translators":
          response = getTranslators();
          break;
        case "newsletter":
          response = getNewsletters(req);
          break;
        case "campaign":
          response = createCampaign(req);
          break;
        case "mtranslators":
          response = modifyTranslators(req);
          break;
      }
      if (response == null) {
        
      }
      resp.setContentType("application/json");
      resp.getWriter().println(response);
    }
        
    private String getPhrases(HttpServletRequest req) {
      String lang = req.getParameter("lang");
      Phrases phrases = Phrases.GetPhrasesForLanguage(lang);
      StringBuilder response = new StringBuilder();
      response.append("{ \"phrases\": [");
      for (Phrase p : phrases.getPhrases()) {
        response.append("{\"key\":\"" + p.getKey() + "\",");
        response.append("\"group\":\"" + Utils.toUnicode(p.getGroup()) + "\",");
        response.append("\"phrase\":\"" + Utils.toUnicode(p.getPhrase()) + "\",");
        response.append("\"isTag\":" + p.isTag() + "},");
      }
      if (phrases.getPhrases().size() > 0) {
        // Remove the last comma.
        response.deleteCharAt(response.length() - 1);
      }
      response.append("]}");
      return response.toString();
    }

    private String getEvents(HttpServletRequest req) {
      Language lang = Language.GetByCode(req.getParameter("lang"));
      if (lang == null) {
        lang = Language.GetByCode("de");
      }
      
      Calendar date = Calendar.getInstance();
      String requested_date = req.getParameter("month");
      if (requested_date != null) {
        try {
          date.setTime(new SimpleDateFormat("yyyy-MM").parse(requested_date));
        } catch (Exception e) {
        }
      }
      // Set the time at midnight, so that the below query stays the same.
      date.set(Calendar.MILLISECOND, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.DATE, 1);

      Events events = Events.getEvents(date, lang.getCode());
      
      StringBuilder response = new StringBuilder();
      response.append("{ \"events\": [");
      for (Event e : events.getSortedEvents()) {
        EventDescription d = e.getDescription();
        if (d != null) {
          response.append("{");
          response.append("\"date\":\"").append(dateToString(e.getDate())).append("\",");
          response.append("\"title\":\"").append(Utils.toUnicode(d.getTitle())).append("\",");
          response.append("\"description\":\"").append(Utils.toUnicode(d.getDesc())).append("\",");
          response.append("\"location\":\"").append(Utils.toUnicode(e.getLocation())).append("\",");
          response.append("\"transit\":\"").append(Utils.toUnicode(e.getTransit())).append("\",");
          response.append("\"url\":\"").append(Utils.toUnicode(e.getUrl())).append("\",");
          response.append("\"tags\": [");
          for (String tag : e.getTags()) {
            response.append("\"").append(Utils.toUnicode(tag)).append("\",");
          }
          if (response.charAt(response.length() - 1) == ',') {
            response.deleteCharAt(response.length() - 1);  // remove the last ,
          }
          response.append("]},");
        }
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }
      
      response.append("]}");
      return response.toString();
    }

    public String dateToString(Date d) {      
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      return new StringBuilder()
          .append(c.get(Calendar.YEAR))
          .append('-')
          .append(c.get(Calendar.MONTH) + 1)
          .append('-')
          .append(c.get(Calendar.DAY_OF_MONTH))
          .toString();
    }
    
    public String getLanguages() {
      StringBuilder response = new StringBuilder();
      response.append("{ \"languages\": [");

      Set<Language> langs = Language.getAllLanguages();
      for (Language l : langs) {
        response.append("{\"code\":\"" + l.getCode() + "\",");
        response.append("\"germanName\":\"").append(Utils.toUnicode(l.getGermanName())).append("\",");
        response.append("\"name\":\"").append(Utils.toUnicode(l.getName())).append("\",");
        response.append("\"days\":[");
        for (String day : l.getDaysOfTheWeek()) {
          response.append("\"").append(Utils.toUnicode(day)).append("\",");
        }
        if (response.charAt(response.length() - 1) == ',') {
          response.deleteCharAt(response.length() - 1);  // remove the last ,
        }
        response.append("],");
        response.append("\"isRtl\":").append(l.isRightToLeft()).append(",");
        response.append("\"inAgenda\":").append(l.isInAgenda()).append(",");
        response.append("\"specificFormat\":").append(l.hasSpecificFormat()).append("},");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }
      
      response.append("]}");
      return response.toString();
    }
    
    public String getTranslators() {
      StringBuilder response = new StringBuilder();
      response.append("{ \"translators\": [");

      Map<String, Translator> translators = Translator.getAllTranslators();
      for (String email : translators.keySet()) {
        Translator t = translators.get(email);
        response.append("{");
        response.append("\"email\":\"").append(Utils.toUnicode(t.getEmail())).append("\",");
        response.append("\"name\":\"").append(Utils.toUnicode(t.getName())).append("\",");
        response.append("\"langs\":[");
        for (String l : t.getLanguages()) {
          response.append("\"").append(Utils.toUnicode(l)).append("\",");
        }
        if (response.charAt(response.length() - 1) == ',') {
          response.deleteCharAt(response.length() - 1);  // remove the last ,
        }
        response.append("]},");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }      
      response.append("]}");
      return response.toString();
    }

    public String getSubscribers() {
      StringBuilder response = new StringBuilder();
      response.append("{ \"subscribers\": [");

      Map<String, Subscriber> subscribers = Subscriber.getAllSubscribers();
      for (String email : subscribers.keySet()) {
        Subscriber s = subscribers.get(email);
        response.append("{");
        response.append("\"email\":\"").append(Utils.toUnicode(s.getEmail())).append("\",");
        response.append("\"name\":\"").append(Utils.toUnicode(s.getName())).append("\",");
        response.append("\"lang\":\"").append(Utils.toUnicode(s.getLanguage())).append("\",");
        response.append("\"hash\":\"").append(Utils.toUnicode(s.getHash())).append("\"},");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }      
      response.append("]}");
      return response.toString();
    }

    public String getNewsletters(HttpServletRequest req) {
      Calendar date = Calendar.getInstance();
      String requested_date = req.getParameter("month");
      if (requested_date != null) {
        try {
          date.setTime(new SimpleDateFormat("yyyy-MM").parse(requested_date));
        } catch (Exception e) {
        }
      }
      // Set the time at midnight, so that the below query stays the same.
      date.set(Calendar.MILLISECOND, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.DATE, 1);

      StringBuilder response = new StringBuilder();
      response.append("{ \"newsletters\": {");

      Set<Language> langs = Language.getAllLanguages();
      Events eventsDe = Events.getEvents(date, "de");
      String baseUrl = "localhost".equals(req.getServerName()) ? 
              "http://localhost:8888" : "http://www.maps-agenda.ch";
      
      for (Language l : langs) {
        Events eventsLang = null;
        if (!l.getCode().equals("de")) {
          eventsLang = eventsDe.clone();
          eventsLang.loadDescriptions(l.getCode());
        }
        NewsletterExport exporter = new NewsletterExport(
                eventsDe, eventsLang, l.getCode(),
                baseUrl, date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                null /* subscriber, none for public render. */);
        
        response.append("\"" + l.getCode() + "\":\"" + Utils.toUnicode(exporter.render()) + "\",");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }      
      response.append("}}");
      return response.toString();
    }

    public String createCampaign(HttpServletRequest req) {
      Calendar date = Calendar.getInstance();
      String requested_date = req.getParameter("month");
      if (requested_date != null) {
        try {
          date.setTime(new SimpleDateFormat("yyyy-MM").parse(requested_date));
        } catch (Exception e) {
        }
      }
      // Set the time at midnight, so that the below query stays the same.
      date.set(Calendar.MILLISECOND, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.HOUR_OF_DAY, 0);
      date.set(Calendar.DATE, 1);

      String background_color = req.getParameter("bgcolor");
      if (background_color == null) {
        background_color = BackgroundColor.fetchFromStore().getColor();
      }
      
      JSONObject options = new JSONObject();
      options.put("list_id", "9357c08f67");  // TEST ONLY. // TODO fix.
      options.put("subject",
	      "MAPS Agenda Newsletter "
      + date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.GERMAN)
      + " " + date.get(Calendar.YEAR));
      options.put("from_name", "Pascal Gwosdek");  // TODO fix.
      options.put("from_email", "pascalgwosdek@google.com");  // TODO fix.
      options.put("to_name", "*|NAME|*");
      options.put("generate_text", true);
      
      JSONObject content = new JSONObject();
      content.put("html", "Please paste text here.");  // TODO fix.
      
      JSONObject json = new JSONObject();
      json.put("apikey", "bd323b0babe5a6615a7c5b0a1adab0fa-us10");
      json.put("type", "regular");
      json.put("options", options);
      json.put("content", content);
      String mailchimpRequest = json.toString();
      
      StringBuilder response = new StringBuilder();
      HttpURLConnection connection = null;
      try {
	  URL url = new URL("https://us10.api.mailchimp.com/2.0/campaigns/create");
	  connection = (HttpURLConnection)url.openConnection();
	  connection.setRequestMethod("POST");
	  connection.setRequestProperty("Content-Type", "application/json");
	  connection.setRequestProperty("Content-Length", Integer.toString(mailchimpRequest.length()));
	  connection.setDoOutput(true);
	  connection.setUseCaches(false);
	  
	  DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
	  outStream.writeBytes(mailchimpRequest);
	  outStream.close();
	  
	  InputStreamReader inStream = new InputStreamReader(connection.getInputStream());
	  BufferedReader reader = new BufferedReader(inStream);
	  String line;
	  while((line = reader.readLine()) != null) {
	      response.append(line);
	      response.append('\r');
	  }
	  reader.close();  
      } catch (Exception e) {
	  return "Error while sending request to MailChimp: " + e.toString();
      } finally {
	  if (connection != null) {
	      connection.disconnect();
	  }
      }
      return response.toString();
    }
    
    public String modifyTranslators(HttpServletRequest req) {
      StringBuilder response = new StringBuilder();      
      if (req.getParameter("modifications") == null || req.getParameter("modifications").equals("")) {
        response.append("Nothing.");
        return response.toString();
      } 
      
      try {
        JSONObject json = new JSONObject(req.getParameter("modifications"));
        response.append("Num keys: " + json.length());
      } catch (JSONException e) {
        response.append("Error: " + e.getMessage());
      }
      return response.toString();
    }
}
