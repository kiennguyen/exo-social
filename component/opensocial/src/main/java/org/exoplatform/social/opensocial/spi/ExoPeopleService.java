package org.exoplatform.social.opensocial.spi;

import org.apache.shindig.social.opensocial.spi.*;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.ListField;
import org.apache.shindig.social.opensocial.model.Url;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.ResponseError;
import org.apache.shindig.social.core.model.PersonImpl;
import org.apache.shindig.social.core.model.ListFieldImpl;
import org.apache.shindig.social.core.model.UrlImpl;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.auth.SecurityToken;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.portal.application.UserGadgetStorage;
import org.exoplatform.portal.webui.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.concurrent.Future;
import java.util.*;
import java.net.URLEncoder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ExoPeopleService extends ExoService implements PersonService, AppDataService {

  private static final Comparator<Person> NAME_COMPARATOR = new Comparator<Person>() {
    public int compare(Person person, Person person1) {
      String name = person.getName().getFormatted();
      String name1 = person1.getName().getFormatted();
      return name.compareTo(name1);
    }
  };

    
  public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId, CollectionOptions collectionOptions, Set<String> fields, SecurityToken token) throws SocialSpiException {
    List<Person> result = Lists.newArrayList();
    try {

      Set<Identity> idSet = getIdSet(userIds, groupId, token);

      Iterator<Identity> it = idSet.iterator();

        while(it.hasNext()) {
            Identity id = it.next();

            if(id != null) {
              result.add(convertToPerson(id, fields));
            }
        }


      // We can pretend that by default the people are in top friends order
      if (collectionOptions.getSortBy().equals(Person.Field.NAME.toString())) {
        Collections.sort(result, NAME_COMPARATOR);
      }

      if (collectionOptions.getSortOrder().equals(SortOrder.descending)) {
        Collections.reverse(result);
      }

      // TODO: The eXocontainer doesn't  have the concept of HAS_APP so
      // we can't support any filters yet. We should fix this.

      int totalSize = result.size();
      int last = collectionOptions.getFirst() + collectionOptions.getMax();
      result = result.subList(collectionOptions.getFirst(), Math.min(last, totalSize));

      return ImmediateFuture.newInstance(new RestfulCollection<Person>(
          result, collectionOptions.getFirst(), totalSize));
    } catch (Exception je) {
      throw new SocialSpiException(ResponseError.INTERNAL_ERROR, je.getMessage(), je);
    }
  }



  public Future<Person> getPerson(UserId id, Set<String> fields, SecurityToken token) throws SocialSpiException {
    try {

        Identity identity = getIdentity(id.getUserId(token));


        return ImmediateFuture.newInstance(convertToPerson(identity, fields));
    } catch (Exception e) {
      throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);    }
  }

  private Person convertToPerson(Identity identity, Set<String> fields) {
    Person p = new PersonImpl();
    Profile pro = identity.getProfile();

    for (String field : fields) {
      if(Person.Field.DISPLAY_NAME.toString().equals(field)) {
        p.setDisplayName(pro.getFullName());  
      }
      else if(Person.Field.EMAILS.toString().equals(field)) {
        p.setEmails(convertToListFields((List<Map>) pro.getProperty("emails")));  
      }
      else if(Person.Field.URLS.toString().equals(field)) {
        p.setUrls(convertToURLListFields((List<Map>) pro.getProperty("urls")));  
      }
      else if(Person.Field.IMS.toString().equals(field)) {
        p.setIms(convertToListFields((List<Map>) pro.getProperty("ims")));  
      }
      else if(Person.Field.ID.toString().equals(field)) {
        p.setId(identity.getId());  
      }
      else if(Person.Field.NAME.toString().equals(field)) {
        NameImpl name = new NameImpl();
        name.setFamilyName((String) pro.getProperty("lastName"));
        name.setGivenName((String) pro.getProperty("firstName"));
        name.setFormatted(name.getGivenName() + " " + name.getFamilyName());
        p.setName(name);
      }
      else if(Person.Field.PROFILE_URL.toString().equals(field)) {
        //todo use a url manager to manage this
        p.setProfileUrl("/portal/private/classic/people/" + identity.getRemoteId());  
      }
      else if(Person.Field.GENDER.toString().equals(field)) {
        String gender = (String) pro.getProperty("gender");
        if (gender != null && gender.equals("female"))
          p.setGender(Person.Gender.female);
        else
          p.setGender(Person.Gender.male);
      }
    }

    return p;
  }

  private List<ListField> convertToListFields(List<Map> fields) {
    List<ListField> l = new ArrayList<ListField>();
    if(fields == null)
      return null;
    for(Map field : fields) {
      l.add(new ListFieldImpl((String)field.get("key"), (String) field.get("value")));
    }
    return l;
  }

  private List<Url> convertToURLListFields(List<Map> fields) {
    List<Url> l = new ArrayList<Url>();
    if(fields == null)
      return null;
    for(Map field : fields) {
      l.add(new UrlImpl((String) field.get("value"), (String)field.get("key"), (String)field.get("key")));
    }
    return l;
  }

  public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws SocialSpiException {
    try {
      Set<Identity> idSet = getIdSet(userIds, groupId, token);
      Map<String, Map<String, String>> idToData = new HashMap<String, Map<String, String>>();
      Iterator<Identity> it = idSet.iterator();

      String gadgetId = clean(appId);  
      String instanceId = "" + token.getModuleId();
      while(it.hasNext()) {
        Identity id = it.next();
        idToData.put(id.getId(), getPreferences(id.getRemoteId(), gadgetId, instanceId, fields));
      }
      return ImmediateFuture.newInstance(new DataCollection(idToData));
    } catch (Exception e) {
      throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
    }
  }

  private String clean(String url) {
    url = URLEncoder.encode(url);
    url = url.replaceAll(":", "");
    return url;
  }

  private Map<String, String> getPreferences(String userID, String gadgetId, String instanceID, Set<String> fields) throws Exception {
      PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
      UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);

      Map<String, String> values = userGadgetStorage.get(userID, gadgetId, instanceID, fields);
      return values;
  }

  private void savePreferences(String userID, String gadgetId, String instanceID, Map<String, String> values) throws Exception {
    PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
    UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);

    userGadgetStorage.save(userID, gadgetId, instanceID, values);
  }

  private void deletePreferences(String userID, String gadgetId, String instanceID, Set<String> keys) throws Exception {
    PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
    UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);

    userGadgetStorage.delete(userID, gadgetId, instanceID, keys);
  }

  public Future<Void> deletePersonData(UserId user, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws SocialSpiException {
    try {
      String userId = user.getUserId(token);

      Identity id = getIdentity(userId);
      String gadgetId = clean(appId);
      String instanceId = "" + token.getModuleId();

      deletePreferences(id.getRemoteId(), gadgetId, instanceId, fields);
    } catch (Exception e) {
      throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
    }
    return ImmediateFuture.newInstance(null);
  }

  public Future<Void> updatePersonData(UserId user, GroupId groupId, String appId, Set<String> fields, Map<String, String> values, SecurityToken token) throws SocialSpiException {
    //TODO: remove the fields that are in the fields list and not in the values map
    try {
      String userId = user.getUserId(token);

      Identity id = getIdentity(userId);
      String gadgetId = clean(appId);
      String instanceId = "" + token.getModuleId();

      savePreferences(id.getRemoteId(), gadgetId, instanceId, values);
    } catch (Exception e) {
      throw new SocialSpiException(ResponseError.INTERNAL_ERROR, e.getMessage(), e);
    }
    return ImmediateFuture.newInstance(null);
  }
}
