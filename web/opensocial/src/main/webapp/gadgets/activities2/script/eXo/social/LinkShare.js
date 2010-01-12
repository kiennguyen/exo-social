/**
 * LinkShare.js
 * gets information of a provided link,
 * edits this information, save to status update
 * @author  <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since   Oct 12, 2009
 * @copyright   eXo Platform SEA
 */

//uses namespace
var eXo = eXo || {};
eXo.social = eXo.social || {};

/**
 * LinkShare constructor 
 */
eXo.social.LinkShare = function(link, lang) {
	//alias imports
	var LinkShare = eXo.social.LinkShare,
		Locale = eXo.social.Locale,
		Util = eXo.social.Util;
	if (link != null) LinkShare.data.link = link;
	if (lang != null) LinkShare.data.lang = lang;
    //content constructed from eXo.social.LinkShare.data
    this.content = null;
    //privileged methods
    this.makeRequest = function() {
        var linkShareRequest = {
            link: LinkShare.data.link,
            lang: LinkShare.data.lang
          },
          url = LinkShare.config.LINKSHARE_REST_URL + '/show.json',
          linkShare = this;
          Util.makeRequest(url, function(res) {linkShare.callbackHandler(res);}, null,
        		         gadgets.io.MethodType.POST, gadgets.io.ContentType.JSON, linkShareRequest);
    }
    /**
     * callback handler 
     */
    this.callbackHandler = function(res) {
        //when can not get any info
    	function fallback(data) {
        	LinkShare.data.title = data.link; //TODO gets domain name only
        	LinkShare.data.link = data.link;
        	LinkShare.data.description = "";
        }
    	if (!res || !res.data) {
    		alert(eXo.social.Locale.getMsg('problem_happens_to_fix_soon'));
    		this.displayAttach(eXo.social.LinkShare.config.LINKSHARE_OPTION_ID);
       		return;
        }
    	
        var data = res.data;
        if (!data.title && !data.description) {
        	fallback(data);
        } else {
        	//binds data
        	for (var key in LinkShare.data) {
        		if (data[key] === null || data[key] === "" || data[key] === undefined) continue;
        		LinkShare.data[key] = data[key];
        	}
        }
        if (data.images.length == 0) {
        	LinkShare.data.images = data.images;
        	LinkShare.data.noThumbnail = true;
        } else if (LinkShare.data.images.length > 0) {//sets selectedImage
        	LinkShare.data.selectedImageIndex = 0;
        	LinkShare.data.noThumbnail = false;
        }
        //displays attachDisplay
        this.displayAttach(LinkShare.config.LINKSHARE_DISPLAY_ID);
    }
    return this;
}

/**
 * static settings object for LINKSHARE_REST_URL
 */
eXo.social.LinkShare.config = {
    //change to the right url
    LINKSHARE_REST_URL : "http://localhost:8080/rest/social/linkshare",
    //the div's id in gadget to work on in this div tag
    WORKSPACE_ID : "UIComposerExtension",
    LINKSHARE_OPTION_ID: "UILinkShareOption",
    LINKSHARE_ACTION_ID: "UILinkShareAction",
    LINKSHARE_DISPLAY_ID: "UILinkShareDisplay",
    THUMBNAIL_DISPLAY_ID : "UIThumbnailDisplay",
    EDITABLE_TEXT_ID : "UIEditableText",
    EXTENSION_MESSAGE_ID: "UIComposerExtensionMessage"
};

//static object for holding data
eXo.social.LinkShare.data = {
	link : null,
	lang : "en",
	title : null,
	description : null,
	images : null,
	//additional info
    mediumType : null,
    mediaType : null,
    mediaSrc : null,
    mediaAlbum : null,
    mediaArtist : null,
    mediaTitle : null,
    mediaHeight : null,
    mediaWidth : null,
    selectedImageIndex : null,
    noThumbnail : true
};

eXo.social.LinkShare.createMsg = function(msg) {
	var el = eXo.social.Util.getElementById(eXo.social.LinkShare.config.EXTENSION_MESSAGE_ID);
	if (el == null) {
		debug.warn('extension message id not found!');
		return;
	}
	el.innerHTML = msg;
}

eXo.social.LinkShare.clearMsg = function() {
	var el = eXo.social.Util.getElementById(eXo.social.LinkShare.config.EXTENSION_MESSAGE_ID);
	if (el == null) {
		debug.warn('extension message id not found!');
		return;
	}
	el.innerHTML = '';
}

/**
 * init function to create user interface 
 */
eXo.social.LinkShare.prototype.init = function() {
	this.addAttachOption();
}

/**
 * displays the element from linkShare.displayedAttach
 */
eXo.social.LinkShare.prototype.displayAttach = function(id) {
	if (!id) {
		debug.warn("No id specified!");
		return;
	}
	//alias 
	var Util = eXo.social.Util,
      LinkShare = eXo.social.LinkShare;
	  LinkShare.clearMsg();
	  Util.showElement(LinkShare.config.WORKSPACE_ID);
	//removes all attachs
	//TODO: Should hide for faster performance instead of removing
	Util.removeElementById(LinkShare.config.LINKSHARE_OPTION_ID);
	Util.removeElementById(LinkShare.config.LINKSHARE_ACTION_ID);
	Util.removeElementById(LinkShare.config.LINKSHARE_DISPLAY_ID);
	if (id === LinkShare.config.LINKSHARE_OPTION_ID) {
		this.addAttachOption();
	} else if (id === LinkShare.config.LINKSHARE_ACTION_ID) {
		this.addAttachAction();
	} else if (id === LinkShare.config.LINKSHARE_DISPLAY_ID) {
		this.addAttachDisplay();	
	}
	gadgets.window.adjustHeight();
}

/**
 * adds
 * <div class="Label">$_(attach)</div>
 * <div class="ImageFile AttachIcon"><span></span></div> //disabled now
 * <div class="VideoFile AttachIcon"><span></span></div> //disabled now
 * <div class="LinkAttach AttachIcon"><span></span></div>
 * <div class="ClearLeft"><span></span></div>
 */
eXo.social.LinkShare.prototype.addAttachOption = function() {
	var Locale = eXo.social.Locale,
      Util = eXo.social.Util,
      config = eXo.social.LinkShare.config;
	//reset data
	for (var key in eXo.social.LinkShare.data) {
		if (key === 'lang' || key === 'noThumbnail') continue;
		eXo.social.LinkShare.data[key] = null;
	}
	this.content = null;
	//creates a div elemenet with id = attachOption for users to click on to share
	var lsOptionTagName = 'div';
	var lsOptionId = config.LINKSHARE_OPTION_ID;
	var lsOptionHtml = [];
	lsOptionHtml.push('<div class="Label">' + Locale.getMsg('attach') + '</div>');
	//atOptionHtml.push('<div class="ImageFile AttachIcon"><span></span></div>'); //disabled now
 	//atOptionHtml.push('<div class="VideoFile AttachIcon"><span></span></div>'); //disabled now
 	lsOptionHtml.push('<div id="LinkAttach" class="LinkAttach AttachIcon"><span></span></div>'); //TODO: change class to LinkAttach
 	lsOptionHtml.push('<div class="ClearLeft"><span></span></div>');
	//adds element
	var newElement = Util.addElement(config.WORKSPACE_ID, lsOptionTagName, lsOptionId, lsOptionHtml.join(''));
	var linkElement = Util.getElementById('LinkAttach');
	if (!linkElement) {
		debug.warn('linkElement is null');
		return;
	}
	var linkShare = this;
	Util.addEventListener(linkElement, 'click', function() {
		linkShare.displayAttach(config.LINKSHARE_ACTION_ID)
	}, false);
}

/**
 * adds 
 * <div class="Addlink">Attach link:</div>
 * <div class="AddLinkContent">
 *   <input class="InputLink" type="textbox" />
 *   <a title="Add Link" id="#" href="#" class="IconAdd"> </a>
 *   <div style="clear: both; height: 0px;"><span></span></div>
 * </div>
 * <div style="clear: both; height: 0px;"><span></span></div>
 * creates a text input to paste the link, a attach button to get info from the link
 */
eXo.social.LinkShare.prototype.addAttachAction = function() {
	var Locale = eXo.social.Locale,
      Util = eXo.social.Util,
      config = eXo.social.LinkShare.config,
      lsActionTagName = 'div',
      lsActionId = config.LINKSHARE_ACTION_ID,
      lsActionHtml = [];
  lsActionHtml.push('<div class="AddLink"><a class="LinkAttach AttachIcon">&nbsp;</a> ' + Locale.getMsg('attach_link') + '(<span class="Close"><a href="#linkShare.displayAttachOption">' + Locale.getMsg('close') + '</a></span>)</div>');
  lsActionHtml.push('<div class="AddLinkContent">');
    lsActionHtml.push('<input id="InputLink" class="InputLink" type="textbox" />');
    lsActionHtml.push('<a id="AddLinkButton" title="' + Locale.getMsg('attach_link') + '" href="#attach_link" class="IconAdd"> </a>');
    lsActionHtml.push('<div style="clear:both; height: 0px"><span></span></div>');
  lsActionHtml.push('</div>');
  lsActionHtml.push('<div style="clear:both; height:0px;"><span></span></div>');
  /*
  lsActionHtml.push('</div>');
	lsActionHtml.push('<div class="LinkAttachAction">' + Locale.getMsg('link') + ' <span class="Close"><a href="#linkShare.displayAttachOption">' + Locale.getMsg('close') + '</a></span></div>');
	lsActionHtml.push('<div class="Attachction">');
		lsActionHtml.push('<input type="text" value="http://"');
		lsActionHtml.push('<a href="#linkShare.getInfo">' + Locale.getMsg('attach') + '</a>');
	lsActionHtml.push('</div>');
  */
	var newElement = Util.addElement(config.WORKSPACE_ID, lsActionTagName, lsActionId, lsActionHtml.join(''));
	//event attach
	var spanCloseElement = Util.getElementsByClass(newElement, 'span', 'Close')[0];
	var linkShare = this;
	
	Util.addEventListener(spanCloseElement, 'click', function() {
		linkShare.displayAttach(config.LINKSHARE_OPTION_ID);
	}, false);
	var inputElement = Util.getElementById('InputLink');
	Util.addEventListener(inputElement, 'focus', function() {
		if (this.value === 'http://') {
			this.value = '';
		}
	}, false);
	Util.addEventListener(inputElement, 'blur', function() {
		if (this.value === '') {
			this.value = 'http://'
		}
	}, false);
	Util.addEventListener(inputElement, 'keypress', function(e) {
		if (Util.isEnterKey(e)) {
			linkShare.getInfo();
		}
	}, false);
	var aAddLinkElement = Util.getElementById('AddLinkButton');
	Util.addEventListener(aAddLinkElement, 'click', function() {
		linkShare.getInfo();
	}, false);
    inputElement.focus();
}

/**
 * gets info from eXo.social.LinkShare.data
 * generates right html format
 * saves to this.content
 * <div id="attachDisplay">
 *	 <div>Display Content <span class="Close"><a href="#">Close</a></span></div>
 *   <div id="ThumbnailDisplay" class="ThumbnailDisplay">
 *	 	<!-- getThumbnailDisplay -->		
 *   </div>
 *	 <div id="NoThumbnail">
 * 	 	<input type="checkbox" /> No thumbnail.
 *   </div>
 *	 <div class="ContentDisplay">
 * 	 	<p class="ContentTitle"></p>
 *      <p class="ContentLink"></p>
 *      <p class="ContentDescription"><p>
 * 	 </div> 
 * </div> 
 * 
 * 
      <div class="LinkShareDisplay">
        <div class="ThumbnailLeft">
          <a class="ThumbnailBG" href="#">
            <img class="Thumbnail" src="http://localhost:8080/social/gadgets/activities2/style/images/AvatarPeople.gif"/>
          </a>
          <div class="ThumbnailAction">
            <div class="BackIcon"><span></span></div>
            <div class="NextIcon"><span></span></div>
            <div style="clear: both; height: 0px;"><span></span></div>
          </div>
        </div>
        <div class="ContentRight">
          <div class="Title">Titlele oghet</div>
          <div class="Content">Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor.</div>
          <div class="Source">Source: <a href="#">http:// localhost:8080/portal</a></div>
        </div>
        <div style="clear: left; height: 0px;"><span></span></div>
      </div>
 */
eXo.social.LinkShare.prototype.addAttachDisplay = function() {
	var Locale = eXo.social.Locale,
      Util = eXo.social.Util,
      LinkShare = eXo.social.LinkShare,
      config = LinkShare.config,
      linkShare = this,
      lsDisplayTagName = "div",
      lsDisplayId = config.LINKSHARE_DISPLAY_ID,
      lsDisplayHtml = [];
	  lsDisplayHtml.push('<div class="AddLink"><a class="LinkAttach AttachIcon">&nbsp;</a> ' + Locale.getMsg('content_to_display') + '(<span class="Close"><a href="#linkShare.displayAttachOption">' + Locale.getMsg('close') + '</a></span>)</div>');
		lsDisplayHtml.push('<div class="ThumbnailLeft">');
	  		lsDisplayHtml.push('<div id="' + config.THUMBNAIL_DISPLAY_ID +'"');
	  			lsDisplayHtml.push(this.getThumbnailDisplay());
	  		lsDisplayHtml.push('</div>');
  			if (LinkShare.data.images.length > 0) {
  			lsDisplayHtml.push('<div class="ThumbnailOption" id="ThumbnailOption">');
  				lsDisplayHtml.push('<input type="checkbox" />' + Locale.getMsg('no_thumbnail'));
  			lsDisplayHtml.push("</div>");
  			
  			} //end if
  			lsDisplayHtml.push('</div>');
		lsDisplayHtml.push('<div class="ContentRight">');
			lsDisplayHtml.push('<div class="Title Editable">' + LinkShare.data.title + '</div>');
			lsDisplayHtml.push('<div class="Content Editable">' + LinkShare.data.description + '</div>');
			lsDisplayHtml.push('<div class="Source">' + LinkShare.data.link + '</div>');
		lsDisplayHtml.push('</div>');
	Util.showElement(config.WORKSPACE_ID);
	var newElement = Util.addElement(config.WORKSPACE_ID, lsDisplayTagName, lsDisplayId, lsDisplayHtml.join(''));
	//attach event
	var spanCloseEl = Util.getElementsByClass(newElement, 'span', 'Close')[0];
	Util.addEventListener(spanCloseEl, 'click', function() {
		linkShare.displayAttach(config.LINKSHARE_OPTION_ID);
	}, false);
	
	var divNoThumbnail = Util.getElementById('ThumbnailOption');
	if (divNoThumbnail) {
		var cbNoThumbnail = divNoThumbnail.getElementsByTagName('input')[0];
		Util.addEventListener(cbNoThumbnail, 'click', function() {
			linkShare.enableThumbnailDisplay(cbNoThumbnail);
		}, false);
		this.addEventListenerToSelector();
	}

	
	var pContentTitle = Util.getElementsByClass(newElement, 'div', 'Title')[0];
	Util.addEventListener(pContentTitle, 'click', function() {
		linkShare.addEditableText(this, 'input');
	}, false);
	var pContentDescription = Util.getElementsByClass(newElement, 'div', 'Content')[0];
	Util.addEventListener(pContentDescription, 'click', function() {
		linkShare.addEditableText(this, 'textarea');
	}, false);
	
	//constructs content
	//this.constructContent();
	this.content = true;
}

/**
 * gets ThumbnailDisplay fragment
          <a class="ThumbnailBG" href="#">
            <img class="Thumbnail" src="http://localhost:8080/social/gadgets/activities2/style/images/AvatarPeople.gif"/>
          </a>
          <div class="ThumbnailAction">
            <div class="BackIcon"><span></span></div>
            <div class="Stats"></div>
            <div class="NextIcon"><span></span></div>
          </div>
 */
eXo.social.LinkShare.prototype.getThumbnailDisplay = function() {
	var Locale = eXo.social.Locale,
		Util = eXo.social.Util,
		LinkShare = eXo.social.LinkShare,
		config = LinkShare.config;
	if (LinkShare.data.selectedImageIndex === null) return;
	var thumbnailDisplay = [];
	thumbnailDisplay.push('<div>');
		thumbnailDisplay.push('<img class="Thumbnail" src="' + LinkShare.data.images[LinkShare.data.selectedImageIndex] + '" />');
		thumbnailDisplay.push('</div>');
		thumbnailDisplay.push('<div class="ThumbnailAction">');
		thumbnailDisplay.push('<div id="BackThumbnail" class="BackIcon"><span></span></div>');
		thumbnailDisplay.push('<div class="Stats">' + (LinkShare.data.selectedImageIndex + 1) + '/' + LinkShare.data.images.length + '</div>');
		thumbnailDisplay.push('<div id="NextThumbnail" class="NextIcon"><span></span></div>');
		thumbnailDisplay.push('<div style="clear: both"><span></span></div>');
	thumbnailDisplay.push('</div>');
	return thumbnailDisplay.join('');
}

/**
 * shows or hides #thumbnailDisplay 
 */
eXo.social.LinkShare.prototype.enableThumbnailDisplay = function(el) {
	var LinkShare = eXo.social.LinkShare;
	var Util = eXo.social.Util;
	var checked = el.checked;
	if (checked === true) {
		//hides
		LinkShare.data.noThumbnail = true;
		Util.hideElement(LinkShare.config.THUMBNAIL_DISPLAY_ID);
		el.parentNode.style.height = '80px';
	} else {
		//shows
		LinkShare.data.noThumbnail = false;
		Util.showElement(eXo.social.LinkShare.config.THUMBNAIL_DISPLAY_ID);
		el.parentNode.style.height = '20px';
	}
}

/**
 * creates input/ textarea element for edit inline
 * if tagName = input 
 * <input type="text" id="editableText" value="" />
 * if tagName = textarea
 * <textarea cols="10" rows="3">value</textarea>
 */
eXo.social.LinkShare.prototype.addEditableText = function(oldEl, tagName) {
	var LinkShare = eXo.social.LinkShare;
	var Util = eXo.social.Util;
	var textContent = oldEl.innerText; //IE
	if (textContent === undefined) {
		textContent = oldEl.textContent;
	}
	var editableEl = document.createElement(tagName);
	editableEl.setAttribute('id', LinkShare.config.EDITABLE_TEXT_ID);
	editableEl.value = textContent;
	//insertafter and hide oldEl
	Util.insertAfter(editableEl, oldEl);
	oldEl.style.display='none';
	editableEl.focus();
	//ENTER -> done
	Util.addEventListener(editableEl, 'keypress', function(e) {
		if (Util.isEnterKey(e)) {
			updateElement(this);
		}
	}, false);
	
	Util.addEventListener(editableEl, 'blur', function() {
		updateElement(this);
	}, false);
	
	var updateElement = function(editableEl) {
		//hide this, set new value and display
		var oldEl = editableEl.previousSibling;
		if (oldEl.innerText != null) { //IE
			oldEl.innerText = editableEl.value;
		} else {
			oldEl.textContent = editableEl.value;
		}
		//updates data
		//detects element by class, if class contains ContentTitle -> update title,
		// if class contains ContentDescription -> update description
		oldEl.style.display="block";
		if (Util.hasClass(oldEl, 'Title')) {
			LinkShare.data.title = editableEl.value;
		} else if (Util.hasClass(oldEl, 'Content')) {
			LinkShare.data.description = editableEl.value;
		}
		editableEl.parentNode.removeChild(editableEl);
	}
}


/**
 * processes to get information
 * Required: link property
 */
eXo.social.LinkShare.prototype.getInfo = function() {
	var Util = eXo.social.Util,
		Locale = eXo.social.Locale,
		LinkShare = eXo.social.LinkShare,
        config = eXo.social.LinkShare.config;
       lsActionEl = Util.getElementById(config.LINKSHARE_ACTION_ID);
	if (!lsActionEl) {
    debug.warn('lsActionEl is null!');
    return;
    }
	var inputEl = Util.getElementById('InputLink');
	if (!inputEl) {
		debug.warn('err: no input element in attachAction');
		return;
	}
	LinkShare.clearMsg();
	/**
	 * hides UIExtensionInput
	 * displays wait message
	 */
	function wait() {
		Util.hideElement(config.WORKSPACE_ID);
		LinkShare.createMsg(Locale.getMsg('getting_link_info_please_wait'));
	}
	if (inputEl.value === 'http://') {
		LinkShare.createMsg(Locale.getMsg('please_provide_link'));
		return;
	}
	if (!Util.isUrl(inputEl.value)) {
		LinkShare.createMsg(Locale.getMsg('not_valid_link'));
		return;
	}
	eXo.social.LinkShare.data.link = inputEl.value;
	wait();
    this.makeRequest();
}

///**
// * updates LinkShare object's attributes 
// */
//eXo.social.LinkShare.prototype.update = function(attrName, attrValue) {
//    if (attrName == null || attrValue == null) return;
//    if (eXo.social.LinkShare.data[attrName] != null) {
//        eXo.social.LinkShare.data[attrName] = attrValue;
//    }
//}

/**
 * views previous image 
 */
eXo.social.LinkShare.prototype.previousImage = function() {
	if (eXo.social.LinkShare.data.selectedImageIndex === 0) return;
	eXo.social.LinkShare.data.selectedImageIndex -= 1;
	this.updateThumbnailDisplay();
}

/**
 * views next image 
 */
eXo.social.LinkShare.prototype.nextImage = function() {
	if (eXo.social.LinkShare.data.selectedImageIndex === (eXo.social.LinkShare.data.images.length -1)) return;
	eXo.social.LinkShare.data.selectedImageIndex += 1;
	this.updateThumbnailDisplay();
}

/**
 * updates thumbnailDislay fragment 
 */
eXo.social.LinkShare.prototype.updateThumbnailDisplay = function() {
	var thumbnailDisplayEl = eXo.social.Util.getElementById(eXo.social.LinkShare.config.THUMBNAIL_DISPLAY_ID);
	if (!thumbnailDisplayEl) return;
	thumbnailDisplayEl.innerHTML = this.getThumbnailDisplay();
	this.addEventListenerToSelector();
}

/**
 * attach event handler to images selector
 */
eXo.social.LinkShare.prototype.addEventListenerToSelector = function() {
	var Util = eXo.social.Util,
		config = eXo.social.LinkShare.config,
		linkShare = this,
		backThumbnail = Util.getElementById('BackThumbnail'),
		nextThumbnail = Util.getElementById('NextThumbnail');
	Util.addEventListener(backThumbnail, 'click', function() {
		linkShare.previousImage();
	}, false);
	Util.addEventListener(nextThumbnail, 'click', function() {
		linkShare.nextImage();
	}, false);
}

/**
 * constructs content from LinkShare object to update status
 * saves to this.content
 * The body of activity should be:
 * <div class="Content">
 * 	<div class="Status"></div>
 *  <div class="Extension LinkShare"> <!-- constructs the content from this tag -->
 *		<div class="Link"></div> 
 *  	<div class="Thumbnail">
 * 			<img src="" />
 * 		</div> 
 * 		<div class="Detail">
 * 			<p class="Title"></p>
 *			<p class="Description"></p>
 * 		</div>
 *  </div>
 * </div> 
 * @warning('notused')
 */
eXo.social.LinkShare.prototype.constructContent = function() {
    var content = [];
    content.push("<div class=\"Extension LinkShare\">");
    	content.push("<div class=\"Link\">" + eXo.social.LinkShare.data.link + "</div>");
    	if (eXo.social.LinkShare.data.images.length > 0) {
    		if (eXo.social.LinkShare.data.noThumbnail == null || eXo.social.LinkShare.data.noThumbnail == false) {
    	content.push("<div class=\"Thumbnail\">");
    		content.push("<img title=\"" + eXo.social.LinkShare.data.title + "\" src=\"" + eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex] + "\" />");
    	content.push("</div>");
    		}//end if
    	} //end if
    	content.push("<div class=\"Detail\">");
    		content.push("<p class=\"Title\">" + eXo.social.LinkShare.data.title +"</p>");
    		if (eXo.social.LinkShare.data.description != null) {
    		content.push("<p class=\"Description\">" + eXo.social.LinkShare.data.description +"</p>");
    		} //end if
    	content.push("</div>");
    content.push("</div>");
    this.content = content.join("");
}

/**
 * saves activity
 * @param	status text input from status
 * @param	callback function callback after sending activity
 */

eXo.social.LinkShare.prototype.save = function(status, callback) {
	var LinkShare = eXo.social.LinkShare,
	//create activity params
		params = {};
	//params[opensocial.Activity.Field.TITLE] = eXo.social.LinkShare.data.title;
	//params[opensocial.Activity.Field.URL] = eXo.social.LinkShare.data.link;
	//add owner's comment to description
	//save all info to body tag
	/**
	 * body = {
	 * 	data : LinkShare.data
	 * } 
	 */
	var body = {};
	body.data = LinkShare.data;
	body.comment = status;
	//body.comment = status;
	params[opensocial.Activity.Field.TITLE] = body.data.title;
	params[opensocial.Activity.Field.BODY] = gadgets.json.stringify(body);
	//debug.info(params[opensocial.Activity.Field.BODY]);
	//thumbnail
//	if (eXo.social.LinkShare.data.noThumbnail == false) {
//		var mediaItems = [];
//		var opt_params = {};
//		opt_params[opensocial.Activity.MediaItem.Field.TYPE] = opensocial.Activity.MediaItem.Type.IMAGE;
//		//opt_params[opensocial.Activity.MediaItem.Field.URL] = eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex];
//		var imageLink = eXo.social.LinkShare.data.images[eXo.social.LinkShare.data.selectedImageIndex];
//		var mediaItem = opensocial.newActivityMediaItem(eXo.social.LinkShare.getMimeType(imageLink), imageLink, opt_params);
//		mediaItems.push(mediaItem);
//		params[opensocial.Activity.Field.MEDIA_ITEMS] = mediaItems;
//	}
	var activity = opensocial.newActivity(params);
	opensocial.requestCreateActivity(activity, opensocial.CreateActivityPriority.HIGH, callback);
	//resets
	this.displayAttach(LinkShare.config.LINKSHARE_OPTION_ID);
	this.content = null;
	eXo.social.LinkShare.data.link = null;
	eXo.social.LinkShare.data.images = null;
	eXo.social.LinkShare.data.title = null;
	eXo.social.LinkShare.data.description = null;
	eXo.social.LinkShare.data.selectedImageIndex = null;
	eXo.social.LinkShare.data.noThumbnail = true;
}