/* = membership.js ========================================================== */
/* Defines basic functionality for use in personalization
 * The global object in charge, window.polopoly, contains functionality for
 * cookies, base64 encoding, json etc.
 * ========================================================================== */

if (polopoly) throw("Initialized twice");
var polopoly = {};

/* = Custom services ======================================================== */
/* Namespace to use for custom services. Services added to this namespace     */
/* have their init method called                                              */
polopoly.service = {};

/* = User preferences ======================================================= */
polopoly.user = {
       init: function() {
           polopoly.user.data = polopoly.user._initCookie("cata");
           polopoly.user.sessionData = polopoly.user._initCookie("data");
       },
       _initCookie: function(cookie) {
           var ud = polopoly.cookie.get(cookie);
           if (!ud) return;
           var data = polopoly.base64.stringDecode(ud);
           if (!data) return;
           try {
               var json = polopoly.json.parse(data);
           } catch (e) {
               return;
           }
           return json;

       },
       getServiceSettings: function(serviceDefinitionId, serviceInstanceId) {
           if (!polopoly.user.data || !polopoly.user.data[serviceDefinitionId]) return;
           return polopoly.user.data[serviceDefinitionId][serviceInstanceId];
       },
       setServiceSettings: function(serviceDefinitionId, serviceInstanceId, instanceData) {
           if (!polopoly.user.data) polopoly.user.data = {};
           if (!polopoly.user.data[serviceDefinitionId]) polopoly.user.data[serviceDefinitionId] = {};

           polopoly.user.data[serviceDefinitionId][serviceInstanceId] = instanceData;
           polopoly.user._persistDataInCookie();
       },
       getSessionServiceSettings: function(serviceDefinitionId, serviceInstanceId) {
           if (!polopoly.user.sessionData || !polopoly.user.sessionData[serviceDefinitionId]) return;
           return polopoly.user.sessionData[serviceDefinitionId][serviceInstanceId];
       },
       setSessionServiceSettings: function(serviceDefinitionId, serviceInstanceId, instanceData) {
           if (!polopoly.user.sessionData) polopoly.user.sessionData = {};
           if (!polopoly.user.sessionData[serviceDefinitionId]) polopoly.user.sessionData[serviceDefinitionId] = {};

           polopoly.user.sessionData[serviceDefinitionId][serviceInstanceId] = instanceData;
           polopoly.user._persistSessionDataInCookie();
       },
       _persistDataInCookie: function() {
           var dataJson = polopoly.json.stringify(polopoly.user.data);
           var dataJsonBase64 = polopoly.base64.stringEncode(dataJson);
           polopoly.cookie.set("cata", dataJsonBase64);
       },
       _persistSessionDataInCookie: function() {
           var dataJson = polopoly.json.stringify(polopoly.user.sessionData);
           var dataJsonBase64 = polopoly.base64.stringEncode(dataJson);
           polopoly.cookie.setForSession("data", dataJsonBase64);
       },
       isLoggedIn: function() {
           return polopoly.cookie.get("sessionKey") != null &&
                  polopoly.cookie.get("loginName")  != null &&
                  polopoly.cookie.get("userId")     != null;
       },
       name: function() {
           return polopoly.base64.stringDecode(polopoly.cookie.get("loginName"));
       },
       screenName: function() {
           return polopoly.base64.stringDecode(polopoly.cookie.get("screenName"));
       },
       popMessageCookie: function(name) {
            var b64cookie = polopoly.cookie.get(name);
            var cookie = polopoly.base64.stringDecode(b64cookie);
            if (cookie.length) {
                var _cv;
                try {
                    _cv = polopoly.json.parse(cookie);
                } catch (e) {
                }
                polopoly.cookie.clear(name);
                return _cv;
            }
        },
        refreshUserData: function(errorCallback, successCallback) {
                jQuery.ajax({"cache": false,
                     "error": errorCallback,
                     "success": successCallback,
                     "timeout": 10000,
                     "type": "GET",
                     "url": "/membership/refresh"});
        },
        persistUserServiceData: function(serviceDefinition, serviceInstance, errorCallback, successCallback) {
            jQuery.ajax({"cache": false,
                        "data": {"sdid": serviceDefinition,
                                 "siid": serviceInstance,
                                 "csrf_token": polopoly.cookie.get("sessionKey")},
                        "error": errorCallback,
                        "success": successCallback,
                        "timeout": 10000,
                        "type": "POST",
                        "url": "/membership/persist"});
        }
};

/* = Cookie functions ======================================================= */
polopoly.cookie = {
        clear: function(name) {
            document.cookie=name + '= ; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/';
        },
        set: function(name, value) {
            document.cookie=name + '=' + value + '; expires=Fri, 01 Jan 2038 00:00:00 UTC; path=/';
        },
        setForSession: function(name, value) {
            document.cookie=name + '=' + value + '; expires=-1; path=/';
        },
        get: function(name) {
            return polopoly.util.stringAsHashValue(name, document.cookie, ';');
        }
 };

polopoly.comments = {
    get: function(contentPath, page, containerSelector, whenDone, showCommentId) {
        var params = {"comments": page, "ajax":"true", "ot":"example.AjaxPageLayout.ot"};
        if (showCommentId !== undefined) {
            params.showCommentId = showCommentId;
        }
        jQuery.get(contentPath, params,
                   function(data) {
                       $(containerSelector).html(data);
                       if (whenDone) {
                           whenDone(containerSelector);
                       }
                   },
                   "html");
    }
};

/* = JSON parsing =========================================================== */
/*  http://www.JSON.org/json2.js Public Domain.
 *  See http://www.JSON.org/js.html
 *
 *  text = polopoly.json.stringify(['e', {pluribus: 'unum'}]);
 *  hash = polopoly.json.parse(text, reviver)                                 */
polopoly.json = {
        _cx: /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        _escapable: /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        _gap: null,
        _indent: null,
        _meta: {    // table of character substitutions
                '\b': '\\b',
                '\t': '\\t',
                '\n': '\\n',
                '\f': '\\f',
                '\r': '\\r',
                '"' : '\\"',
                '\\': '\\\\'
          },
          _rep: null,

        _f: function(n) {
            return n < 10 ? '0' + n : n;
        },
        init: function() {

            if (typeof Date.prototype.toJSON !== 'function') {

                Date.prototype.toJSON = function (key) {

                    return this.getUTCFullYear()   + '-' +
                        this._f(this.getUTCMonth() + 1) + '-' +
                        this._f(this.getUTCDate())      + 'T' +
                        this._f(this.getUTCHours())     + ':' +
                        this._f(this.getUTCMinutes())   + ':' +
                        this._f(this.getUTCSeconds())   + 'Z';
                };

                String.prototype.toJSON =
                    Number.prototype.toJSON =
                    Boolean.prototype.toJSON = function (key) {
                    return this.valueOf();
                };
            }
        },
        _quote: function(string) {
                this._escapable.lastIndex = 0;
                return this._escapable.test(string) ?
                    '"' + string.replace(this._escapable, function (a) {
                    var c = this._meta[a];
                    return typeof c === 'string' ? c :
                        '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                }) + '"' :
                    '"' + string + '"';
            },


          _str: function(key, holder) {
                var i,          // The loop counter.
                k,          // The member key.
                v,          // The member value.
                length,
                mind = this._gap,
                partial,
                value = holder[key];

                if (value && typeof value === 'object' &&
                    typeof value.toJSON === 'function') {
                    value = value.toJSON(key);
                }
                if (typeof this._rep === 'function') {
                    value = this._rep.call(holder, key, value);
                }
                switch (typeof value) {
                    case 'string':
                        return this._quote(value);

                    case 'number':
                        return isFinite(value) ? String(value) : 'null';

                    case 'boolean':
                    case 'null':
                        return String(value);

                    case 'object':
                        if (!value) {
                            return 'null';
                        }
                        this._gap += this._indent;
                        partial = [];
                        if (Object.prototype.toString.apply(value) === '[object Array]') {
                            length = value.length;
                            for (i = 0; i < length; i += 1) {
                                partial[i] = this._str(i, value) || 'null';
                            }
                            v = partial.length === 0 ? '[]' :
                                this._gap ? '[\n' + this._gap +
                                partial.join(',\n' + this._gap) + '\n' +
                                mind + ']' :
                                '[' + partial.join(',') + ']';
                            this._gap = mind;
                            return v;
                        }
                        if (this._rep && typeof this._rep === 'object') {
                            length = this._rep.length;
                            for (i = 0; i < length; i += 1) {
                                k = this._rep[i];
                                if (typeof k === 'string') {
                                    v = this._str(k, value);
                                    if (v) {
                                        partial.push(this._quote(k) + (this._gap ? ': ' : ':') + v);
                                    }
                                }
                            }
                        } else {
                            for (k in value) {
                                if (Object.hasOwnProperty.call(value, k)) {
                                    v = this._str(k, value);
                                    if (v) {
                                        partial.push(this._quote(k) + (this._gap ? ': ' : ':') + v);
                                    }
                                }
                            }
                        }
                        v = partial.length === 0 ? '{}' :
                            this._gap ? '{\n' + this._gap + partial.join(',\n' + this._gap) + '\n' +
                            mind + '}' : '{' + partial.join(',') + '}';
                        this._gap = mind;
                        return v;
                }
            },

        stringify: function(value, replacer, space) {
            var i;
            this._gap = '';
            this._indent = '';
            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    this._indent += ' ';
                }
            } else if (typeof space === 'string') {
                this._indent = space;
            }
            this._rep = replacer;
            if (replacer && typeof replacer !== 'function' &&
                (typeof replacer !== 'object' ||
                typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }
            return this._str('', {'': value});
        },

        parse: function (text, reviver) {
            var j;
            function walk(holder, key) {
                var k, v, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }
            this._cx.lastIndex = 0;
            if (this._cx.test(text)) {
                text = text.replace(this._cx, function (a) {
                    return '\\u' +
                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }
            if (/^[\],:{}\s]*$/.
                test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@').
                replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
                replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {
                j = eval('(' + text + ')');
                return typeof reviver === 'function' ?
                    walk({'': j}, '') : j;
            }
            throw new SyntaxError('JSON.parse');
        }
};

/* = General utilities =======================================================*/
polopoly.util = {
    querystringValue: function(name) {
        return this.stringAsHashValue(name, location.search.substr(1), '&');
    },

    stringAsHashValue: function(key, string, sep) {
        if (string && string != '') {
            var items = string.split(sep);
            for (var i = 0; i < items.length; i++) {
                var value = jQuery.trim(items[i]);
                if (value.substring(0, key.length + 1) == (key + '=')) {
                    return decodeURIComponent(value.substring(key.length + 1));
                }
            }
        }
    },

    injectCSRFToken: function(target) {
       var token = polopoly.cookie.get("sessionKey");
       jQuery("<input>").attr({
            type: 'hidden',
            name: 'csrf_token',
            value: token
        }).appendTo(target);
    }
};

/* = Base64 encode/decode =================================================== */
polopoly.base64 = {

    _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

        /* This code was written by Tyler Akins and has been placed in the
         * public domain.  It would be nice if you left this header intact.
         * Base64 code from Tyler Akins -- http://rumkin.com
         * Modded to handle input with stripped ending =.
         */

        //
        // Decode a string in utf8 encoded in base64 into a string.
        //
        stringDecode: function(input) {
            return this._utf8_decode(this.decode(input));
        },
        decode : function (input) {
            try {
                if (input.length==0) {
                    return "";
                }
            }
            catch (e) {
                return "";
            }

            // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
            input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
            input = this._padIfNecessary(input);

            var output = "";
            var chr1, chr2, chr3;
            var enc1, enc2, enc3, enc4;

            var i = 0;
            do {
                enc1 = this._keyStr.indexOf(input.charAt(i++) || "=");
                enc2 = this._keyStr.indexOf(input.charAt(i++) || "=");
                enc3 = this._keyStr.indexOf(input.charAt(i++) || "=");
                enc4 = this._keyStr.indexOf(input.charAt(i++) || "=");

                chr1 = (enc1 << 2) | (enc2 >> 4);
                chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                chr3 = ((enc3 & 3) << 6) | enc4;

                output = output + String.fromCharCode(chr1);

                if (enc3 != 64) {
                    output = output + String.fromCharCode(chr2);
                }
                if (enc4 != 64) {
                    output = output + String.fromCharCode(chr3);
                }
            } while (i < input.length);

            return output;
        },
        _padIfNecessary: function(input) {
            if ((input.length % 4) == 0) {
                return input;
            }

            var missingChars = (4 - (input.length % 4));
            for (var i = 0; i < missingChars; i++) {
                input += "=";
            }

            return input;
        },

        //
        // Encode a string into utf8 encoded bytes in a base64 string.
        //
        stringEncode: function(input) {
            return this.encode(this._utf8_encode(input));
        },
        encode : function (input) {
            var output = "";
            var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
            var i = 0;

            while (i < input.length) {

                chr1 = input.charCodeAt(i++);
                chr2 = input.charCodeAt(i++);
                chr3 = input.charCodeAt(i++);

                enc1 = chr1 >> 2;
                enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                enc4 = chr3 & 63;

                if (isNaN(chr2)) {
                    enc3 = enc4 = 64;
                } else if (isNaN(chr3)) {
                    enc4 = 64;
                }

                output = output +
                this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
                this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);

            }

            return output;
        },
        _utf8_encode : function (string) {
            string = string.replace(/\r\n/g,"\n");
            var utftext = "";

            for (var n = 0; n < string.length; n++) {

                var c = string.charCodeAt(n);

                if (c < 128) {
                    utftext += String.fromCharCode(c);
                }
                else if((c > 127) && (c < 2048)) {
                    utftext += String.fromCharCode((c >> 6) | 192);
                    utftext += String.fromCharCode((c & 63) | 128);
                }
                else {
                    utftext += String.fromCharCode((c >> 12) | 224);
                    utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                    utftext += String.fromCharCode((c & 63) | 128);
                }

            }

            return utftext;
        },
        _utf8_decode: function(utftext) {
            var string = "";
            var i = 0;
            var c = 0;
            var c1 = 0;
            var c2 = 0;

            while ( i < utftext.length ) {
                c = utftext.charCodeAt(i);
                if (c < 128) {
                    string += String.fromCharCode(c);
                    i++;
                }
                else if((c > 191) && (c < 224)) {
                    c2 = utftext.charCodeAt(i+1);
                    string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                    i += 2;
                }
                else {
                    c2 = utftext.charCodeAt(i+1);
                    var c3 = utftext.charCodeAt(i+2);
                    string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                    i += 3;
                }
            }
            return string;
        }
 };

/* = Login box ============================================================== */
/* Assumes a global object exists on the page, called p_l_i18n                */
polopoly.loginBox = {

    _login: {
        "auth":    "authError",
        "perm":    "permError",
        "down":    "downError",
        "user":    "userError",
        "default": "defaultError"
    },

        init: function() {
            document.__ppUseDefLogin = false;
            try {
                this.defLogin =  p_l_i18n.defaultLoginName;
            }
            catch (e) {
                this.defLogin = "E-mail";
            }
            var defLogin = this.defLogin;

            // Give the browser some time to autofill before
            // hooking in default values
            window.setTimeout(this._tryAutoFill, 500);

            // Textboxes that clear themselves extravaganza.
            jQuery(".clearable").
            focus(function() {
                e=jQuery(this); if(document.__ppUseDefLogin && e.val() == defLogin) {
                    e.val("");
                }
            });
            jQuery(".clearable").blur (function() {
                e=jQuery(this); if(document.__ppUseDefLogin && e.val() == "") {
                    e.val(defLogin);
                }
            });

            // Fix so that when someone tries to submit the default values,
            // we submit the empty values instead.
            jQuery(".not-loggedin .submit").click(function() {
                var parent = jQuery(this).parents(".not-loggedin");
                var username = parent.find(".loginname");
                var password = parent.find(".password");
                if (username.val() == defLogin && password.val() == defLogin) {
                    username.val("");
                    password.val("");
                }
            });
            jQuery(".clearable").blur();

            var loginName  = polopoly.cookie.get("loginName");
            var isLoggedIn = polopoly.cookie.get("sessionKey");

            // Decide on which box to show
            if (polopoly.user.isLoggedIn()) {
                jQuery(".loggedin").show();
                jQuery(".loggedin .user-name").append(polopoly.user.name());
            }
            else {
                jQuery(".not-loggedin").show();
            }

            // Error management
            this._trySetError(".not-loggedin .form-error",
                polopoly.util.querystringValue("login_formerror"));

            if (polopoly.util.querystringValue("login_formerror")) {
                jQuery('#loginForm').show();
            }
        },
      _trySetError: function(selector, errorKey) {
            if (!errorKey) return;
            var err = p_l_i18n[this._login[errorKey]] || p_l_i18n[this._login["default"]];
            jQuery(selector).css("display","block").text(err);
        },

        _tryAutoFill: function() {
            if (jQuery(".loginname").val() == "" && jQuery(".password").val() == "") {
                jQuery(".loginname").val(this.defLogin);
                jQuery(".password").val(this.defLogin);
                document.__ppUseDefLogin = true;
            }
        }
 };

/* = LRU Map ================================================================ */
/* Not really a map at all. Based on the assumption that lists in the cookies
 * in general are small (eg less than 10 items), so that mantaining them as
 * lists are cheap.                                                           */
 polopoly.util.lrumap = function(lrumap, maxsize){
     var myMap;
     if (lrumap) {
         myMap = lrumap;
     } else {
         myMap = [];
     }
     if (!maxsize) {
         maxsize = 10;
     }

     return {
        map: myMap,
        size: maxsize,
        get: function(id) {
            var key = this._findId(id);
            if (key) {
                var retval = this.map[key][1];
                this._setLeader(key);
                return retval;
            }
        },
        rawMap: function() {
            return this.map;
        },
        put: function(id, val) {
            var key = this._findId(id);
            if (key) {
                this.map[key] = [id, val];
            //this._setLeader(key);
            } else {
                this.map.unshift([id,val]);
            }
            if (this.map.length > this.size) {
                this.map.splice(this.size);
            }
        },
        remove: function(id) {
            var key = this._findId(id);
            if (key) {
                this.map.splice(key, 1);
            }
        },
        _findId: function(id) {
            for (key in this.map) {
                if (this.map[key][0] == id) {
                    return key;
                }
            }
        },
        _setLeader: function(id) {
            var leader = this.map[key];
            this.map.splice(key, 1);
            this.map.unshift(leader);
        }
    };
 };

/* = My Newslist ============================================================ */
/* serviceInstanceId is content id of News list element, e.g. 7.100.          */
/* Assumes the element is contained in a div with id newsList_7_100.          */

polopoly.Newslist = function(serviceDefinitionId, serviceInstanceId, allListIds,
                              defaultListIds, editOnlyWhenLoggedIn)
{

    var sdid      = serviceDefinitionId,
        siid      = serviceInstanceId,
        mnlDivId  = "newsList_" + siid.replace("\.","_"),
        jqMnl     = jQuery("#" + mnlDivId),
        jqEditMnl = jQuery("#edit_" + mnlDivId);

    /**
     * Loads settings for this My news list service. If the element is configured
     * to only support customization for logged in users,  it gets the data via the
     * "data" cookie. If this is false, it pulls from the client "cata" cookie instead.
     *
     * In general, sessionServiceSettings should be used for settings you want cleared
     * when the user ends his session,  data that while not secret aren't really
     * public either.
     *
     * This is also used in user blogs to keep a list of the users blogs while the user
     * is logged in, for example.
     *
     * serviceSettings should be used for data which there are no problems in persisting,
     * and doesn't depend on a user being logged in or logged outs.
     */
    var savedSelection = function() {
        if (editOnlyWhenLoggedIn) {
            return polopoly.user.getSessionServiceSettings(sdid, siid);
        } else {
            return polopoly.user.getServiceSettings(sdid, siid);
        }
    };

    /**
     * Returns the saved selection if it exits. If not, it picks the default selection for
     * the elment, as given by the constructor.
     */
    var selection = function() {
        return savedSelection() || defaultListIds;
    };

    /**
     * @saveInSession   If to persist in cata or data cookie
     *
     * Persists data about the users selection  in either service settings (the data cookie)
     * or session service settings (the cata cookie).
     */
    var saveChecked = function(saveInSession) {
        var values = jqMnl.find("input:checkbox:checked").map(function() { return this.value }).get();
        if (saveInSession) {
            polopoly.user.setSessionServiceSettings(serviceDefinitionId, serviceInstanceId, values);
        } else {
            polopoly.user.setServiceSettings(serviceDefinitionId, serviceInstanceId, values);
        }
    };

    /**
     *  This function is used by the closure below
     */
    var populateMnlList = function(buffer, index, count, jqList) {
                return function(data) {
                        buffer[index] = data;
                        var isComplete = true;
                        for (var i = 0; i < count; i++) {
                                if (!buffer[i]) {
                                    isComplete = false;
                                }
                        }
                        if (isComplete) {
                            jqList.html(buffer.join(""));
                        }
        };
    };

    /**
     * The actual newslist object
     */
    var mnlObj = {

        /**
         * Saves user preferences to cookie. This cookie will never expire.
         */
        storeInCookie: function() {
            saveChecked(false);
            this.updateList();
            jqMnl.find(".settings").hide(100);
        },

        /**
         * Save user perferences to a session cookie, and persist them on the user
         * server wise. Only makes sense if the user is logged in, which is why the
         * save button is only enabled when a user is logged in for server based
         * persistence.
         */
        storeOnServer: function() {
            jQuery("#error_" + mnlDivId).hide();
            saveChecked(true);
            this.updateList();

            polopoly.user.persistUserServiceData(sdid, siid,
                    (function(mnlDivId) {
                        return function(xmlHttpRequest, textStatus, errorThrown) {
                            jQuery("#error_" + mnlDivId + "").show();
                        };
                    })(mnlDivId),
                     (function(mnlDivId) {
                        return function(textStatus, data) {
                             jQuery("#" + mnlDivId + " .settings").hide(100);
                        };
                     })(mnlDivId)
                );
        },

        /**
         * Updates the news list by getting selection and then polling the news list
         * element for its various queues. Does one poll at a time to improve
         * cacheability.
         */
        updateList: function() {
                var selectedIds = selection(),
                    jqList = jqMnl.find(".lists");

                // If empty, clear and return
                if (!selectedIds.length) {
                    jqList.html("");
                    return;
                }

                // Only fetch lists that are still available
                var idsToFetch = [];
                for (var i = 0; i< selectedIds.length; i++) {
                    if  (jQuery.inArray(selectedIds[i], allListIds) > -1) {
                       idsToFetch.push(selectedIds[i]);
               }
                }

                var buffer = [], count = idsToFetch.length;
            for (i = 0; i < idsToFetch.length; i++) {
                var listId = idsToFetch[i];
                // Populating a buffer on return of ajax requests to ensure
                // correct order and to avoid flickering
                jQuery.get( "/cmlink/" + siid, {"topic": listId, "mode": "ajax"},
                            populateMnlList(buffer, i, count, jqList),
                            "html");
            };
        }
    };

    /** Initialization code */
    var selectedIds = selection();

    // Create the slide effect (on edit and save buttons)
    jqEditMnl.find(".button").click(function () { jqMnl.find(".settings").slideToggle(100); });
    jQuery("#" + mnlDivId + " :checkbox").removeAttr("checked");
    var siidId = "#c" + serviceInstanceId.replace("\.", "_") + "-";
    for (var index = 0; index < selectedIds.length; index++) {
        var checkedValue = siidId + selectedIds[index].replace("\.", "_");
        jqMnl.find(checkedValue).attr("checked", "checked");
    }

    // Show edit button.
    if (editOnlyWhenLoggedIn && polopoly.user.isLoggedIn() || !editOnlyWhenLoggedIn) {
        jqEditMnl.show();
    }

    // Different funcions if logged in or not.
    var saveButton = jqMnl.find("input.save");
    saveButton.click(editOnlyWhenLoggedIn ?
            function() { mnlObj.storeOnServer.apply(mnlObj); } :
            function() { mnlObj.storeInCookie.apply(mnlObj); }
    );

    mnlObj.updateList.apply(mnlObj);
    return mnlObj;
};

 /* = Password Meter ========================================================= */

polopoly.passwordMeter = {
    _colors : [ "#FF0000", // Very Weak
                "#FF0000", // Weak
                "#FFCC00", // Medium
                "#00CC00", // Strong
                "#00FF00"  // Very Strong
              ],
    create : function(input, strength) {
        var innerStrength = "is" + Math.ceil(Math.random() * 3000);
        jQuery(strength).html(
        "<div id='" + innerStrength + "' style='height: 100%;'></div>");

        var width = jQuery(strength).width();

        // Function to run when password is updated
        var checkPasswordFn = function m(p,c) { return function(e) {
            var text = jQuery(input).val();
            var score = p(text);
            var newWidth = score > 0 ? Math.ceil(score / 4.0 * width)
                    : (text.length > 0 ? 0.01 * width : 0);
            jQuery("#" + innerStrength).css("width", newWidth + "px");
            jQuery("#" + innerStrength).css("background-color", c[score]);
        };}(this._testPassword, this._colors);

        jQuery(input).keyup(checkPasswordFn);
    },

    /*
     * Copyright (c) 2006 Steve Moitozo <god at zilla dot us>
     *
     * Permission is hereby granted, free of charge, to any person obtaining a
     * copy of this software and associated documentation files (the
     * "Software"), to deal in the Software without restriction, including
     * without limitation the rights to use, copy, modify, merge, publish,
     * distribute, sublicense, and/or sell copies of the Software, and to permit
     * persons to whom the Software is furnished to do so, subject to the
     * following conditions:
     *
     * The above copyright notice and this permission notice shall be included
     * in all copies or substantial portions of the Software.
     */
    _testPassword : function(passwd) {
        var score = 0;
        if (passwd.length < 5) {
            score += 3;
        } else if (passwd.length > 4 && passwd.length < 8) {
            score += 6;
        } else if (passwd.length > 7 && passwd.length < 16) {
            score += 12;
        } else if (passwd.length > 15) {
            score += 18;
        }
        if (passwd.match(/[a-z]/)) {
            score += 1;
        }
        if (passwd.match(/[A-Z]/)) {
            score += 5;
        }
        if (passwd.match(/\d+/)) {
            score += 5;
        }
        if (passwd.match(/(.*[0-9].*[0-9].*[0-9])/)) {
            score += 5;
        }
        if (passwd.match(/.[!,@,#,$,%,^,&,*,?,_,~]/)) {
            score += 5;
        }
        if (passwd
                .match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)) {
            score += 5;
        }
        if (passwd.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)) {
            score += 2;
        }
        if (passwd.match(/([a-zA-Z])/) && passwd.match(/([0-9])/)) {
            score += 2;
        }
        if (passwd
                .match(/([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~])|([!,@,#,$,%,^,&,*,?,_,~].*[a-zA-Z0-9])/)) {
            score += 2;
        }
        // Scoring updated to give nicer metric.
        if (score < 5) {
            return 0;
        } else if (score >= 5 && score < 15) {
            return 1;
        } else if (score >= 15 && score < 30) {
            return 2;
        } else if (score >= 30 && score < 35) {
            return 3;
        } else {
            return 4;
        }
    }
};

jQuery().ready(function() {
  return function(namespaces) {
    for (var i = 0; i < namespaces.length; i++) {
      for (key in namespaces[i]) {
        var obj = namespaces[i][key];
        if (typeof(obj["init"]) == "function") {
            obj.init();
        }
      }
    }
  }([polopoly, polopoly.service]);
});
