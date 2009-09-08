/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.relation;

import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormPageIterator;

/**
 * Created by The eXo Platform SAS
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIPendingRelation.gtmpl",
    events = { 
        @EventConfig(listeners = UIPendingRelation.DenyContactActionListener.class)
      }
)
public class UIPendingRelation extends UIForm {
  /** UIFormPageIterator */
  UIFormPageIterator uiFormPageIterator_;
  /** UIFormPageIterator ID. */
  private final String iteratorID_ = "UIFormPageIteratorPendingRelation";
  /** Current identity. */
  Identity            currIdentity = null;
  /** RelationshipManager */
  RelationshipManager rm           = null;
  /** IdentityManager */
  IdentityManager     im           = null;
  
  /**
   * Get UIFormPageIterator.
   * @return
   */
  public UIFormPageIterator getUiFormPageIterator() {
    return uiFormPageIterator_;
  }

  /**
   * Constructor.
   * @throws Exception 
   */
  public UIPendingRelation() throws Exception {
    uiFormPageIterator_ = createUIComponent(UIFormPageIterator.class, null, iteratorID_);
    addChild(uiFormPageIterator_);
  }
  
  /**
   * Get list of relationship that has status is PENDING.
   * 
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Relationship> getPendingRelationList() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentIdentity();
    List<Relationship> listRelationShip = relm.getPending(currentIdentity, true);
    int currentPage = uiFormPageIterator_.getCurrentPage();
    LazyPageList<Relationship> pageList = new LazyPageList<Relationship>(new RelationshipListAccess(listRelationShip), 5);
    uiFormPageIterator_.setPageList(pageList) ;  
    int pageCount = uiFormPageIterator_.getAvailablePage();
    if(pageCount >= currentPage){
      uiFormPageIterator_.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      uiFormPageIterator_.setCurrentPage(currentPage-1);
    }
    List<Relationship> lists;
    lists = uiFormPageIterator_.getCurrentPageData();
    
    return lists;
  }
    
  public String getCurrentUriObj() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
    String portalUrl = pcontext.getPortalURI();
    String uriObj = requestUrl.replace(portalUrl, "");
    if (uriObj.contains("/"))
      uriObj = uriObj.split("/")[0];
    return uriObj;
  }
  
  /**
   * Get current identity.
   * 
   * @return
   * @throws Exception
   */
  public Identity getCurrentIdentity() throws Exception {
    if (currIdentity == null) {
      IdentityManager im = getIdentityManager();
      currIdentity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    }
    return currIdentity;
  }
  
  /**
   * Get current user name.
   * 
   * @return
   */
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  /**
   *  Revoke the request with relation that has status is PENDING. 
   */
  public static class DenyContactActionListener extends EventListener<UIPendingRelation> {
    public void execute(Event<UIPendingRelation> event) throws Exception {
      UIPendingRelation portlet = event.getSource();
  
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();
  
      IdentityManager im = portlet.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);
  
      Identity requestedIdentity = im.getIdentityById(userId);
  
      RelationshipManager rm = portlet.getRelationshipManager();
  
      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);
      if (rel != null)
        rm.remove(rel);
    }
  }
  
  /**
   * Get Relationship manager.
   * 
   * @return
   */
  private RelationshipManager getRelationshipManager() {
    if (rm == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return rm;
  }
  
  /**
   * Get identity manager.
   * 
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return im;
  }
}