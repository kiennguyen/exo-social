/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.opensocial.auth;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Zun
 *          exo@exoplatform.com
 * Jul 30, 2010
 */
public class RestPortalContainerNameConfig extends BaseComponentPlugin {
  private static Log LOG = ExoLogger.getExoLogger(RestPortalContainerNameConfig.class);

  String containerName;

  public RestPortalContainerNameConfig(InitParams params) {
    try {
      this.containerName = params.getValueParam("rest-container-name").toString();
    } catch (Exception e) {
      LOG.error("Cannot init container name for REST service : ", e);      
    }
  }

  public void setRestContainerName(RestPortalContainerNameConfig containerNameConfig){
    containerName = containerNameConfig.getContainerName();
  }

  public String getContainerName() {
    return containerName;
  }
}