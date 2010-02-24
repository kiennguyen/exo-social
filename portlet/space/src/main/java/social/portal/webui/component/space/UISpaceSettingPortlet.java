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

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

import social.portal.webui.component.UISpaceSetting;

/**
 * {@link UISpaceSettingPortlet} used as a portlet containing {@link UISpaceSetting}.
 * Created by The eXo Platform SARL
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Jan 6, 2009
 */

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/portal/webui/space/UISpaceSettingPortlet.gtmpl"
)

public class UISpaceSettingPortlet extends UIPortletApplication {
  
  final private UISpaceSetting uiSpaceSetting;
  final private SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceSettingPortlet() throws Exception {
    uiSpaceSetting = createUIComponent(UISpaceSetting.class, null, null);
    addChild(uiSpaceSetting);
  }
  /**
   * data initialization; set space by spaceUrl to work with
   * @throws Exception
   */
  public void initData() throws Exception {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    Space space  = spaceSrc.getSpaceByUrl(spaceUrl);
    uiSpaceSetting.setValues(space);
  }
  
  /**
   * render popop message, this method is called from template file.
   * @throws Exception
   */
  public void renderPopupMessages() throws Exception {
    UIPopupMessages uiPopupMsg = getUIPopupMessages();
    if(uiPopupMsg == null)  return ;
    WebuiRequestContext  context =  WebuiRequestContext.getCurrentInstance() ;
    uiPopupMsg.processRender(context);
  }
}