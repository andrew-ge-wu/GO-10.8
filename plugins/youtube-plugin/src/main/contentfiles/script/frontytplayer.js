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
	if(endTime != -1) {
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
	$('#video-error').remove();
	if(code == 101 || code == 150) {
		$('<div id="video-error" style="color:red">Video requested does not allow playback in the embedded players.</div>').insertBefore($('#myytplayer'));
	} else if(code == 100) {
		$('<div id="video-error" style="color:red">Video requested is not found. This occurs when a video has been removed (for any reason), or it has been marked as private.</div>').insertBefore($('#myytplayer'));
	} else if(code == 2) {
		$('<div id="video-error" style="color:red">Invalid Video ID.</div>').insertBefore($('#myytplayer'));
	} else {
		$('<div id="video-error" style="color:red">Unknow error occured.</div>').insertBefore($('#myytplayer'));
	}
}