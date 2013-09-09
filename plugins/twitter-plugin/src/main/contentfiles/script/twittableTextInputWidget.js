var compoundId = 'compoundId';

function twittableJsWidget() {}

twittableJsWidget.prototype.initSelf = function() {
    compoundId = this.initParams[0]+"_placeHolder";
}

jQuery(document).ready(function() {
    jQuery("#"+compoundId+" .tweetitnow :checkbox").click(function(){
        processTextDiv();
    });
    
    processTextDiv();
});

function processTextDiv(){
    var checked = jQuery("#"+compoundId+" .tweetitnow :checkbox:checked").val();
    var name = jQuery(".field.text.heading.name :text").val();
    var treeDiv = jQuery("#"+compoundId+" .tweetText");
    var singleSelectInput = jQuery("#"+compoundId+" .account");
    var textSelector = jQuery("#"+compoundId+" .tweetText :text");
    var tweetText = jQuery.trim(textSelector.val());

    if (checked=='true'){
        treeDiv.fadeIn("slow");
        singleSelectInput.fadeIn("slow");
        if (tweetText=='') {
            textSelector.val(name);
            var tweetTextCount = name.length;
            updateCounter(tweetTextCount);
        }
    } else {
        treeDiv.fadeOut("slow");
        singleSelectInput.fadeOut("slow");
    }
}

