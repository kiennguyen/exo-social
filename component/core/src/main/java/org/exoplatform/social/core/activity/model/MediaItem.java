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
package org.exoplatform.social.core.activity.model;

import org.apache.shindig.social.opensocial.model.Address;

/**
 * The Class MediaItem represent MediaItem in opensocial.
 */
public class MediaItem implements org.apache.shindig.social.opensocial.model.MediaItem {

  /** The mime type. */
  private String mimeType;

  /** The type. */
  private Type type;

  /** The url. */
  private String url;

  private String thumbnailUrl;


  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   *
   * @param mimeType the new mime type
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url.
   *
   * @param url the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

   public String getAlbumId()
   {
      throw new RuntimeException("PAF");
   }

   public void setAlbumId(final String albumId)
   {
      throw new RuntimeException("PAF");
   }

   public String getCreated()
   {
      throw new RuntimeException("PAF");
   }

   public void setCreated(final String created)
   {
      throw new RuntimeException("PAF");
   }

   public String getDescription()
   {
      throw new RuntimeException("PAF");
   }

   public void setDescription(final String description)
   {
      throw new RuntimeException("PAF");
   }

   public String getDuration()
   {
      throw new RuntimeException("PAF");
   }

   public void setDuration(final String duration)
   {
      throw new RuntimeException("PAF");
   }

   public String getFileSize()
   {
      throw new RuntimeException("PAF");
   }

   public void setFileSize(final String fileSize)
   {
      throw new RuntimeException("PAF");
   }

   public String getId()
   {
      throw new RuntimeException("PAF");
   }

   public void setId(final String id)
   {
      throw new RuntimeException("PAF");
   }

   public String getLanguage()
   {
      throw new RuntimeException("PAF");
   }

   public void setLanguage(final String language)
   {
      throw new RuntimeException("PAF");
   }

   public String getLastUpdated()
   {
      throw new RuntimeException("PAF");
   }

   public void setLastUpdated(final String lastUpdated)
   {
      throw new RuntimeException("PAF");
   }

   public Address getLocation()
   {
      throw new RuntimeException("PAF");
   }

   public void setLocation(final Address location)
   {
      throw new RuntimeException("PAF");
   }

   public String getNumComments()
   {
      throw new RuntimeException("PAF");
   }

   public void setNumComments(final String numComments)
   {
      throw new RuntimeException("PAF");
   }

   public String getNumViews()
   {
      throw new RuntimeException("PAF");
   }

   public void setNumViews(final String numViews)
   {
      throw new RuntimeException("PAF");
   }

   public String getNumVotes()
   {
      throw new RuntimeException("PAF");
   }

   public void setNumVotes(final String numVotes)
   {
      throw new RuntimeException("PAF");
   }

   public String getRating()
   {
      throw new RuntimeException("PAF");
   }

   public void setRating(final String rating)
   {
      throw new RuntimeException("PAF");
   }

   public String getStartTime()
   {
      throw new RuntimeException("PAF");
   }

   public void setStartTime(final String startTime)
   {
      throw new RuntimeException("PAF");
   }

   public String getTaggedPeople()
   {
      throw new RuntimeException("PAF");
   }

   public void setTaggedPeople(final String taggedPeople)
   {
      throw new RuntimeException("PAF");
   }

   public String getTags()
   {
      throw new RuntimeException("PAF");
   }

   public void setTags(final String tags)
   {
      throw new RuntimeException("PAF");
   }

   public String getTitle()
   {
      throw new RuntimeException("PAF");
   }

   public void setTitle(final String title)
   {
      throw new RuntimeException("PAF");
   }
}
