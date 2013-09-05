//google.load("swfobject", "2.1");
var params = { allowScriptAccess: "always", bgcolor: "#cccccc" };
var atts = { id: "myytplayer" };
//swfobject.embedSWF("http://www.youtube.com/apiplayer?enablejsapi=1&playerapiid=ytplayer","ytapiplayer", width, height, "8", null, null, params, atts);
swfobject.embedSWF("http://www.youtube.com/apiplayer?enablejsapi=1&playerapiid=ytplayer","ytapiplayer", width, height, "8", null, null, params, atts);
//swfobject.embedSWF("http://www.youtube.com/e/"+ yid + "?enablejsapi=1&playerapiid=ytplayer","ytapiplayer", width, height, "8", null, null, params, atts);

//value 0 is video play and stop, 1 is video never start
var isPlay = '0';
function cuePlayVideo() {
	isPlay = '1';
	//unit in secs
	ytplayer.cueVideoById(yid, startTime);	
}

function onYouTubePlayerReady(playerId) {
      ytplayer = document.getElementById("myytplayer");
      ytplayer.addEventListener("onStateChange", "onytplayerStateChange");
      ytplayer.addEventListener("onError", "onyterror");
      ytplayer.setSize(width, height);
      setTimeout("cuePlayVideo()", 300);
}

function onytplayerStateChange(newState) {
	//unstarted (-1), ended (0), playing (1), paused (2), buffering (3), video cued (5)
	if(newState == 1) {
   //unit in Ms
	if(endTime > 0) {
		setTimeout('ytplayer.stopVideo()', endTime);
   	}
   }
   if(newState == 5) {
	   if(isPlay) {
	    setTimeout("cuePlayVideo()", 1000);
   	}
   }
}
function onyterror(code) {
	$('#myytplayer').parent().removeClass('field');
	$('#myytplayer').parent().addClass('errorField');
	if(code == 101 || code == 150) {
		$('#myytplayer').parent().prepend($('<div class="error"><img src="images/icons/error.png" alt="System" class="messageIcon">Youtube Viewer: Video requested does not allow playback in the embedded players.</div>'));
	} else if(code == 100) {
		$('#myytplayer').parent().prepend($('<div class="error"><img src="images/icons/error.png" alt="System" class="messageIcon">Youtube Viewer: Video requested is not found. This occurs when a video has been removed (for any reason), or it has been marked as private.</div>'));
	} else if(code == 2) {
		$('#myytplayer').parent().prepend($('<div class="error"><img src="images/icons/error.png" alt="System" class="messageIcon">Youtube Viewer: Invalid Video ID.</div>'));
	} else {
		$('#myytplayer').parent().prepend($('<div class="error"><img src="images/icons/error.png" alt="System" class="messageIcon">Youtube Viewer: Unknow error occured.</div>'));
	}
}