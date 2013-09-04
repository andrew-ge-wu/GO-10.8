<%@page import="java.util.List,
                com.polopoly.html.CharConv,                
                com.polopoly.management.troubleshooting.Troubleshooter,
                com.polopoly.management.troubleshooting.Status,
                com.polopoly.management.troubleshooting.cmclient.cache.MemoryCacheUtilization,
                com.polopoly.management.troubleshooting.cmclient.cache.PersistentCacheLocation,
                com.polopoly.management.troubleshooting.search.SearchUsage,
	            javax.management.InstanceNotFoundException,
                java.util.Iterator"
         contentType="text/html; charset=UTF-8"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%
String applicationName = request.getParameter("a");
if (applicationName == null) {
    applicationName = "front";
}
String cmClientModuleName = request.getParameter("cm");
if (cmClientModuleName == null) {
    cmClientModuleName = "cm";
}
String cmClientComponentName = request.getParameter("c");
if (cmClientComponentName == null) {
    cmClientComponentName = "client";
}
String rmiSearchClientModuleName = request.getParameter("rm");
if (rmiSearchClientModuleName == null) {
    rmiSearchClientModuleName = "search";
}
String rmiSearchClientComponentName = request.getParameter("rc");
if (rmiSearchClientComponentName == null) {
    rmiSearchClientComponentName = "rmiClient";
}
String indexName = request.getParameter("i");
if (indexName == null) {
    indexName = "PublicIndex";
}

Troubleshooter troubleshooter =
    new Troubleshooter(applicationName, cmClientModuleName, cmClientComponentName,
                       rmiSearchClientModuleName, rmiSearchClientComponentName, indexName);

List cacheUtilizationList =
    troubleshooter.getMemoryCacheUtilizationList();

List cacheLocationList =
    troubleshooter.getPersistentCacheLocationList();

List searchUsageList =
    troubleshooter.getSearchUsageList();
%>

<html>
<head>
  <title>Polopoly Troubleshooter</title>
  <meta http-equiv="Cache-Control" content="no-cache">
  <meta http-equiv="Pragma" content="no-cache">
  <meta http-equiv="Expires" content="0">
  <link rel="stylesheet" href="css/polopoly.css" type="text/css" />
  <script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
  <script type="text/javascript" src="js/tablesorter.js"></script>
  <script type="text/javascript" src="js/jquery-ui-1.7.2.custom.min.js"></script>
  <script type="text/javascript" src="js/jquery.timer.js"></script>
  <script type="text/javascript" src="js/jquery.md5.js"></script>
  <script type="text/javascript" src="js/series.js"></script>
  <script type="text/javascript" src="js/timetree.js"></script>
  <script type="text/javascript" src="js/polopoly.js"></script>

  <script type="text/javascript">
  $().ready(function() {
    startCacheMissAnalysis(1, "<%= applicationName %>", "<%= cmClientModuleName %>", "<%= cmClientComponentName %>", $("#memory-cache"), $("#persistent-cache"));
	$("#lucene-head").hide();
	/* Bind buttons to help with analysis */
	$("#start-search").click(function() {
		$(this).addClass("disabled");
		$("#stop-search").removeClass("disabled");
		$("#lucene-start-panel").addClass("active");
		startLucenePoll("<%= applicationName %>","<%= rmiSearchClientModuleName %>",
			            "<%= rmiSearchClientComponentName %>", "<%= indexName %>"); 
	});
	$("#stop-search").click(function() {
		stopLucenePoll();
		$(this).addClass("disabled");
		$("#lucene-start-panel").removeClass("active");
		$("#start-search").removeClass("disabled");
	});
	$("#start-thread").click(function() {
		$(this).addClass("disabled");
		$("#stop-thread").removeClass("disabled");
		$("#thread-start-panel").addClass("active");
		startThreadAnalysis($("#thread-interval").val(), '<%= Troubleshooter.getUniqueIdentifier() %>');
	});
	$("#stop-thread").click(function() {
		stopThreadAnalysis();
		$(this).addClass("disabled");
		$("#thread-start-panel").removeClass("active");
		$("#start-thread").removeClass("disabled");
	});
	
	/* Request watcher */
    $("#requestwatcher-trigger").click(function() {
        var trigger = $("#requestwatcher-trigger");
        if(trigger.hasClass("triggered")) {
            trigger.removeClass("triggered");
            $("#requestwatcher-panel").removeClass("active");
            $("#requestwatcher-panel").addClass("disabled");
            requestWatcher.unwatch();
        } else {
            trigger.addClass("triggered");
            $("#requestwatcher-panel").addClass("active");
            $("#requestwatcher-panel").removeClass("disabled");
            if($("#requestwatcher-savedlogsize").val() == "0") {
                $("#requestwatcher-savedlogsize").val("10");
                requestWatcher.setConfiguration({SavedLogSize: $("#requestwatcher-savedlogsize").val()});
            }
            requestWatcher.watch();
        }
    });

    $("#requestwatcher-type-longest").click(function() {
       if(!$(this).hasClass("disabled")) {
           $(this).addClass("disabled");
        	$("#requestwatcher-type-latest").removeClass("disabled");
        	requestWatcher.setWatchType("longest");
        	$("#request-type-label").text("longest running");
        	if(requestWatcher.isWatching) {
        	    requestWatcher.unwatch();
        	    requestWatcher.watch();
        	}
    	} 
    });
    
    $("#requestwatcher-type-latest").click(function() {
        if(!$(this).hasClass("disabled")) {
            $(this).addClass("disabled");
         	$("#requestwatcher-type-longest").removeClass("disabled");
         	requestWatcher.setWatchType("latest");
         	$("#request-type-label").text("latest");
     	} 
     });
    
    
	$("#requestwatcher-clear").click(function() {
		$(this).addClass("disabled");
		requestWatcher.clear($('#requestwatcher-savedlogsize').val());
	});
	
	$("#requestwatcher-save").click(function() {
		$(this).addClass("disabled");
		requestWatcher.setConfiguration({MaxTimeMillis: $("#requestwatcher-maxtimemillis").val(), 
                                         SavedLogSize: $("#requestwatcher-savedlogsize").val()});
    });

    $("#requestwatcher-list .treeview div.pinner").live("click", function(event) {
        $(this).toggleClass("pinned");
        $(this).parents('.treeview').toggleClass("pinned");
        return false;
    });
    $("#requestwatcher-maxtimemillis").keypress(function(event) {if(event.keyCode == '13') $("#requestwatcher-save").trigger('click');});
    $("#requestwatcher-savedlogsize").keypress(function(event) {if(event.keyCode == '13') $("#requestwatcher-save").trigger('click');});

    var application = "<%=applicationName%>";
    requestWatcher.setApplication(application);
    requestWatcher.setErrorHandler(function(message) { $("#requestwatcher-error").text(message); });
    requestWatcher.getConfiguration(function(data) {
        $("#requestwatcher-maxtimemillis").val(data.MaxTimeMillis);
        $("#requestwatcher-savedlogsize").val(data.SavedLogSize);
        var toggleFilterActivation = function() {}
        if(data.AllowGuiActivation) {
             var button = '<p><a id="requestwatcher-filter-trigger" class="button';
             if(data.WatchRequests) {
                 button += ' triggered';
             }
             button +='">Filter active</a></p>';
             $("#requestwatch-filter-status").append(button);
             $('#requestwatcher-filter-trigger').click(function() {
                 if($('#requestwatcher-filter-trigger').hasClass('triggered')) {
                     requestWatcher.setRequestWatching(false);
                     $('#requestwatcher-filter-trigger').removeClass('triggered');
                 } else {
                     requestWatcher.setRequestWatching(true);
                     $('#requestwatcher-filter-trigger').addClass('triggered');
                 }
             });
        } else if (data.WatchRequests) {
             $("#requestwatch-filter-status").text("Filter is enabled");
        } else {
             $("#requestwatch-filter-status").text("Filter is disabled");
        }
        if(data.WatcherThreadError) {
             $("#requestwatcher-error").text("Warning, requests can not be watched since the request watcher thread failed to start with message '" + data.WatcherThreadError + "'"); 
        }
    });
    requestWatcher.getApplications(function (data) {
        $.each(data, function(index, app) {
            if (application == app) {
              $("#requestwatcher-applications").append('<div>'+app+'</div>');
            } else {
              var link = "troubleshooter?a="+app+"#requestwatcher-tab";
              $("#requestwatcher-applications").append('<div><a href="'+link+'">'+app+'</a></div>');
            }
        });
    });
  });
  
  </script>

</head>
<body>
<div class="padder">
<div class="bigframe">
  <div class="header">
    <img src="img/logo.gif" title="Polopoly Troubleshooter" />
    <ul>
      <!--<li class="selected"><a href="#general-config-tab" id="general-config">General configuration</a></li>-->
      <li><a href="#cache-settings-tab" id="cache-settings">System caches</a></li>
      <li><a href="#lucene-query-analyser-tab" id="lucene-query-analyser">Lucene queries</a></li>
      <li><a href="#requestwatcher-tab" id="requestwatcher">Request Watcher</a></li>
      <li><a href="#find-stuck-threads-tab" id="find-stuck-threads">Find stuck threads</a></li>
      <li><a href="#configuration-tab" id="configuration">Configuration</a></li>
    </ul>
  </div>
  <div class="outerbody">
  <div class="body">

   <!-- CACHE SETTINGS -->
  <div class="tab" id="cache-settings-tab">
    <div class="big panel">
      <h2>Cache Overview</h2>
        <table cellspacing="0" class="sortable">
            <thead>
            <tr>
                <th class="primaryKey">Cache</th>
                <th>Hit Ratio (%)</th>
                <th>Size</th>
                <th>Max Size</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <%
                Iterator iterator = cacheUtilizationList.iterator();
                int row = 0;
                while (iterator.hasNext()) {
                    MemoryCacheUtilization mcu = (MemoryCacheUtilization) iterator.next();
            %>
                <tr class="<%=((row % 2 == 0) ? "even" : "odd") +
                              ((mcu.getStatus() == Status.OK) ? "": " error") %>">
                    <th><%= toString(mcu.getName()) %></th>
                    <td><%= toString(mcu.getHitPercentage()) %></td> 
                    <td><%= toString(mcu.getSize()) %></td>
                    <td><%= toString(mcu.getMaxSize()) %></td>
                    <td><%= toString(mcu.getStatus(), "Hit rate is too low") %></td>
                </tr>
                <%
                    row++;
                }
                %>
            </tbody>
        </table>
    </div>
    
    <div class="big panel">
      <h2>Persistent Cache Location</h2>
        <table cellspacing="0">
            <thead>
            <tr>
                <th class="primaryKey">Cache</th>
                <th>Path</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <%
                iterator = cacheLocationList.iterator();
                row = 0;
                while (iterator.hasNext()) {
                    PersistentCacheLocation pcl = (PersistentCacheLocation) iterator.next();
            %>
                <tr class="<%= ((row % 2 == 0) ? "even" : "odd") + ((pcl.getStatus() == Status.OK) ? "": " error") %>">
                    <th><%= toString(pcl.getName()) %></th>
                    <td><%= toString(pcl.getPath()) %></td>
                    <td><%= toString(pcl.getStatus(), "Move cache from temporary directory") %></td>
                </tr>
             <% 
                    row++;
                }
             %>
            </tbody>
        </table>
    </div>

    <div class="big panel">
      <h2>Cache miss rates for {a=<%=applicationName %>, cm=<%=cmClientModuleName %>, c=<%=cmClientComponentName %>}</h2>
        <table cellspacing="0">
            <thead>
            <tr>
                <th class="primaryKey">Cache</th>
                <th>Miss rate</th>
            </tr>
            </thead>
            <tbody>
                <tr class="odd">
                    <th>Memory Cache</th>
                    <td id="memory-cache">0</td>
                </tr>
                <tr class="even">
                    <th>Persistent Cache</th>
                    <td id="persistent-cache">0</td>
                </tr>
            </tbody>
        </table>
    </div>
  </div>

	<!-- LUCENE QUERY ANALYSER -->
	<div class="tab" id="lucene-query-analyser-tab">
		    <div class="small panel">
		      	 <h2>Search Usage - <%= indexName %></h2>
			        <table cellspacing="0" id="lucene-indices-info">
			            <thead>
			            <tr>
			                <th class="primaryKey">Type</th>
			                <th>Query</th>
			                <th>Search Time (ms)</th>
			                <th>Hit Size</th>
			                <th>Limit</th>
			                <th>Offset</th>
			                <th>Status</th>
			            </tr>
			            </thead>
			            <tbody>
			            <%
			                row = 0;
                            iterator = searchUsageList.iterator();
                            while (iterator.hasNext()) {
                                SearchUsage su = (SearchUsage) iterator.next();
                        %>
			                <tr class="<%= ((row % 2 == 0) ? "even" : "odd") +
			                              ((su.getStatus() == Status.OK) ? "": " error") %>">
			                    
			                    <th><%= toString(su.getName()) %></th>
			                    <td class='queryClass'><%= toString(su.getQuery()) %></td>
			                    <td><%= toString(su.getSearchTime())%></td>
			                    <td><%= toString(su.getHitSize()) %></td>
			                    <td><%= toString(su.getLimit()) %></td>
			                    <td><%= toString(su.getOffset()) %></td>
			                    <td><%= toString(su.getStatus(), "Analyze this query") %></td>
			                    </tr>
			             <%
			                    row++;
			                }

			             %>
			            </tbody>
			       </table>
		    </div>
			<div class="small">

		<div class="small panel">
			<h2>Lucene query analyser</h2>
			<div class="panelbody" id="lucene-start-panel">
				When active, the Lucene query analyzer samples the mbeans for the various 
					indexes and presents them graphically.
		      <div>
				<div id="start-search" class="button">Start</div>
				<div id="stop-search" class="disabled button">Stop</div>
			  </div>
		    </div>
		</div>
		<div class="small panel" id="lucene-head">
			<h2>Results</h2>
			<div id="lucene-host">
			</div>
		</div>
	</div>
</div>

<!-- REQUEST WATCHER -->
<div class="tab" id="requestwatcher-tab">
    <div class="small">
	<div class="small panel" id="requestwatcher-panel">
    	<h2>Request watcher <span id="requestwatcher-requestcount"></span></h2>
		<p id="requestwatcher-information">Show  
			<span id="requestwatcher-type-latest" class="disabled button">latest</span>
			<span id="requestwatcher-type-longest" class="button">longest running</span>
			filtered requests
		 </p>
        <p><a id="requestwatcher-trigger" class="button">Capture</a>
		<a id="requestwatcher-clear" class="button">Clear log</a></p>
    </div>
    </div>
    <div class="small">
      <div class="small panel configuration">
	   	<h2>Configuration</h2>
        <div style="position:relative">
          <div class="configElement">
            <p>Slow request cutoff (ms)</p>
            <p><input id="requestwatcher-maxtimemillis" type="text" value="unknown" /></p>  
          </div>
          <div class="configElement">
            <p>Log size</p>
            <p><input id="requestwatcher-savedlogsize" type="text" value="unknown" /></p> 
          </div>
          <div>
          		<a id="requestwatcher-save" class="button">Update</a>
            	<div style="float: right" id="requestwatch-filter-status"></div>
          </div>
          <div id="requestwatcher-applications" style="display:inline-block; right: 5px; top: 5px; position: absolute">
          </div>
        </div>
      </div>
    </div>
    <div class="big panel">
    <span id="requestwatcher-error"></span>
    </div>
    <div class="big panel">
    	<h2>Request Watcher Log (showing <span id="request-type-label">latest</span> requests)</h2>
		<div class="timetree-host"  id="requestwatcher-list">
		</div>
    </div>
</div>
  <!-- FIND STUCK THREADS -->
    <div class="tab" id="find-stuck-threads-tab">
        <div class="small panel">
            <h2>Thread dump</h2>
            <div class="panelbody" id="thread-dump-panel">
			<p>Take a thread dump of currently running threads by clicking the button below.
			The dumps is plain text and opens in a new window.</p>
            <p><a href="dumper" target="_blank" class="button">Dump threads</a></p>
            </div>
        </div>
		<div class="small">
        <div class="small panel">
            <h2>Continous analysis of long running threads</h2>
            <div class="panelbody" id="thread-start-panel">
                When active, the thread analyzer will continously look for long running threads.
				In each sample sequence, the analyzer will print the threads that has been active since the previous
				dump. So if thread <em>T</em> has the same stack in <em>t<sub>i</sub></em> and <em>t<sub>i+1</sub></em> it will be printed on <em>t<sub>i+1</sub></em>.<br /><br />
                <div>
                    <label for="thread-interval">Sample interval (s)</label>
                    <select id="thread-interval">
                        <option>1</option>
                        <option>2</option>
                        <option>3</option>
                        <option>4</option>
                        <option selected="selected">5</option>
                        <option>10</option>
                        <option>30</option>
                        <option>60</option>
                    </select>
                    <div>
                    	<div id="start-thread" class="button">Start</div>
                    	<div id="stop-thread" class="disabled button">Stop</div>
					</div>
                </div>
            </div>
        </div>
		</div>
        <div class="big panel">
            <h2>Long running threads output</h2>
			<div class="timetree-host" id="stalker-host">
			
			</div>
         </div>
	</div>
    
    <div class="tab" id="configuration-tab">
<div class="big panel">
  
  
<h2>Configuration</h2>

<h3>Current configuration</h3>
<table class="config">
  <thead>
    <tr>
        <th>Description</th>
        <th>Current value</th>
        <th>How to configure</th>
    </tr>
  </thead>
  <tbody>
    <tr class="even">
        <td>Application name</td>
        <td><%=applicationName %></td>
        <td>Set request parameter 'a'</td>
    </tr>
    <tr>
        <td>CM client module name</td>
        <td><%=cmClientModuleName %></td>
        <td>Set request parameter 'cm'</td>
    </tr>
    <tr class="even">
        <td>CM client component name</td>
        <td><%=cmClientComponentName %></td>
        <td>Set request parameter 'c'</td>
    </tr>
    <tr>
        <td>Search client module name</td>
        <td><%=rmiSearchClientModuleName %></td>
        <td>Set request parameter 'rm'</td>
    </tr>
    <tr class="even">
        <td>Search client component name</td>
        <td><%=rmiSearchClientComponentName %></td>
        <td>Set request parameter 'rc'</td>
    </tr>
    <tr>
        <td>Search index name</td>
        <td><%=indexName %></td>
        <td>Set request parameter 'i'</td>
    </tr>
  </tbody>
</table>
</div>
</div>
</div>
</div>
</div>  
</body>
</html>

<%!
public String toString(int value) {
    return value < 0 ? "N/A" : String.valueOf(value);
}
public String toString(long value) {
    return value < 0 ? "N/A" : String.valueOf(value);
}
public String toString(String value) {
    return value == null ? "N/A" : value;
}
public String toString(Status status, String alertMsg) {
    return status == Status.OK ? "OK" : status == Status.NOT_AVAILABLE ? "N/A" : alertMsg; 
}
%>