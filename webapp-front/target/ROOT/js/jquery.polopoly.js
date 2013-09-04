function ajaxLoad() {
    var elements = $("div").filter(function() { return $(this).attr('polopoly:ajax'); });
    var numberOfElements = elements.length;
	elements.each(function (i) {
            var ajaxElement = this;

            // Detect when all ajax tags have been loaded
            $(ajaxElement).bind('ajax.loaded', function() {
            	if (--numberOfElements == 0) {
            		$(document).trigger('document.ajax.loaded');
            	};
            });

            // Schedule ajax load
            var ajaxUrl = $(ajaxElement).attr("polopoly:ajax");
            $.get(ajaxUrl, function(data){
                    $(ajaxElement).before(data);
                    $(ajaxElement).trigger('ajax.loaded');
                    $(ajaxElement).remove();
                });
        });
}

$(document).ready(function() { ajaxLoad(); });
