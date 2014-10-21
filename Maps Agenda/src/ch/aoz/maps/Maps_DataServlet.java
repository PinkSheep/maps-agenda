package ch.aoz.maps;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import ch.aoz.maps.Event;
import ch.aoz.maps.Language;
import ch.aoz.maps.Phrase;

@SuppressWarnings("serial")
public class Maps_DataServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
      String response = null;
      switch (req.getParameter("type")) {
        case "events":
          response = getEvents(req);
          break;
        case "tags":
          response = getTags();
          break;
      }
      if (response == null) {
        
      }
      resp.setContentType("application/json");
      resp.getWriter().println(response);
    }
    
    private String getTags() {
      List<String> tags = Phrase.GetKeysForTags();
      StringBuilder response = new StringBuilder();
      response.append("{ \"tags\": [");
      for (String tag : tags) {
        response.append("\"" + tag + "\",");
      }
      if (!tags.isEmpty()) {
        // Remove the last comma.
        response.deleteCharAt(response.length() - 1);
      }
      response.append("]}");
      return response.toString();
    }
    
    private String getEvents(HttpServletRequest req) {
      String lang = req.getParameter("lang");
      if (lang == null) {
        lang = "de";
      }

      int num_events;
      try {
        num_events = Integer.parseInt(req.getParameter("numEvents"));
      } catch (Exception e) {
        num_events = 15;
      }

      // Can be null.
      String startCursor = req.getParameter("cursor");
      
      Calendar date = Calendar.getInstance();
      String requested_date = req.getParameter("startDate");
      if (requested_date != null) {
        try {
          date.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(requested_date));
        } catch (Exception e) {
        }
      }
      // Set the time at midnight, so that the below query stays the same.
      date.set(Calendar.MILLISECOND, 0);
      date.set(Calendar.SECOND, 0);
      date.set(Calendar.MINUTE, 0);
      date.set(Calendar.HOUR_OF_DAY, 0);

      Language l = Language.GetByCode(lang);
      List<Event> events = new ArrayList<Event>(num_events);
      String cursor = Event.GetNextEvents(date, num_events, startCursor, events);
      
      StringBuilder response = new StringBuilder();
      response.append("{ \"events\": [");
      for (Event e : events) {
        Translation t = e.getTranslation(l);
        if (t != null) {
          response.append("{");
          response.append("\"date\":\"").append(dateToString(e.getDate())).append("\",");
          response.append("\"title\":\"").append(Utils.toUnicode(t.getTitle())).append("\",");
          response.append("\"description\":\"").append(Utils.toUnicode(t.getDesc())).append("\",");
          response.append("\"location\":\"").append(Utils.toUnicode(t.getLocation())).append("\",");
          response.append("\"transit\":\"").append(Utils.toUnicode(t.getTransit())).append("\",");
          response.append("\"url\":\"").append(Utils.toUnicode(t.getUrl())).append("\"");
          response.append("},");
        }
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }
      
      response.append("], \"strings\": {");
      for (Phrase phrase : getPhrases(lang).values()) {
        response.append("\"").append(phrase.getKey()).append("\":");
        response.append("\"").append(Utils.toUnicode(phrase.getPhrase())).append("\",");
      }
      if (response.charAt(response.length() - 1) == ',') {
        response.deleteCharAt(response.length() - 1);  // remove the last ,
      }
      response.append("}, \"cursor\": \"").append(cursor).append("\"}");
      return response.toString();
    }

    public String dateToString(Date d) {      
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      return new StringBuilder()
          .append(c.get(Calendar.MONTH) + 1)
          .append('/')
          .append(c.get(Calendar.DAY_OF_MONTH))
          .append('/')
          .append(c.get(Calendar.YEAR))
          .toString();
    }
        
    public Map<String, Phrase> getPhrases(String lang) {
      List<Phrase> laPhrases = Phrase.GetPhrasesForLanguage(lang);
      Map<String, Phrase> phrases = new HashMap<String, Phrase>();
      for (Phrase phrase : laPhrases) {
        if (phrase.getPhrase().length() > 0) {
          phrases.put(phrase.getKey(), phrase);
        }
      }
      
      if (lang != "de") {
        for (Phrase phrase : Phrase.GetPhrasesForLanguage("de")) {
          if (!phrases.containsKey(phrase.getKey())) {
            phrases.put(phrase.getKey(), phrase);
          }
        }
      }
      
      return phrases;
    }
}