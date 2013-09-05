// Constructor for the JSYouTubeItemWidget JavaScript class
function JSYouTubeItemWidget() {
}

// Initialize the JSWidget.
JSYouTubeItemWidget.prototype.initSelf = function() {
    // The ID of the text input is available in the initParams array
    // that the OExampleTextInputPolicyWidget created in the getInitParams() 
    // method
    this.textinput = document.getElementById(this.initParams[0]);
}

//Copy the value of the text input to the clipboard.
JSYouTubeItemWidget.prototype.copy = function() {
    return JSClipFactory.getInstance().newSimpleClip(
        "text", this.textinput.value);
}

// Paste the value of the given clip to the text input
JSYouTubeItemWidget.prototype.paste = function(clip) {
    this.textinput.value = clip.getValue("text");
}