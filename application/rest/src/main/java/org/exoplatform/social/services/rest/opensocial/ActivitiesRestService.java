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
package org.exoplatform.social.services.rest.opensocial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.services.rest.Util;

/**
 * ActivitiesRestService.java <br />
 * 
 * Provides rest services for activity gadget: like/unlike; comment; delete activity. <br />
 * apis: <br />
 * GET:  /restContextName/social/activities/{activityId}/likes/show.{format} <br />
 * POST: /restContextName/social/activities/{activityId}/likes/update.{format} <br />
 * POST: /restContextName/social/activities/{activityId}/likes/destroy/{identity}.{format} <br />
 * ... <br />
 * See methods for more api details.
 * 
 *
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Dec 29, 2009
 * @copyright  eXo Platform SEA
 */
@Path("social/activities")
public class ActivitiesRestService implements ResourceContainer {
  private ActivityManager _activityManager;
  private IdentityManager _identityManager;
  
  /**
   * constructor
   */
  public ActivitiesRestService() {}
  
  
  /**
   * destroys activity by activityId
   * if detects any comments of that activity, destroys these comments, too.
   * @param activityId
   * @return activity
   */
  private Activity destroyActivity(String activityId) {
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    String rawCommentIds = activity.getExternalId();
    //rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds != null) {
      String[] commentIds = rawCommentIds.split(",");
      //remove the first empty element
      commentIds = removeItemFromArray(commentIds, "");
      for (String commentId : commentIds) {
        try {
          _activityManager.deleteActivity(commentId);
        } catch(Exception ex) {
          //TODO hoatle LOG
          //TODO hoatle handles or ignores?
        }
      }
    }
    try {
      _activityManager.deleteActivity(activityId);
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return activity;
  }
  
  /**
   * destroys activity and gets json/xml return format
   * @param uriInfo
   * @param activityId
   * @param format 
   * @return response
   * @throws Exception
   */
  @POST
  @Path("destroy/{activityId}.{format}")
  public Response destroyActivity(@Context UriInfo uriInfo,
                                  @PathParam("activityId") String activityId,
                                  @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    Activity activity = destroyActivity(activityId);
    return Util.getResponse(activity, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * show list of like by activityId
   * @param activityId  
   * @return
   * @throws Exception
   */
  private LikeList showLikes(String activityId) {
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String[] identityIds = activity.getLikeIdentityIds();
    if (identityIds == null) {
      likeList.setLikes(new ArrayList<Like>());
    } else {
      likeList.setLikes(getLikes(identityIds));
    }
    return likeList;
  }
  
  /**
   * updates like of an activity
   * @param activityId
   * @param like
   * @throws Exception
   */
  private LikeList updateLike(String activityId, Like like) throws Exception {
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String[] identityIds = activity.getLikeIdentityIds();
    String identityId = like.getIdentityId();
    boolean alreadyLiked = false;
    if (identityId == null) {
      throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }
    if (identityIds != null) {
      for (String id : identityIds) {
        if (id.equals(identityId)) {
          alreadyLiked = true;
        }
      }
    }
    if (!alreadyLiked) {
      identityIds = addItemToArray(identityIds, identityId);
      activity.setLikeIdentityIds(identityIds);
      try {
        _activityManager.saveActivity(activity);
      } catch (Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      //TODO hoatle let it run smoothly or informs that user already liked the activity?
    }
    likeList.setLikes(getLikes(identityIds));
    return likeList;
  }
  
  /**
   * destroys like from an activity 
   * @param activityId
   * @param identityId
   */
  private LikeList destroyLike(String activityId, String identityId) {
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String[] identityIds = activity.getLikeIdentityIds();
    if (identityIds == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    boolean alreadyLiked = true;
    for (String id : identityIds) {
      if (id.equals(identityId)) {
        identityIds = removeItemFromArray(identityIds, identityId);
        activity.setLikeIdentityIds(identityIds);
        try {
          _activityManager.saveActivity(activity);
        } catch(Exception ex) {
          throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        alreadyLiked = false;
      }
    }
    if (alreadyLiked) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    if (identityIds == null) {
      likeList.setLikes(new ArrayList<Like>());
    } else {
      likeList.setLikes(getLikes(identityIds));
    }
    return likeList;
  }
  
  /**
   * shows list of like by activityId and returns json/xml format
   * @param uriInfo
   * @param activityId
   * @param format 
   * @return response
   * @throws Exception
   */
  @GET
  @Path("{activityId}/likes/show.{format}")
  public Response showLikes(@Context UriInfo uriInfo,
                            @PathParam("activityId") String activityId,
                            @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    LikeList likeList = null;
    likeList = showLikes(activityId);
    return Util.getResponse(likeList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * updates like by json/xml format
   * @param uriInfo
   * @param activityId
   * @param format 
   * @param like
   * @return response
   * @throws Exception 
   */
  @POST
  @Path("{activityId}/likes/update.{format}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response updateLike(@Context UriInfo uriInfo,
                             @PathParam("activityId") String activityId,
                             @PathParam("format") String format,
                             Like like) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    LikeList likeList = null;
    likeList = updateLike(activityId, like);
    return Util.getResponse(likeList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * destroys like by identityId and gets json/xml return format
   * @param uriInfo
   * @param activityId
   * @param identityId
   * @param format 
   * @return response
   * @throws Exception
   */
  @POST
  @Path("{activityId}/likes/destroy/{identityId}.{format}")
  public Response destroyLike(@Context UriInfo uriInfo,
                              @PathParam("activityId") String activityId,
                              @PathParam("identityId") String identityId,
                              @PathParam("format") String format) throws Exception{
    MediaType mediaType = Util.getMediaType(format);
    LikeList likeList =  null;
    likeList = destroyLike(activityId, identityId);
    return Util.getResponse(likeList, uriInfo, mediaType, Response.Status.OK);
  }
  
  
  /**
   * shows comment list by activityId
   * @param activityId
   * @return commentList
   * @see CommentList
   */
  private CommentList showComments(String activityId) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String rawCommentIds = activity.getExternalId();
    //rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds == null) {
      commentList.setComments(new ArrayList<Activity>());
    } else {
      String[] commentIds = rawCommentIds.split(",");
      for (String commentId: commentIds) {
        if (commentId.length() > 0) {
          commentList.addComment(_activityManager.getActivity(commentId));
        }
      }
    }
    return commentList;
  }
  
  /**
   * updates comment by activityId
   * @param activityId
   * @param comment
   * @return commentList
   * @see CommentList
   */
  private CommentList updateComment(String activityId, Activity comment) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    _activityManager = getActivityManager();
    //TODO hoatle set current userId from authentication context instead of getting userId from comment
    if (comment.getUserId() == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    comment.setPostedTime(System.currentTimeMillis());
    comment.setExternalId(Activity.IS_COMMENT);
    try {
      comment = _activityManager.saveActivity(comment);
      String rawCommentIds = activity.getExternalId();
      if (rawCommentIds == null) rawCommentIds = "";
      rawCommentIds += "," + comment.getId();
      activity.setExternalId(rawCommentIds);
      _activityManager.saveActivity(activity);
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    commentList.addComment(comment);
    return commentList;
  }
  
  /**
   * destroys comment by activityId and commentId
   * @param activityId
   * @param commentId
   * @return commentList
   * @see CommentList
   */
  private CommentList destroyComment(String activityId, String commentId) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String rawCommentIds = activity.getExternalId();
    try {
      if (rawCommentIds.contains(commentId)) {
        Activity comment = _activityManager.getActivity(commentId);
        if (activity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        commentList.addComment(comment);
        _activityManager.deleteActivity(commentId);
        commentId = "," + commentId;
        rawCommentIds = rawCommentIds.replace(commentId, "");
        activity.setExternalId(rawCommentIds);
        _activityManager.saveActivity(activity);
      } else {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
    } catch(Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return commentList;
  }
  

  /**
   * shows comment list by json/xml format
   * @param uriInfo
   * @param activityId
   * @param format 
   * @return response
   * @throws Exception
   */
  @GET
  @Path("{activityId}/comments/show.{format}")
  public Response showComments(@Context UriInfo uriInfo,
                               @PathParam("activityId") String activityId,
                               @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    CommentList commentList = null;
    commentList = showComments(activityId);
    return Util.getResponse(commentList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * updates comment by json/xml format
   * @param uriInfo
   * @param activityId
   * @param format 
   * @param comment
   * @return response
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/update.{format}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response updateComment(@Context UriInfo uriInfo,
                                @PathParam("activityId") String activityId,
                                @PathParam("format") String format,
                                Activity comment) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    CommentList commentList = null;
    commentList = updateComment(activityId, comment);
    return Util.getResponse(commentList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * destroys comments and returns json/xml format
   * @param uriInfo
   * @param activityId
   * @param commentId
   * @param format 
   * @return response
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/destroy/{commentId}.{format}")
  public Response destroyComment(@Context UriInfo uriInfo,
                                 @PathParam("activityId") String activityId,
                                 @PathParam("commentId") String commentId,
                                 @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    CommentList commentList = null;
    commentList = destroyComment(activityId, commentId);
    return Util.getResponse(commentList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * LikeList model
   * @author hoatle
   */
  @XmlRootElement
  static public class LikeList {
    private String _activityId;
    private List<Like> _likes;
    /**
     * sets activityId
     * @param activityId
     */
    public void setActivityId(String activityId) {
      _activityId = activityId;
    }
    /**
     * gets activityId
     * @return
     */
    public String getActivityId() {
      return _activityId;
    }
    /**
     * sets like list
     * @param likes like list
     */
    public void setLikes(List<Like> likes) {
      _likes = likes;
    }
    
    /**
     * gets like list
     * @return like list
     */
    public List<Like> getLikes() {
      return _likes;
    }
    
    /**
     * adds like to like list
     * @param like
     */
    public void addLike(Like like) {
      if (_likes == null) {
        _likes = new ArrayList<Like>();
      }
      _likes.add(like);
    }
  }
  
  /**
   * CommentList model
   * @author hoatle
   *
   */
  @XmlRootElement
  static public class CommentList {
    private String _activityId;
    private List<Activity> _comments;
    /**
     * sets activityId
     * @param activityId
     */
    public void setActivityId(String activityId) {
      _activityId = activityId;
    }
    /**
     * gets activityId
     * @return activityId
     */
    public String getActivityId() {
      return _activityId;
    }
    /**
     * sets comment list
     * @param comments comment list
     */
    public void setComments(List<Activity> comments) {
      _comments = comments;
    }
    /**
     * gets comment list
     * @return comments
     */
    public List<Activity> getComments() {
      return _comments;
    }
    /**
     * add comment to comment List
     * @param activity comment
     */
    public void addComment(Activity activity) {
      if (_comments == null) {
        _comments = new ArrayList<Activity>();
      }
      _comments.add(activity);
    }
  }
  
  /**
   * gets activityManager
   * @return activityManager
   * @see ActivityManager
   */
  private ActivityManager getActivityManager() {
    if (_activityManager == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      _activityManager = (ActivityManager) portalContainer.getComponentInstanceOfType(ActivityManager.class);
    }
    return _activityManager;
  }
  
  /**
   * gets identityManger
   * @return
   * @see IdentityManager
   */
  private IdentityManager getIdentityManager() {
    if (_identityManager == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      _identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return _identityManager;
  }
  
  /**
   * gets repository
   * @return repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    RepositoryService repositoryService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class);
    return repositoryService.getCurrentRepository().getConfiguration().getName();
  }
  
  /**
   * gets portalName
   * @return portal name
   */
  private String getPortalName() {
    return PortalContainer.getCurrentPortalContainerName();
  }
  
  /**
   * gets like list
   * @param identityIds
   * @return
   */
  private List<Like> getLikes(String[] identityIds) {
    String username, fullName, thumbnail;
    Profile profile;
    Identity identity;
    ProfileAttachment profileAttachment;
    Like like;
    List<Like> likes = new ArrayList<Like>();
    _identityManager = getIdentityManager();
    try {
      for (String identityId : identityIds) {
        identity = _identityManager.getIdentity(identityId);
        profile = identity.getProfile();
        username = (String) profile.getProperty(Profile.USERNAME);
        fullName = profile.getFullName();
        profileAttachment = (ProfileAttachment)profile.getProperty(Profile.AVATAR);
        thumbnail = null;
        if (profileAttachment != null) {
          thumbnail = "/" + getPortalName() + "/rest/jcr/" + getRepository() + "/" + profileAttachment.getWorkspace() +
                      profileAttachment.getDataPath() + "/?rnd=" + System.currentTimeMillis();
        }
        like = new Like();
        like.setIdentityId(identityId);
        like.setUsername(username);
        like.setFullName(fullName);
        like.setThumbnail(thumbnail);
        likes.add(like);
      }
    } catch (Exception ex) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    return likes;
  }
  
  /**
   * removes an item from an array
   * @param arrays
   * @param str
   * @return new array
   */
  private String[] removeItemFromArray(String[] array, String str) {
    List<String> list = new ArrayList<String>();
    list.addAll(Arrays.asList(array));
    list.remove(str);
    if(list.size() > 0) return list.toArray(new String[list.size()]);
    else return null;
  }
  
  /**
   * adds an item to an array
   * @param array
   * @param str
   * @return new array
   */
  private String[] addItemToArray(String[] array, String str) {
    List<String> list = new ArrayList<String>();
    if(array != null && array.length > 0) {
      list.addAll(Arrays.asList(array));
      list.add(str);
      return list.toArray(new String[list.size()]);
    } else return new String[] {str};
  }
}
