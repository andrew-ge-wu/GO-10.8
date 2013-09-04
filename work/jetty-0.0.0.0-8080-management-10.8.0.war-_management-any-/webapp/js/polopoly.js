openTab = function(hash) {
	$(".body .tab").hide();
	$(".header ul li").removeClass("selected");
	$(".header ul li a[href='"+hash+"']").parent().addClass("selected");
	$(hash).show();
}

/* Samples a Gaussian from -interval <= x < interval.
 * Will give crappy measurements for sigma < 9 due to crappy
 * precision.
 */
_dbg_makeGaussian = function(mhu, sigma, interval) {
	var a = 1/(sigma*Math.sqrt(2*Math.PI));
	var denom = 2*sigma*sigma;
	var result = [];
	for (x = -interval; x < interval; x++) {
		var nom = (x-mhu)*(x-mhu);
		var y = a*Math.exp(-(nom/(denom)));
		result.push(y);
	}
	return result;
}

startLucenePoll = function(applicationName, rmiSearchClientModuleName,
		                   rmiSearchClientComponentName, indexName) {
	if( window.lucenePollRunning == true ){
		return;
	}
	window.lucenePollRunning = true;
	if ($("#lucene-head").css("display") == "none") {
		$("#lucene-head").slideDown(50);
	}
	$.timer(1000, function(timer) {
		if (!window.lucenePollRunning) {
			timer.stop();
		} else {
			var url = "latestsearch.jsp?a=" + applicationName + "&rm="
			          + rmiSearchClientModuleName + "&rc=" + rmiSearchClientComponentName
			          + "&i=" + indexName;
			if (typeof(window.previousRequestTotalTime) != 'undefined') {
				url += "&pt=" + window.previousRequestTotalTime;
			}
			$.getJSON(url, 
				function(data) { 
					if (typeof(data.query) != 'undefined') {

						window.previousRequestTotalTime = data.totalTime;
						analyzer.updateSeries(data.query, data.time);
					}
				});
		}
	});
}

stopLucenePoll = function() {
	window.lucenePollRunning = false;
}

fixQueryClasses = function() {
	$(".queryClass").map(function() {
		var text = $(this).text();
		var fulltext = text;
		var fullId = $.md5("!!!!!!!" + text);
		if (text.length > 20) {
			text = text.substr(0,17) + "...";
		}
		$(this).text(text);

		$(this).parent().after("<tr id='"+fullId+"' class='fullVal'><td colspan='7'>"+fulltext+"</td></tr>");
		$(this).click(function() { $("#lucene-indices-info tr.fullVal").hide(); 
		$("#" + fullId).show(); })
	});
	$("#lucene-indices-info tr.fullVal").hide(); 
}

$().ready(function() { 
	/* Early bound initialisation (pre-ready) */
	openTab(window.location.hash || "#cache-settings-tab");
		
	/* Add tab changing actions */
	$(".header ul li a").map(function() { 
		$(this).click(
   		function() {
     		var id = $(this).attr("href");
			openTab(id);
   		})
  	})

	/* Fix sortable tables */
	$("table.sortable").tablesorter({cssHeader: "table-header", 
									 cssAsc: "table-header-asc",
									 cssDesc: "table-header-desc",
									 widgets: ['zebra']});

	/* Create an analyzer */
	analyzer = new SeriesAnalyzer("#lucene-host");
	analyzer.remakeTotal();
	
	stalkerList = new TimeTreeDisplay("#stalker-host");
	
	$('#requestwatcher-list .treeview h3').live('click', function() { 
        $(this).parent().children(".stackTree").slideToggle(150);
    });
	
	$('#requestwatcher-list .treeview div.stackHeader').live('click', function() { 
        $(this).parent().children("pre").slideToggle(150);
    });
	
	fixQueryClasses();
	
	$(".button").map(function() { this.onselectstart = function() { return false; }  });
	
});

startThreadAnalysis = function(interval, identifier) {
	if( window.threadAnalysisRunning == true ){
		return;
	}
	window.threadAnalysisRunning = true;
	
	$.timer((1000 * interval), function(timer) {
		if (!window.threadAnalysisRunning) {
			timer.stop();
		} else {
			var url = "stuckthreads?uuid=" + identifier;
			
			$.getJSON(url, function(data) {
				stalkerList.populate(data);
			});
		}
	});
}

stopThreadAnalysis = function() {
	window.threadAnalysisRunning = false;
}

startCacheMissAnalysis = function(interval, application, module, component, memoryCacheField, persistentCacheField) {
    if( window.cacheMissAnalysisRunning == true ){
        return;
    }
    window.cacheMissAnalysisRunning = true;
    var url = "misses?a=" + application + "&cm=" + module + "&c=" + component;
    $.getJSON(url, function(data) {
        var memoryCacheMissRate = (data.MemoryContentDataMisses * 1000) / data.MilliSecondsElapsed;
        var persistentCacheMissRate = (data.PersistentContentDataMisses * 1000) / data.MilliSecondsElapsed;
        memoryCacheField.text(isNaN(memoryCacheMissRate) ? "N/A (Waiting for data...)" : memoryCacheMissRate);
        persistentCacheField.text(isNaN(persistentCacheMissRate) ? "N/A (Waiting for data...)" : persistentCacheMissRate);
    });

    $.timer((10000 * interval), function(timer) {
        if (!window.cacheMissAnalysisRunning) {
            timer.stop();
        } else {
            $.getJSON(url, function(data) {
                var memoryCacheMissRate = (data.MemoryContentDataMisses * 1000) / data.MilliSecondsElapsed;
                var persistentCacheMissRate = (data.PersistentContentDataMisses * 1000) / data.MilliSecondsElapsed;
                memoryCacheField.text(isNaN(memoryCacheMissRate) ? "N/A (Waiting for data...)" : memoryCacheMissRate);
                persistentCacheField.text(isNaN(persistentCacheMissRate) ? "N/A (Waiting for data...)" : persistentCacheMissRate);
            });
        }
    });
}

stopCacheMissAnalysis = function() {
    window.cacheMissAnalysisRunning = false;
}

var requestWatcher = (function($) {
    var _application = "front";
    var _logTimer = undefined;
    var _countTimer = undefined;
    var _watchType = "latest";
    function _errorHandler() {
    }
    function _baseUrl() {
        return "requestwatcher?a="+_application;
    }
    function _get(url, data, callback) {
        _errorHandler('');
        $.getJSON(url, data, function(data) {
           if (data.errorCode && data.errorCode != 'OK') {
               _errorHandler(data.errorMessage);
           } else if (callback) {
               callback(data);
           }
        });
    }
    function _updateData(data) {
        var toBeSaved = {};
        
        var renderStackTraces = function (stackTraces, addAt) {
            var result = "";
            for(i = addAt ; i < stackTraces.length; i++) {
                result += "<div class='stackRoot'><div class='stackHeader'>";
                result += stackTraces[i]['stacktrace'].split("\n")[0];
                result += " (last seen at <span class='stackTraceLastSeen'>" + stackTraces[i]['timestamp'] + "</span> into request)";
                result += "</div><pre>";
                result += stackTraces[i]['stacktrace'];
                result += "</pre></div>";
            }
            return result;
        };
        
        $.each(data, function(index, thread) {
            var id = "thread-" + thread.uuid;
            
            if($('#' + id).length == 0) {
                var result = "<div id='" + id + "' class='treeview";
                if(!thread.finished) {
                    result += ' activeRequest';
                }
                result += "'><h3>";
                result += " <b>Request:</b> " + thread.request;
                result += " <b>Thread:</b> " + thread.threadName; 
                result += " <b>Duration:</b> <span id='duration-"+id+"'>" + thread.duration + "</span>";
                result += "<div class='pinner'><img id='pinnedIcon' src='img/pin.png'/></div>";
                result += "<div class='loaderIcon'";
                if(thread.finished) {
                    result += ' style="display: none;" ';
                }
                result += "><img class='loaderIcon' src='img/ajax-loader.gif'/></div>";
                result += "</h3><div class='stackTree'>";
                result += renderStackTraces(thread.stackTraces, 0);
                result += "</div>";
                result += "</div>";
                $('#requestwatcher-list').append(result);
            } else {
                if(thread.finished) {
                    $('#' + id).removeClass('activeRequest');
                    $('#' + id + ' div.loaderIcon').hide();
                } else {
                    $('#' + id).addClass('activeRequest');
                    $('#' + id + ' div.loaderIcon').show();
                }
                $('#' + id + ' div.stackTree').append(renderStackTraces(thread.stackTraces, $('#' + id + ' div.stackTree div.stackRoot').size()));
                $('#' + id + ' div.stackTree div.stackRoot:last span.stackTraceLastSeen').text(thread.stackTraces[thread.stackTraces.length-1]['timestamp']);
                $('#duration-' + id).text(thread.duration);
            }
            toBeSaved[id] = true;
        });
        $('#requestwatcher-list div.treeview').each(function(index, node) {
            var id = $(node).attr('id');
            if (toBeSaved[id]) {
                //pass
            } else if ($(node).find("div.pinner").hasClass("pinned")) {
                $(node).find('h3 div.loaderIcon').hide();
                $(node).removeClass('activeRequest');
            } else {
                $(node).remove();
            }
        });
    }
    function _updateCount(data) {
        $("#requestwatcher-requestcount").text(data.RequestCount.slow + " slow out of " + data.RequestCount.total);
    }
    function _setConfiguration(data, callback) {
        _get(_baseUrl()+"&o=setConfiguration", data, callback);
    }
    return {
        setApplication: function(application) {
            _application = application;
        },
        setErrorHandler: function(errorHandler) {
            _errorHandler = errorHandler;
        },
        setWatchType: function(newType) {
            _watchType = newType;
        },
        watch: function() {
            function updateData() {
                _get(_baseUrl(), {"o": "getLoggedRequests", "t": _watchType }, _updateData);
            }
            updateData();
            _logTimer = $.timer(5000, updateData);
            function updateCount() {
                _get(_baseUrl(), {"o": "getRequestCount"}, _updateCount);
            }
            updateCount();
            _countTimer = $.timer(3000, updateCount);
        },
        unwatch: function() {
            _logTimer.stop();
            _countTimer.stop();
            _logTimer = undefined;
            _countTimer = undefined;
        },
        isWatching: function() {
            return _logTimer !== undefined;
        },
        setConfiguration: _setConfiguration,
        getConfiguration: function(callback) {
            _get(_baseUrl(), {"o": "getConfiguration"}, callback);
        },
        getApplications: function(callback) {
            _get(_baseUrl(), {"o": "getApplications"}, callback);
        },
        setRequestWatching: function(watchRequests) {
            _setConfiguration({"WatchRequests": watchRequests}, function() {
                if (watchRequests) {
                    _errorHandler("Filter now watching requests");
                } else {
                    _errorHandler("Filter no longer watching requests");
                }
            });
        },
        clear: function(currentLogSize) {
            function setSize(size, callback) {
                _setConfiguration({SavedLogSize: size}, callback);
            }
            setSize(0, function() { setSize(currentLogSize); });
            $('#requestwatcher-list').html("");
        }
    }
})(jQuery);
