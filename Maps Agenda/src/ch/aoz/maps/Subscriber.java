package ch.aoz.maps;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.HashMap;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;

/**
 * TODO: Insert description here. (generated by tobulogic)
 */
public class Subscriber {
  public static final String entityKind = "Subscriber";
  
  private final String email;
  public static final String emailProperty = "email";

  private String language;
  public static final String languageProperty = "language";

  private final String hash;
  public static final String hashProperty = "hash";

  private final boolean ok;
  
  public Subscriber(String email, String language) {
    this.email = email;
    this.language = language;
    this.hash = constructHash();
    this.ok = (email != null && this.hash != null && language != null);
  }
  
  public String constructHash() {
	  // The hash consists of sha1(email + language + 128-bit random nonce)
	  MessageDigest sha1;
	  try {
		sha1 = MessageDigest.getInstance("SHA");
	  } catch (NoSuchAlgorithmException e) {
		return null;	// Signals to the caller that things are not all right
	  }
	  SecureRandom random = new SecureRandom();
	  
	  byte randomNonce[] = new byte[16];
	  random.nextBytes(randomNonce);
	  
	  String string_to_hash = this.email + this.language + new String(randomNonce); 
	  sha1.update(string_to_hash.getBytes());
	  // Convert the resulting digest byte array to a string. Yes, the following solution
	  // is ridiculously slow, but it's short & readable, and perf doesn't matter here.
	  StringBuilder sb = new StringBuilder();
	  for(byte int8 : sha1.digest())
	      sb.append(String.format("%02x", int8 & 0xff));
	  return sb.toString();
  }
  
  public Subscriber(Entity entity) {
    boolean ok = true;
    if (entity.hasProperty(emailProperty)) {
      email = (String)entity.getProperty(emailProperty);
    } else {
      email = "";
      ok = false;
    }
    if (entity.hasProperty(languageProperty)) {
      language = (String)entity.getProperty(languageProperty);
    } else {
      language = "";
      ok = false;
    }
    if (entity.hasProperty(hashProperty)) {
      hash = (String)entity.getProperty(hashProperty);
    } else {
      hash = "";
      ok = false;
    }
    this.ok = ok;
  }

  public static HashMap<String, Subscriber> getAllSubscribers() {
    HashMap<String, Subscriber> subscribers = new HashMap<String, Subscriber>();
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(entityKind);
    List<Entity> entities = 
        datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    for (Entity e : entities) {
      Subscriber t = new Subscriber(e);
      subscribers.put(t.getEmail(), t);
    }
    return subscribers;
  }
  
  public static Subscriber getSubscriberByHash(String hash) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(entityKind);
    // I don't know how to query by hash. Given the number of subscribers, this should be 
    // fine. We're not expecting millions of queries.
    List<Entity> entities = 
        datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    for (Entity e : entities) {
      if(hash.equals(e.getProperty(hashProperty))) {
    	  return new Subscriber(e);
      }
    }
    return null;
  }
  
  public static boolean DeleteSubscriber(Subscriber t) {
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 
	  try {
	      datastore.delete(KeyFactory.createKey(entityKind, t.getEmail()));
	  } catch (Exception ex) {
	      return false;
	  }
	  return true;  
  }
  
  public static boolean AddSubscriber(Subscriber t) {
    if (!t.isOk())
      return false;
    Entity e = new Entity(entityKind, t.getEmail());
    e.setProperty(emailProperty, t.getEmail());    
    e.setProperty(languageProperty, t.getLanguage());
    e.setProperty(hashProperty, t.getHash());
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.put(e);
    } catch (Exception ex) {
      return false;
    }
    return true;
  }
  
  public static boolean exists(String email) {
    Filter f = new FilterPredicate(
        Entity.KEY_RESERVED_PROPERTY, 
        FilterOperator.EQUAL,
        KeyFactory.createKey(entityKind, email));
    
    Query q = new Query(entityKind);
    q.setFilter(f);
    q.setKeysOnly();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity e = datastore.prepare(q).asSingleEntity();
      return e != null;
    } catch (Exception ex) {
      return false;
    }
  }

  public static Translator GetByEmail(String key) {
    DatastoreService datastore = DatastoreServiceFactory
            .getDatastoreService();

    Entity item;
    try {
      item = datastore.get(KeyFactory.createKey(entityKind, key));
    } catch (EntityNotFoundException e) {
      return null;
    }  
    return new Translator(item);
  }
  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return the name
   */
  public String getHash() {
    return hash;
  }

  /**
   * @return the languages
   */
  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
  
  /**
   * @return the ok
   */
  public boolean isOk() {
    return ok;
  }
}
