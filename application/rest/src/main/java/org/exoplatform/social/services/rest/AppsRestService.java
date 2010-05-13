/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.social.services.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;


/**
 * AppsRestService.java
 * <br/>
 * Provides rest services for application registry gadget: shows application list.
 * <br />
 * GET: /{restContextName}/social/apps/show.{format}
 * <br />
 * ex: GET: http://localhost:8080/rest/social/apps/show.json
 *
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Jan 6, 2010
 * @copyright  eXo Platform SAS 
 */
@Path("social/apps")
public class AppsRestService implements ResourceContainer {
  
  private ApplicationRegistryService _applicationRegistryService;
  /**
   * constructor
   */
  public AppsRestService() {}

  /**
   * shows appList
   * @return appList application list entity
   * @see AppList
   */
  private AppList showApps() {
    AppList appList = new AppList();
    ApplicationRegistryService applicationRegistryService = getApplicationRegistryService();
    //String[] applicationTypes = {org.exoplatform.web.application.Application.EXO_PORTLET_TYPE};
    try {
      List<ApplicationCategory> applicationCategoryList = applicationRegistryService.getApplicationCategories("root");
      Iterator<ApplicationCategory> applicationCategoryItr = applicationCategoryList.iterator();
      ApplicationCategory applicationCategory;
      while (applicationCategoryItr.hasNext()) {
        applicationCategory = applicationCategoryItr.next();
        ApplicationType<org.exoplatform.portal.pom.spi.portlet.Portlet> portletType = ApplicationType.PORTLET;
        List<Application> applications = applicationRegistryService.getApplications(applicationCategory, portletType);
        Iterator<Application> applicationItr = applications.iterator();
        Application application;
        while (applicationItr.hasNext()) {
          App app = new App();
          application = applicationItr.next();
          app.setAppId(application.getId());
          app.setAppName(application.getDisplayName());
          appList.addApp(app);
        }
      }
    } catch (Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return appList;
  }
  
  /**
   * shows apps by json/xml format: <br />
   * GET: /{restContextName}/social/apps/show.{format}
   * @param uriInfo provided as {@link Context}
   * @param format can be json or xml
   * @return response
   * @throws Exception
   */
  @GET
  @Path("show.{format}")
  public Response showApps(@Context UriInfo uriInfo, @PathParam("format") String format) throws Exception {
    //TODO hoatle gets currentUser for filter
    MediaType mediaType = Util.getMediaType(format);
    AppList appList = showApps();
    return Util.getResponse(appList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Describes an Application entity from application registry service of portal.<br />
   * This class is needed as a model for converter of the rest service.
   */
  static public class App {
    private String _appId;
    private String _appName;
    /**
     * sets appId
     * @param appId application id
     */
    public void setAppId(String appId) { _appId = appId; }
    /**
     * gets appId
     * @return appId application id
     */
    public String getAppId() { return _appId; }
    /**
     * sets application name
     * @param appName application name
     */
    public void setAppName(String appName) { _appName = appName; }
    /**
     * gets application name
     * @return application name
     */
    public String getAppName() {return _appName ;}
  }
  
  /**
   * List that contains applications from application registry service of portal<br>
   * This class is needed as a model for converter of the rest service.
   */
  @XmlRootElement
  static public class AppList {
    private List<App> _apps;
    /**
     * sets application list
     * @param apps application list
     */
    public void setApps(List<App> apps) { _apps = apps; }
    /**
     * gets application list
     * @return application list
     */
    public List<App> getApps() { return _apps; }
    /**
     * adds application to application list
     * @param app application
     * @see App
     */
    public void addApp(App app) {
      if (_apps == null) {
        _apps = new ArrayList<App>();
      }
      _apps.add(app);
    }
  }
  
  /**
   * gets applicationRegistryService
   * @return applicationRegistryService
   * @see ApplicationRegistryService
   */
  private ApplicationRegistryService getApplicationRegistryService() {
    if (_applicationRegistryService == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      _applicationRegistryService = (ApplicationRegistryService) portalContainer.getComponentInstanceOfType(ApplicationRegistryService.class);
    }
    return _applicationRegistryService;
  }
}
