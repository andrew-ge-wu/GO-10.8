/* For richer inline documentation, see teaser.js */
(function() {
    window.Example = window.Example || {};

    /**
     * Constructor: EditableArticle
     * Create a news article editable in interactive preview
     *
     *  Parameters:
     *   elementDiv - The HTML DOM element that will be turned in to an editable article
     *
     *  Returns:
     *   Editable article object that can communicate with the page manager
     *
     */
    Example.EditableArticle = function(elementDiv) {
    	var article = Polopoly.EditableObject();

        var title = article.createEditableTitleField(jq(elementDiv).find('div h1:first').parent('div'),
            {
                childSelector: 'h1:first',
                fontResizeTitle: getLocale().fontResizer
            }
    	);

    	/* For speed, we create a lead selector once and use it several times,
    	 * instead of passing it in every argument.
    	 */
        var leadSelector = jq(elementDiv).find('.leadContainer');
    	var lead         = article.createEditableLeadField(leadSelector,
    		{
    			/* The title attribute of the font size sliders handle */
    			fontResizeTitle: getLocale().textFontResizer,

    			/* This value is specified if we dont want the whole of the
    			 * object to be editable, but rather its children. In this
   				 * case we only want the p-tags and not the wrapping div
  				 * to be editable. If this is left blank, it will default
                 * to the standard selector.
   				 */
                childSelector: 'p',

    			/* If there are attributes inside the selector that needs
    			 * to stay in place, for example images that we dont want to
    			 * be able to delete by mistake, we must ignore this. This
    			 * is a browser quirk depending on that the contentEditable
   				 * attributes is inherited downwards in the DOM.
                 * The elements that match this selector will also be ignored
                 * when the content is saved.
   				 */
   				ignoredFieldsSelector: leadSelector.find('img, .interactive-ignore')
            }
    	);
        var jq_body = jq(elementDiv).find(".articleBody");
        var ckeditor_body;
        var originalBackgroundColor = jq(elementDiv).css("background-color");

        var originalTitleFontSizeSet = jq(elementDiv).find('h1:first').parent("div").attr('style') &&
                                       jq(elementDiv).find('h1:first').parent("div").attr('style').toLowerCase().indexOf("font-size") >= 0;

        /**
         * Callback: onPageModeChange
         *  Called on all elements when page changes to and from edit mode.
         *
         * Parameters:
         *  inElementEditMode - True if page entered edit mode.
         */
        article.bind('onPageModeChange', function(inElementEditMode) {
            if (inElementEditMode) {
                // if the title font size was not originally set,
                // we need to set the font size on the parent div to be
                // able to adjust it

                if (!originalTitleFontSizeSet) {
                   var fsh1 = jq(elementDiv).find("h1:first").css("font-size");

                   jq(elementDiv).find("h1:first").parent("div").css("font-size", fsh1);
                   jq(elementDiv).find("h1:first").css("font-size", "inherit");
                }

                jq(elementDiv).addClass("selectable").click(function(e) {
                    article.activate();
                    jq(e.target).focus();
                    return false;
                });
            } else {
                // if the title font size was not set originally and has
                // not changed, we need to reset the font sizes so that
                // the title will adjust to css when rearranging.

                if (!title.fontSize() && !originalTitleFontSizeSet) {
                  jq(elementDiv).find("h1:first").parent("div").css("font-size", "");
                  jq(elementDiv).find("h1:first").css("font-size", "");
                }

                jq(elementDiv).unbind("click").removeClass("selectable selected");
            }
        });

        article.bind('activate', function() {
            jq(elementDiv).addClass("selected");
            jq_body.attr('contenteditable', 'true');
            ckeditor_body = CKEDITOR.inline(jq_body[0],
                {on: {
                    selectionChange: function(){
                        if (this.checkDirty())
                            article.changed();
                    },
                    key: function() {
                        article.changed();
                    }
                },
                format_tags: 'p;h2;h3',
                removePlugins: 'tabletools,contextmenu',
                removeButtons: 'Styles,Underline,Subscript,Superscript,Table,Blockquote'
                });

        });

        function getLocale() {
            return window.Polopoly.Localization.Toolbar;
        }

        /**
         * Method: modelMap
         * Create a map containing data to be saved.
         *
         * Returns:
         *   Javascript object where field names are dot separated model paths
         *   and field values are string with the values to store on those paths.
         */
        article.bind('modelMap', function() {
            return {
                'name'                    : title.rawValue(),
                'body.value'              : jq_body.html(),
                'titleFontSize.value'     : title.fontSize(),

                'lead.value'              : lead.rawValue(),
                'leadFontSize.value'      : lead.fontSize()
            };
        });

        /**
         * Callback: onDeactivate
         * Called when this article loses editing focus.
         */
        article.bind('onDeactivate', function() {
            jq(elementDiv).removeClass("selected");

            if (ckeditor_body && ckeditor_body.checkDirty) {
                if (ckeditor_body.checkDirty()) {
                    article.changed();
                }
                ckeditor_body.destroy();
            }
            jq_body.removeAttr('contenteditable');
        });

        return article;
    };
})();
