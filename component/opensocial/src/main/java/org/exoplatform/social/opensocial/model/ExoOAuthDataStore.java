/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.exoplatform.social.opensocial.model;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;

import org.apache.shindig.auth.AuthenticationMode;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.crypto.Crypto;
import org.apache.shindig.social.core.oauth.OAuthSecurityToken;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.opensocial.service.ExoActivityService;
import org.exoplatform.social.opensocial.service.ExoPeopleService;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Jul 15, 2009
 */

public class ExoOAuthDataStore implements OAuthDataStore {
  // This needs to be long enough that an attacker can't guess it.  If the attacker can guess this
  // value before they exceed the maximum number of attempts, they can complete a session fixation
  // attack against a user.
  private final int CALLBACK_TOKEN_LENGTH = 6;

  // We limit the number of trials before disabling the request token.
  private final int CALLBACK_TOKEN_ATTEMPTS = 5;

  private final ExoPeopleService peopleService;
  private final ExoActivityService activityService;
  private final OAuthServiceProvider SERVICE_PROVIDER;
  private ServiceProviderStore providerStore;

  private static final Log LOG = ExoLogger.getExoLogger(ExoOAuthDataStore.class);

  @Inject
  public ExoOAuthDataStore(ExoPeopleService peopleService, ExoActivityService activityService, @Named("shindig.oauth.base-url") String baseUrl) {
    this.peopleService = peopleService;
    this.activityService = activityService;
    this.SERVICE_PROVIDER = new OAuthServiceProvider(baseUrl + "requestToken", baseUrl + "authorize", baseUrl + "accessToken");
  }

  // All valid OAuth tokens
  private static ConcurrentMap<String,OAuthEntry> oauthEntries = new MapMaker().makeMap();

  // Get the OAuthEntry that corresponds to the oauthToken
  public OAuthEntry getEntry(String oauthToken) {
    Preconditions.checkNotNull(oauthToken);
    return oauthEntries.get(oauthToken);
  }

  public OAuthConsumer getConsumer(String consumerKey) {
    try {

      ServiceProviderData data = getProviderStore().getServiceProvider(consumerKey);
      if (data == null) {
        LOG.warn("No provider was found for consumer key: " + consumerKey);
        return null;
      }

      String consumerSecret = data.getSharedSecret();
      if (consumerSecret == null) {
          return null;
      }

      String callbackUrl = data.getCallbackUrl();

      OAuthConsumer consumer = new OAuthConsumer(callbackUrl, consumerKey, consumerSecret, SERVICE_PROVIDER);

      return consumer;

    } catch (Exception e) {
       return null;
    }
  }

  // Generate a valid requestToken for the given consumerKey
  public OAuthEntry generateRequestToken(String consumerKey, String oauthVersion,
      String signedCallbackUrl) {
    OAuthEntry entry = new OAuthEntry();
    entry.setAppId(consumerKey);
    entry.setConsumerKey(consumerKey);
    entry.setDomain("samplecontainer.com");
    entry.setContainer("default");

    entry.setToken(UUID.randomUUID().toString());
    entry.setTokenSecret(UUID.randomUUID().toString());

    entry.setType(OAuthEntry.Type.REQUEST);
    entry.setIssueTime(new Date());
    entry.setOauthVersion(oauthVersion);
    if (signedCallbackUrl != null) {
      entry.setCallbackUrlSigned(true);
      entry.setCallbackUrl(signedCallbackUrl);
    }

    oauthEntries.put(entry.getToken(), entry);
    return entry;
  }

  // Turns the request token into an access token
  public OAuthEntry convertToAccessToken(OAuthEntry entry) {
    Preconditions.checkNotNull(entry);
    Preconditions.checkState(entry.getType() == OAuthEntry.Type.REQUEST, "Token must be a request token");

    OAuthEntry accessEntry = new OAuthEntry(entry);

    accessEntry.setToken(UUID.randomUUID().toString());
    accessEntry.setTokenSecret(UUID.randomUUID().toString());

    accessEntry.setType(OAuthEntry.Type.ACCESS);
    accessEntry.setIssueTime(new Date());

    oauthEntries.remove(entry.getToken());
    oauthEntries.put(accessEntry.getToken(), accessEntry);

    return accessEntry;
  }

  // Authorize the request token for the given user id
  public void authorizeToken(OAuthEntry entry, String userId) {
    Preconditions.checkNotNull(entry);
    entry.setAuthorized(true);
    entry.setUserId(Preconditions.checkNotNull(userId));
    if (entry.isCallbackUrlSigned()) {
      entry.setCallbackToken(Crypto.getRandomDigits(CALLBACK_TOKEN_LENGTH));
    }
  }

  public void disableToken(OAuthEntry entry) {
    Preconditions.checkNotNull(entry);
    entry.setCallbackTokenAttempts(entry.getCallbackTokenAttempts()+1);
    if (!entry.isCallbackUrlSigned() || entry.getCallbackTokenAttempts() >= CALLBACK_TOKEN_ATTEMPTS) {
      entry.setType(OAuthEntry.Type.DISABLED);
    }

    oauthEntries.put(entry.getToken(), entry);
  }

  public void removeToken(OAuthEntry entry) {
    Preconditions.checkNotNull(entry);

    oauthEntries.remove(entry.getToken());
  }

  // Return the proper security token for a 2 legged oauth request that has been validated
  // for the given consumerKey. App specific checks like making sure the requested user has the
  // app installed should take place in this method
  public SecurityToken getSecurityTokenForConsumerRequest(String consumerKey, String userId) {
    String domain = "samplecontainer.com";
    String container = "default";

    return new OAuthSecurityToken(userId, null, consumerKey, domain, container, null,
        AuthenticationMode.OAUTH_CONSUMER_REQUEST.name());

  }

  public ServiceProviderStore getProviderStore() {
    if (providerStore == null) {
      ExoContainer container = PortalContainer.getInstance();
      providerStore = (ServiceProviderStore) container.getComponentInstanceOfType(ServiceProviderStore.class);
    }
    return providerStore;
  }

  public void setProviderStore(ServiceProviderStore secretProvider) {
    this.providerStore = secretProvider;
  }
}
