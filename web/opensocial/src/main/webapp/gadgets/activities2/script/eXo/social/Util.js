/**
 * Util.js
 * Utility class
 * @author	<a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since	Oct 20, 2009
 * @copyright	eXo Platform SEA
 */
 
//namespace
var eXo = eXo || {};
eXo.social = eXo.social || {};

/**
 * class definition
 */
 eXo.social.Util = function() {
 	//do not allow creating new object
 	if (this instanceof eXo.social.Util) {
		throw ("static class does not allow constructing a new object");
 	}
 }
 
/**
 * dynamic import js file from js file
 * virtual like java class import 
 */
 
eXo.social.Util.require = function(clazz) {
 	var arr = clazz.split(".");
 	var path = arr.join("/") + ".js";
 	var absPath = eXo.social.StatusUpdate.config.path.SCRIPT_PATH + "/" + path;
 	var js = document.createElement("script");
 	js.setAttribute("type", "text/javascript");
 	js.setAttribute("src", absPath);
 	var head = document.getElementsByTagName("head")[0];
 	if (!head) {
 		debug.error("no head tag found. Can not import js file: " + clazz);
 	}
 	head.appendChild(js);
 }


//importJS("eXo.social.IEPatch");
//importJS("eXo.social.Locale")
 
 /**
  * gets element by id
  * @static 
  */
 eXo.social.Util.getElementById = function(id) {
 	var el = document.getElementById(id);
 	if (!el) debug.info("can not find element with id: " + id);
 	return el;
 }
 
 /**
  * gets element by tagName
  * @param	tagName
  * @param	parent element
  * @static
  */
 eXo.social.Util.getElementsByTagName = function(tagName, parent) {
 	var parent = parent || document;
 	var els = parent.getElementsByTagName(tagName);
 	if (!els) debug.info("can not find elements with tagName: " + tagName);
 	debug.groupEnd();
 	return els;
 }
 
 /**
 * Returns true if element has the css clazz
 * Uses a regular expression to search more quickly
 * @param	element
 * @param	clazz
 * @return	boolean
 * @static
 */
eXo.social.Util.hasClass = function(element, clazz) {
	var reg = new RegExp('(^|\\s+)' + clazz + '(\\s+|$)');
	return reg.test(element['className']);
} ;
 
 /**
  * gets element by clazz
  * @param	clazz
  * @param	parentElement
  * @return	array
  * @static
  */
 eXo.social.Util.getElementsByClass = function(root, tagName, clazz) {
 	var Util = eXo.social.Util;
	var list = [];
	var nodes = root.getElementsByTagName(tagName);
	for (var i = 0, l = nodes.length; i < l; i++)  {
		if (Util.hasClass(nodes[i], clazz)) list.push(nodes[i]);
	}
  	return list;
 }
 
 
/**
 * adds element with specified parentId, tagName, elementId and html content
 * @param	parentId
 * @param	tagName
 * @param	elementId
 * @param	html
 * @return	newElement 
 * @static
 */
eXo.social.Util.addElement = function(parentId, tagName, elementId, html) {
    if (parentId === null || tagName === null || html === null) {
    	debug.warn("Do not provide all params");
    	return;
    }
    if (document.getElementById(elementId)) {
    	return; //do not create repeated element
    }
    var parent = document.getElementById(parentId);
    if (parent === null) return;
    var newElement = document.createElement(tagName);
    if (elementId) {
    	newElement.setAttribute('id', elementId);
    }
    newElement.innerHTML = html;
    parent.appendChild(newElement);
    return newElement;
}

/**
 * removes element from DOM by its id
 * @param	elementId
 * @static
 */
eXo.social.Util.removeElement = function(elementId) {
    var element = document.getElementById(elementId);
    if (element === null) {
    	return;
    }
    element.parentNode.removeChild(element);
}

/**
 * hides element by its id
 * @param	elementId
 * @static
 */
eXo.social.Util.hideElement = function(elementId) {
	var element = document.getElementById(elementId);
	if (element === null) {
		debug.warn('Util.hideElement: Can not find element by its id: ' + elementId);
		return;
	} 
	element.style.display='none';
}

/**
 * shows element by id
 * @param	elementI
 * @display	can be "inline" or "block" with default = "block" 
 * @static
 */
eXo.social.Util.showElement = function(elementId, display) {
	if (display !== 'inline') {
		display = 'block';
	}
	var element = document.getElementById(elementId);
	if (element == null) {
		debug.warn('Util.showElement: Can not find element by its id: ' + elementId);
		return;
	} 
	element.style.display = display;
}

/**
 * inserts an element after an element
 * @param	newNode the node/ element to be inserted
 * @param	refNode the reference node/ element
 * @static 
 */
eXo.social.Util.insertAfter = function(newNode, refNode) {
	if (!newNode || !refNode) {
		debug.warn("newNode or refNode is null");
		debug.groupEnd();
		return;
	}
	//checks if refNode.nextSibling is null
	refNode.parentNode.insertBefore(newNode, refNode.nextSibling);
}

/**
 * checks if a provided string is a correct url format
 * @param	url
 * @return true if url is correct format, else false 
 * @static
 */
eXo.social.Util.isUrl = function(url) {
	var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
	return regexp.test(url);
}

/**
 * checks if keyNum == ENTER key
 * @param	event
 * @static
 */
eXo.social.Util.isEnterKey = function(e) {
	var keyNum;
	var ENTER_KEY_NUM = 13;
	if(window.event) {// IE
  		keyNum = e.keyCode;
  	} else if (e.which) { // Netscape/Firefox/Opera
  		keyNum = e.which;
  	}
  	if (ENTER_KEY_NUM == keyNum) {
		return true;
  	}
  	return false;
}

/**
 * gets value of an attribute name from an element
 * @param	dom element
 * @param	attribute name
 * @return	attribute value
 * @static
 * @deprecated
 */
eXo.social.Util.getAttributeValue = function(element, attrName) {
	for(var x = 0, l = element.attributes.length; x < l; x++) {
	  if(element.attributes[x].nodeName.toLowerCase() == attrName) {
		return element.attributes[x].nodeValue;
	  }
	}
	return null;
}

/**
 * gets mimetype from a provided link
 * @param	link provided link
 * @return	mimetype string = type/extension
 * @static 
 * //TODO hoatle: complete this method with more mime types
 */
eXo.social.Util.getMimeType = function(link) {
	if (!link) {
		debug.warn("link param is required!");
		debug.groupEnd();
		return;
	}
	//currently gets mimeType from images
	var imagesArr = ['jpg', 'jpeg', 'gif', 'png'];
	
	var startIndex = link.lastIndexOf('.');
	var extension = link.substring((startIndex + 1), link.length);
	var type = null;
	if (imagesArr.indexOf(extension) > -1) type = 'image';
	if (type == null) {
		debug.warn("Can not detect the mime type");
		debug.groupEnd();
		return;
	}
	debug.groupEnd();
	return type + '/' + extension;
}

/**
 * Cross browser add event listener method. For 'evt' pass a string value with the leading "on" omitted
 * e.g. Util.addEventListener(window,'load',myFunctionNameWithoutParenthesis,false);
 * @param	obj object to attach event
 * @param	evt event name: click, mouseover, focus, blur...
 * @param	func	function name
 * @param	useCapture	true or false; if false => use bubbling
 * @static
 * @see		http://phrogz.net/JS/AttachEvent_js.txt
 */
eXo.social.Util.addEventListener = function(obj, evt, fnc, useCapture) {
	if (obj === null || evt === null || fnc ===  null || useCapture === null) {
		debug.warn('all params is required from Util.addEventListener!');
		return;
	}
	if (!useCapture) useCapture = false;
	if (obj.addEventListener){
		obj.addEventListener(evt, fnc, useCapture);
	} else if (obj.attachEvent) {
		obj.attachEvent('on'+evt, fnc);
	} else{
		myAttachEvent(obj, evt, fnc);
		obj['on'+evt] = function() { myFireEvent(obj,evt) };
	}

	//The following are for browsers like NS4 or IE5Mac which don't support either
	//attachEvent or addEventListener
	var myAttachEvent = function(obj, evt, fnc) {
		if (!obj.myEvents) obj.myEvents={};
		if (!obj.myEvents[evt]) obj.myEvents[evt]=[];
		var evts = obj.myEvents[evt];
		evts[evts.length] = fnc;
	}
	
	var myFireEvent = function(obj, evt) {
		if (!obj || !obj.myEvents || !obj.myEvents[evt]) return;
		var evts = obj.myEvents[evt];
		for (var i=0,len=evts.length;i<len;i++) evts[i]();
	}
}

/**
 * removes event listener. 
 * @param	obj element
 * @param	evt event name, 'click', 'blur'. 'focus'...
 * @func	function name to be removed if found
 * @static
 * //TODO make sure method cross-browsered
 */
eXo.social.Util.removeEventListener = function(obj, evt, func) {
	if (obj.removeEventListener) {
		obj.removeEventListener(evt, func, false);
	} else if (obj.detachEvent) {//IE
		obj.detachEvent('on'+evt, func)
	}
}

/**
 * makes remote request
 * @param	url
 * @param	callback
 * @param	opt_refreshInterval
 * @param	opt_method
 * @param	opt_contentType
 * @static 
 */
eXo.social.Util.makeRequest = function(url, callback, opt_refreshInterval, opt_method, opt_contentType) {
	//TODO handles method + contentType
	var refreshInterval = opt_refreshInterval || 0;
	var method = gadgets.io.MethodType.GET;
	var contentType = gadgets.io.ContentType.JSON;
	
	var ts = new Date().getTime();
	var sep = "?";
	if (refreshInterval && refreshInterval > 0) {
	    ts = Math.floor(ts / (refreshInterval * 1000));
	}
	if (url.indexOf("?") > -1) {
	   sep = "&";
	}
	  
	url = [ url, sep, "nocache=", ts ].join("");
	var params = {};
	params[gadgets.io.RequestParameters.METHOD] = method;
	params[gadgets.io.RequestParameters.CONTENT_TYPE] = contentType;
	gadgets.io.makeRequest(url, callback, params);
}
/**
 * converts timestamp to pretty time
 * Use resource bundle
 * //TODO about (?) days ago? 
 * @param	date Number
 * @static 
 */
eXo.social.Util.toPrettyTime = function(date) {
	var Locale = eXo.social.Locale;
	if (isNaN(date)) {
	    return Locale.getMsg('an_undetermined_amount_of_time_ago');
	}
	time = (new Date().getTime() - date) / 1000;
	
	if (time < 60) {
	    return Locale.getMsg('less_than_a_minute_ago');
	} else {
	    if (time < 120) {
	        return Locale.getMsg('about_a_minute_ago');
	    } else {
	        if (time < 3600) {
	            var mins = Math.round(time / 60);
	            return Locale.getMsg('about_0_minutes_ago', [mins]);
	        } else {
	            if (time < 7200) {
	                return Locale.getMsg('about_an_hour_ago');
	            } else {
	                if (time < 86400) {
	                    var hours = Math.round(time / 3600);
	                    return Locale.getMsg('about_0_hours_ago', [hours]);
	                } else {
	                	return '' + getPostedDate(date);
	                }
	            }
	        }
	    }
	}
	var getPostedDate = function(date) {
		var dayNames = ["sunday", "monday", "tuesday",
		"wednesday", "thursday", "friday", "saturday"];
	
		var monthNames = ["january", "february", "march", 
		"april", "may", "june", "july", "august", "september", 
		"october", "november", "december"];
		for (var i = 0, l = dayNames.length; i < l; i++) {
			dayNames[i] = Locale.getMsg(dayNames[i]);
		}
		
		for (var i = 0, l = monthName.length; i < l; i++) {
			monthNames = Locale.getMsg(monthNames[i]);
		}
	
		var currentMonth = date.getMonth();
		var currentYear = date.getFullYear();
		var currentDay = date.getDay();
		var currentDate = date.getDate();
		var ap = "";
		var currentHour = date.getHours();
		
		if (currentHour < 12) {
		   ap = Locale.getMsg('am');
		}
		else {
		   ap = Locale.getMsg('pm');
		}
		if (currentHour === 0) {
		   currentHour = 12;
		}
		if (currentHour > 12) {
		   currentHour = currentHour - 12;
		}
	
		var currentMin = date.getMinutes();
	
		currentMin = currentMin + "";
	
		if (currentMin.length === 1) {
		   currentMin = "0" + currentMin;
		}
		return Locale.getMsg('day_date_month_year_at_hour_min_ap',
							[dayNames[currentDay], currentDate, monthNames[currentMonth], currentYear, currentHour, currentMin, ap]);
	}
}