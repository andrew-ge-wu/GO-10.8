<html>
<head>
<script type='text/javascript' src='script/jquery-1.2.6.min.js'></script>
<script type='text/javascript' src='script/jquery.autocomplete.js'></script>
<script type='text/javascript' src='script/jquery.json-1.3.js'></script>
<link rel='stylesheet' href='stylesheets/jquery.autocomplete.css'/>
</head>
<body>
<h2>Hello World!</h2>

  <script>
  ;(function($) { 
	  var tag_completion_parameters = function(dimensionId) {
	      var solrDataParser = function(response) {
	         
	         suggestions = $.evalJSON(response)
	         matching_tags = suggestions.facet_counts.facet_fields["tag_facet_" + dimensionId]
	         parsed = [] 
	         for(i=0; i < matching_tags.length; i+=2) {      
	            
	         parsed[parsed.length] = {
	                 data: [matching_tags[i] + " (" + matching_tags[i+1] + ")" , matching_tags[i]],
	                 value: matching_tags[i],
	                 result: matching_tags[i]
	             };
	         }
	         return parsed;
	      }
	
	      parameters = {
	           multiple: true,
	           matchContains: true,
	           parse: solrDataParser,
	               extraParams: {
	                   dimensionId: dimensionId
	           }
	      }
	      return parameters
	  }
	  
	  autocomplete = function(widgetId) {
	
			  $(document).ready(function(){ 
					  $("#"+widgetId).autocomplete('solrproxy', 
						  tag_completion_parameters("department.categorydimension.person"));
			  });
		  
	  }
  })(jQuery);
  autocomplete("example")
  
  </script>
  
<form>
<input id="example" type="text" />
<div id="suggestion">
</div>

</form> 
</body>
</html>
