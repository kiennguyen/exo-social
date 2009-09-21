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
package social.portal.webui.component.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Nov 07, 2008          
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/groovy/portal/webui/space/UIUserListPortlet.gtmpl"
)
public class UIUserListPortlet extends UIPortletApplication {
  
  private UIPageIterator iterator_;
  private IdentityManager identityManager_ = null;
  
  public UIUserListPortlet() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, null);
    addChild(iterator_);
    init();
  }
  
  public String getUserAvatar(String userId) throws Exception {
    Identity identity = getIdentityManager().getIdentityByRemoteId("organization", userId);
    Profile profile = identity.getProfile();
    ProfileAttachment attach = (ProfileAttachment) profile.getProperty("avatar");
    if (attach != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + attach.getWorkspace()
              + attach.getDataPath() + "?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public void init() throws Exception {
    int n = iterator_.getCurrentPage();
    Space space = getSpace();
    List<User> users = new ArrayList<User>();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    UserHandler userHandler = orgSrc.getUserHandler();
    List<String> userNames = spaceService.getMembers(space);
    for (String name : userNames) {
      users.add(userHandler.findUserByName(name));
    }
    PageList pageList = new ObjectPageList(users,3);
    iterator_.setPageList(pageList);
    if (n <= pageList.getAvailablePage()) iterator_.setCurrentPage(n);
  }

  public UIPageIterator getUIPageIterator() throws Exception { 
    return iterator_;
    }
  
  private Space getSpace() throws SpaceException {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    return spaceService.getSpaceByUrl(spaceUrl);
  }
  
  @SuppressWarnings("unchecked")
  public List<User> getUsersInSpace() throws Exception{
    init();
    return iterator_.getCurrentPageData();
  }
  
  @SuppressWarnings("unchecked")
  public String getMemberships(String userName) throws Exception {
    String memberShip = null;
    OrganizationService orgService = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgService.getMembershipHandler();
    Collection<Membership> memberShips= memberShipHandler.findMembershipsByUserAndGroup(userName, getSpace().getGroupId());
    for(Membership aaa : memberShips) {
      if(memberShip == null) memberShip = aaa.getMembershipType();
      else memberShip += "," + aaa.getMembershipType();
    }
    return memberShip;
  }
  
  private IdentityManager getIdentityManager() {
    if(identityManager_ == null) {
      PortalContainer pcontainer =  PortalContainer.getInstance();
      identityManager_ = (IdentityManager) pcontainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager_;
  }
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);    
    return rService.getCurrentRepository().getConfiguration().getName();
  }
}
