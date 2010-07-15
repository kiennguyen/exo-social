/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.exoplatform.social.webui.profile.UIDisplayProfileList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;


/**
 * Manages information about all existing users. Manages actions
 * such as request make connections, invoke request, accept or deny invitation
 * and delete connection.
 *
 * Author : hanhvq@gmail.com
 * Oct 24, 2009
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UIPeoplePortlet.gtmpl"
)
public class UIPeoplePortlet extends UIPortletApplication {

  /**
   * Default Constructor.<br>
   *
   * @throws Exception
   */
  public UIPeoplePortlet() throws Exception {
    addChild(UIDisplayProfileList.class, null, null);
  }

}