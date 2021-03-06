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
package org.exoplatform.social.portlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.common.UserListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.profile.UIProfileUserSearch;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * {@link UIMembersPortlet} used as a portlet displaying space members.<br />
 *
 * Created by The eXo Platform SARL
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Nov 07, 200
 */

@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/social/portlet/UIMembersPortlet.gtmpl",
    events = {
      @EventConfig(
         listeners=UIMembersPortlet.SearchActionListener.class, phase=Phase.DECODE)
    }
  )
})
public class UIMembersPortlet extends UIPortletApplication {
  private final UIPageIterator iterator_;
  private List<User> memberList;
  private List<User> leaderList;
  private final UIPageIterator iteratorLeaders;
  private final UIPageIterator iteratorMembers;
  private final String iteratorLeaderID = "UIIteratorLeader";
  private final String iteratorMemberID = "UIIteratorMember";
  private final Integer ITEMS_PER_PAGE = 5;
  private static final String SPACE_MEMBER = "member_of_space";
  private IdentityManager identityManager_ = null;
  private UIProfileUserSearch uiSearchMemberOfSpace = null;
  private List<Identity> identityList;
  
  private static final int FIRST_PAGE = 1;

  /**
   * gets identity list
   * @return identity list
   */
  public List<Identity> getIdentityList() { return identityList; }

  /**
   * set identity list
   * @param identityList
   */
  public void setIdentityList(List<Identity> identityList) { this.identityList = identityList; }

  /**
   * constructor
   * @throws Exception
   */
  public UIMembersPortlet() throws Exception {
    initMember();
    initLeader();
    iterator_ = createUIComponent(UIPageIterator.class, null, null);
    addChild(iterator_);
    iteratorLeaders = createUIComponent(UIPageIterator.class, null, iteratorLeaderID);
    iteratorMembers = createUIComponent(UIPageIterator.class, null, iteratorMemberID);
    addChild(iteratorLeaders);
    addChild(iteratorMembers);
    uiSearchMemberOfSpace = createUIComponent(UIProfileUserSearch.class, null, "UIProfileUserSearch");
    uiSearchMemberOfSpace.setTypeOfRelation(SPACE_MEMBER);
    uiSearchMemberOfSpace.setSpaceURL(getSpace().getUrl());
    addChild(uiSearchMemberOfSpace);
  }
  /**
   * sets member list
   * @param memberList
   */
  public void setMemberList(List<User> memberList) { this.memberList = memberList;}

  /**
   * sets leader list
   * @param leaderList
   */
  public void setLeaderList(List<User> leaderList) { this.leaderList = leaderList;}

  /**
   * gets leader list
   * @return leader list
   * @throws Exception
   */
  public List<User> getLeaderList() throws Exception {
    initLeader();
    return leaderList;
  }

  /**
   * gets leader page iterator
   * @return leader page iterator
   */
  public UIPageIterator getIteratorLeaders() { return iteratorLeaders; }

  /**
   * gets member page iterator
   * @return member page iterator
   */
  public UIPageIterator getIteratorMembers() { return iteratorMembers; }

  /**
   * gets leader list
   * @return leader list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<User> getLeaders() throws Exception {
    initLeader();
    int currentPage = iteratorLeaders.getCurrentPage();
    LazyPageList<User> pageList = new LazyPageList<User>(new UserListAccess(leaderList), ITEMS_PER_PAGE);
    iteratorLeaders.setPageList(pageList);
    int pageCount = iteratorLeaders.getAvailablePage();
    if (pageCount >= currentPage) {
      iteratorLeaders.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      iteratorLeaders.setCurrentPage(currentPage - 1);
    }

    return iteratorLeaders.getCurrentPageData();
  }

  /**
   * gets member list
   * @return member list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<User> getMembers() throws Exception {
    initMember();
    int currentPage = iteratorMembers.getCurrentPage();
    LazyPageList<User> pageList = new LazyPageList<User>(new UserListAccess(memberList), ITEMS_PER_PAGE);
    iteratorMembers.setPageList(pageList);
    if (this.uiSearchMemberOfSpace.isNewSearch()) {
      iteratorMembers.setCurrentPage(FIRST_PAGE);
    } else {
      iteratorMembers.setCurrentPage(currentPage);
    }
    this.uiSearchMemberOfSpace.setNewSearch(false);
    return iteratorMembers.getCurrentPageData();
  }

  /**
   * gets user avatar url
   * @param userId
   * @return user avatar url
   * @throws Exception
   */
  public String getUserAvatar(String userId) throws Exception {
    Identity identity = getIdentityManager().getOrCreateIdentity("organization", userId);
    return identity.getProfile().getAvatarUrl();
  }

  /**
   * gets identity by userId
   * @param userId
   * @return user identity
   * @throws Exception
   */
  public Identity getIdentity(String userId) throws Exception {
    Identity identity = getIdentityManager().getOrCreateIdentity("organization", userId);
    if (identity != null) {
      return identity;
    }
    return null;
  }

  /**
   * initialize members, called from {@link #getMembers()}
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void initMember() throws Exception {
    Space space = getSpace();
    memberList = new ArrayList<User>();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    UserHandler userHandler = orgSrc.getUserHandler();
    List<String> userNames = spaceService.getMembers(space);
    Iterator<String> itr = userNames.iterator();
    while(itr.hasNext()) {
      String userName = itr.next();
      if(spaceService.isLeader(space, userName)){
        itr.remove();
      }
    }

    List<Identity> matchIdentities = getIdentityList();
    if (matchIdentities != null) {
      List<String> searchResultUserNames = new ArrayList<String>();
      String userName = null;
      for (Identity id : matchIdentities) {
        userName = id.getRemoteId();
        if (userNames.contains(userName)) searchResultUserNames.add(userName);
      }
      userNames = searchResultUserNames;
    }

    for (String name : userNames) {
      memberList.add(userHandler.findUserByName(name));
    }

  }

  /**
   * initialize leaders, called from {@link #getLeaderList()}
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void initLeader() throws Exception {
    Space space = getSpace();
    leaderList = new ArrayList<User>();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    UserHandler userHandler = orgSrc.getUserHandler();
    List<String> userNames = spaceService.getMembers(space);
    Iterator<String> itr = userNames.iterator();
    while(itr.hasNext()) {
      String userName = itr.next();
      if(!spaceService.isLeader(space, userName)){
        itr.remove();
      }
    }
    for (String name : userNames) {
      leaderList.add(userHandler.findUserByName(name));
    }

  }

  /**
   * This method is called by template file
   * @throws Exception
   */
  private void update() throws Exception {
    //int n = iterator_.getCurrentPage();
    //TODO dang.tung 3.0
    //PageList pageList = new ObjectPageList(memberList, ITEMS_PER_PAGE);
    //iterator_.setPageList(pageList);
    //if (n <= pageList.getAvailablePage()) iterator_.setCurrentPage(n);
    //TODO dang.tung 3.0
  }


  /**
   * get uiPageIterator
   * @return uiPageIterator
   * @throws Exception
   */
  public UIPageIterator getUIPageIterator() throws Exception {
    return iterator_;
  }

  /**
   * gets space, space identified by the url.
   * @return space
   * @throws SpaceException
   */
  public Space getSpace() throws SpaceException {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    return spaceService.getSpaceByUrl(spaceUrl);
  }

  /**
   * gets user list in a space
   * @return user list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<User> getUsersInSpace() throws Exception{
    update();
    return iterator_.getCurrentPageData();
  }

  /**
   * gets current path
   * @return current path
   */
  public String getPath() {
    String nodePath = Util.getPortalRequestContext().getNodePath();
    String uriPath = Util.getPortalRequestContext().getRequestURI();
    return uriPath.replaceAll(nodePath, "");
  }

  /**
   * gets memberships of a user in a space.
   * @param userName
   * @return string of membership name
   * @throws Exception
   */
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

  /**
   * Get the userList from UISpaceUserSearch
   * and then update UIMembersPortlet
   */
//  static public class SearchActionListener extends EventListener<UIMembersPortlet> {
//    @Override
//    public void execute(Event<UIMembersPortlet> event) throws Exception {
//      UIMembersPortlet uiMembersPortlet = event.getSource();
//      UISpaceUserSearch uiUserSearch = uiMembersPortlet.getChild(UISpaceUserSearch.class);
//      uiMembersPortlet.setMemberList(uiUserSearch.getUserList());
//      /////// Check .....
//      uiMembersPortlet.setLeaderList(uiUserSearch.getUserList());
//    }
//
//  }

  /**
   * triggers this action when user clicks on search button
   */
  public static class SearchActionListener extends EventListener<UIMembersPortlet> {
    @Override
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      UIMembersPortlet uiMembersPortlet = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiMembersPortlet.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getIdentityList();
      uiMembersPortlet.setIdentityList(identityList);
    }
  }

  /**
   * get identityManager
   * @return identityManager
   * @see IdentityManager
   */
  private IdentityManager getIdentityManager() {
    if(identityManager_ == null) {
      PortalContainer pcontainer =  PortalContainer.getInstance();
      identityManager_ = (IdentityManager) pcontainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager_;
  }

  /**
   * gets current repository name
   * @return current repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }
}