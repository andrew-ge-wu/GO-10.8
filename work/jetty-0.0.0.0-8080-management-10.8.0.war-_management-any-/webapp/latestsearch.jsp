<%@page import="com.polopoly.management.troubleshooting.search.SearchUsage,
                com.polopoly.management.troubleshooting.search.SearchUsageCollector" 
        contentType="application/json; charset=UTF-8"
%><%
	response.setHeader("Cache-Control","no-cache");
 
	String applicationName = request.getParameter("a");
	String rmiSearchClientModuleName = request.getParameter("rm"); 
	String rmiSearchClientComponentName = request.getParameter("rc");
	String indexName = request.getParameter("i");
	String previousTotalTimeStr = request.getParameter("pt");	
	
	if (applicationName != null && rmiSearchClientModuleName != null
	    && rmiSearchClientComponentName != null && indexName != null) {
	    
	    SearchUsageCollector collector =
	        new SearchUsageCollector(applicationName, rmiSearchClientModuleName,
	                                 rmiSearchClientComponentName, indexName);
	    
	    SearchUsage bean = collector.getLastRequestSearchUsage();
	    
		long totalSuccessfulSearchTime = bean.getTotalSearchTime();
		boolean newQuery = true;
		
		if (previousTotalTimeStr != null) {
			long previousTotalTime = Long.parseLong(previousTotalTimeStr);
			newQuery = previousTotalTime != totalSuccessfulSearchTime;
		}

		if (bean.isSuccessful() && newQuery) {
%>{
    query: '<%= bean.getQuery() %>',
    time: <%= bean.getSearchTime() %>,
    totalTime: <%= totalSuccessfulSearchTime %> 
}    
<%
		} else {
%>{}
<%
		}
	}
%>