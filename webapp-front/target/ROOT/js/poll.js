
submitVote = function(element) {
	popupPollResultWindow('');
	element.form.submit();
	return false;
}

popupPollResultWindow = function(url) {
	var win = window.open(url, 'pollResultWindow',
			'resizable=yes,width=480,height=400');
	if (win) {
		win.focus();
	}
	return false;
}
