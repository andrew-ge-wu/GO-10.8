jQuery(document).ready(function() {
	var s = "<div id=\"pagemask\"></div>";
	jQuery(s).css({
		'opacity' : 0.5,			
		'position' : 'relative',
		'z-index' : '9000',
		'background-color' : '#EEEEEE',
		'display' : 'block'
	})
	.appendTo(".department");
	
	jQuery(".autoDepartment :radio").click(function(){
		processTreeDiv();
	});
	
	processTreeDiv();
});

function maskTreeDiv(){
	var treeDiv = jQuery(".department");
	
	// +2 because border=1
	var maskWidth = jQuery(".department div").width() + 2;
	var maskHeight = treeDiv.height() + 2;
	
	jQuery('#pagemask').css({
		'width' : maskWidth,
		'height' : maskHeight,
		'margin-top' : -maskHeight
	})
	.show();
}

function unmaskTreeDiv(){
	jQuery('#pagemask').hide();
}

function processTreeDiv(){
	var checked = jQuery(".autoDepartment :radio:checked").val();
	
	if(checked=='true'){
		maskTreeDiv();
	}else{
		unmaskTreeDiv();
	}
}