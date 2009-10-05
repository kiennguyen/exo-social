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
package social.portal.webui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.social.space.SpaceException.Code;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 12, 2008          
 */

@ComponentConfigs ( {
  @ComponentConfig(
     lifecycle = UIFormLifecycle.class,
     template =  "app:/groovy/portal/webui/uiform/UISpaceMember.gtmpl",
     events = {
       @EventConfig(listeners = UISpaceMember.InviteActionListener.class),
       @EventConfig(listeners = UISpaceMember.SearchUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.RevokeInvitedUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.DeclineUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.ValidateUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.RemoveUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.RemoveLeaderActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.MakeLeaderActionListener.class, phase=Phase.DECODE)
       }
 ),
 @ComponentConfig(
    type = UIPopupWindow.class,
    id = "SearchUser",
    template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
    events = {
      @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
      @EventConfig(listeners = UISpaceMember.CloseActionListener.class, name = "Close", phase = Phase.DECODE)  ,
      @EventConfig(listeners = UISpaceMember.AddActionListener.class, name = "Add", phase = Phase.DECODE)
    }
 )
})



public class UISpaceMember extends UIForm {

  static private final String MSG_ERROR_REMOVE_MEMBER = "UISpaceMember.msg.error_remove_member";
  static private final String MSG_ERROR_REMOVE_LEADER = "UISpaceMember.msg.error_remove_leader";
  
  private String spaceId;
  private SpaceService spaceService = null;
  private final static String user = "user";
  private UIPageIterator iteratorPenddingUsers;
  private UIPageIterator iteratorInvitedUsers;
  private UIPageIterator iteratorExistingUsers;
  private final String iteratorPenddingID = "UIIteratorPendding";
  private final String iteratorInvitedID = "UIIteratorInvited";
  private final String iteratorExistingID = "UIIteratorExisting";
  
  public UISpaceMember() throws Exception {
    addUIFormInput(new UIFormStringInput(user,null,null)
                    .addValidator(MandatoryValidator.class)
                    .addValidator(ExpressionValidator.class, "^\\p{L}[\\p{L}\\d._,]+\\p{L}$", "UISpaceMember.msg.Invalid-char"));
    UIPopupWindow searchUserPopup = addChild(UIPopupWindow.class, "SearchUser", "SearchUser");
    searchUserPopup.setWindowSize(640, 0); 
    iteratorPenddingUsers = createUIComponent(UIPageIterator.class, null, iteratorPenddingID);
    iteratorInvitedUsers = createUIComponent(UIPageIterator.class, null, iteratorInvitedID);
    iteratorExistingUsers = createUIComponent(UIPageIterator.class, null, iteratorExistingID);
    addChild(iteratorPenddingUsers);
    addChild(iteratorInvitedUsers);
    addChild(iteratorExistingUsers);
  }
  
  private SpaceService getSpaceService() {
    if(spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService; 
  }
  
  private UserACL getUserACL() throws Exception {
    return getApplicationComponent(UserACL.class);
  }
  
  private String getRemoteUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }
  
  public UIPageIterator getUIPageIteratorPenddingUsers() { return iteratorPenddingUsers;}
  
  public UIPageIterator getUIPageIteratorInvitedUsers() { return iteratorInvitedUsers;}
  
  public UIPageIterator getUIPageIteratorExistingUsers() { return iteratorExistingUsers;}
  
  public void setValue(String spaceId) throws Exception {
    this.spaceId = spaceId;
  }
  
  @SuppressWarnings("unchecked")
  public List<String> getPenddingUsers() throws Exception {
    List<String> pendingUsersList = new ArrayList<String>();
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);    
    String[] pendingUsers = space.getPendingUsers();
    if(pendingUsers != null) {
      pendingUsersList.addAll(Arrays.asList(pendingUsers));
    }    
    PageList pageList = new ObjectPageList(pendingUsersList,5);
    iteratorPenddingUsers.setPageList(pageList);
    return iteratorPenddingUsers.getCurrentPageData();
  }
    
  @SuppressWarnings("unchecked")
  public List<String> getInvitedUsers() throws Exception {
    List<String> invitedUsersList = new ArrayList<String>(); 
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    String[] invitedUsers = space.getInvitedUsers();    
    if(invitedUsers != null) {
      invitedUsersList.addAll(Arrays.asList(invitedUsers));
    }       
    PageList pageList = new ObjectPageList(invitedUsersList,5);
    iteratorInvitedUsers.setPageList(pageList);
    return iteratorInvitedUsers.getCurrentPageData();
  }
  
  @SuppressWarnings("unchecked")
  public List<String> getExistingUsers() throws Exception {    
    SpaceService spaceService = getSpaceService();
    Space space  = spaceService.getSpaceById(spaceId);
    PageList pageList = new ObjectPageList(spaceService.getMembers(space),5);
    iteratorExistingUsers.setPageList(pageList);
    return iteratorExistingUsers.getCurrentPageData();
  }

  public void setUsersName(String userName) {
    getUIStringInput(user).setValue(userName); 
  }
  
  public String getUsersName() {
    return getUIStringInput(user).getValue(); 
  }
  
  /**
   * Checking whether the remote user is super user
   * @return
   * @throws Exception 
   */
  public boolean isSuperUser() throws Exception {
    return getRemoteUser().equals(getUserACL().getSuperUser());
  }
  
  /**
   * Get spaceUrl
   * @return
   * @throws Exception
   */
  public String getSpaceUrl() throws Exception {
   Space space = getSpaceService().getSpaceById(spaceId);
   return Util.getPortalRequestContext().getPortalURI() + space.getUrl();
  }
  
  public boolean isLeader(String userName) throws Exception {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    return spaceService.isLeader(space, userName);
  }
  
  static public class InviteActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      uiSpaceMember.validateInvitedUser();
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }
  
  private void validateInvitedUser() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    UIApplication uiApp = requestContext.getUIApplication();
    String[] invitedUserList = getUsersName().split(",");      
    String usersNotExist = null;
    String usersIsInvited = null;
    String usersIsMember = null;
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    for(String invitedUser : invitedUserList){
      try {
        spaceService.inviteMember(space, invitedUser);
      } catch (SpaceException e) {
        if(e.getCode() == SpaceException.Code.USER_NOT_EXIST) {
          if(usersNotExist == null) usersNotExist = invitedUser;
          else usersNotExist += "," +invitedUser; 
        } else if (e.getCode() == SpaceException.Code.USER_ALREADY_INVITED) {
          if(usersIsInvited == null) usersIsInvited = invitedUser;
          else usersIsInvited += "," +invitedUser;
        } else if(e.getCode() == SpaceException.Code.USER_ALREADY_MEMBER) {
          if(usersIsMember == null) usersIsMember = invitedUser;
          else usersIsMember += "," +invitedUser;
        }
      }
    }
    String remainUsers = null;
    if(usersNotExist != null){
      remainUsers = usersNotExist;
      uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-not-exist", new String[] {usersNotExist},ApplicationMessage.WARNING));
    }
    if(usersIsInvited != null){
      if(remainUsers == null) remainUsers = usersIsInvited;
      else remainUsers += "," + usersIsInvited;
      uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-is-invited",new String[] {usersIsInvited},ApplicationMessage.WARNING));
    }
    if(usersIsMember != null){
      if(remainUsers == null) remainUsers = usersIsMember;
      else remainUsers += "," + usersIsMember;
      uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-is-member",new String[] {usersIsMember},ApplicationMessage.WARNING));
    }     
    setUsersName(remainUsers);   
  }
  
  static public class SearchUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      UIPopupWindow searchUserPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      UIUserSelector userSelector = uiSpaceMember.createUIComponent(UIUserSelector.class, null, null);
      userSelector.setShowSearchGroup(false);
      searchUserPopup.setUIComponent(userSelector);
      searchUserPopup.setShow(true);
    }
  }
  
  static public class RevokeInvitedUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      spaceService.revokeInvitation(uiSpaceMember.spaceId, userName); 
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class DeclineUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      
      SpaceService spaceService = uiSpaceMember.getSpaceService();
      spaceService.declineRequest(uiSpaceMember.spaceId, userName);      
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class RemoveUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      SpaceService spaceService = uiSpaceMember.getSpaceService();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      try {
        spaceService.removeMember(space, userName);      
      } catch(SpaceException se) {
          uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REMOVE_MEMBER, null, ApplicationMessage.WARNING));
      }
    }
  }
  
  static public class ValidateUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      spaceService.validateRequest(uiSpaceMember.spaceId, userName);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class RemoveLeaderActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      try {
        spaceService.setLeader(space, userName, false);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REMOVE_LEADER, null, ApplicationMessage.WARNING));
      }
    }
  }
  
  static public class MakeLeaderActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      spaceService.setLeader(space, userName, true);
      
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiForm = event.getSource();
      UISpaceMember uiSpaceMember = uiForm.getAncestorOfType(UISpaceMember.class);
      uiSpaceMember.setUsersName(uiForm.getSelectedUsers());
      UIPopupWindow uiPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      uiSpaceMember.validateInvitedUser();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {            
      UIUserSelector uiForm = event.getSource();
      UISpaceMember uiSpaceMember = uiForm.getAncestorOfType(UISpaceMember.class);
      UIPopupWindow uiPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
}
