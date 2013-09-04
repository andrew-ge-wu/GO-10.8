(function() {
    window.Example = window.Example || {};

    /**
     * Global state for the ExtraFields drop-down pane and its
     * associated toolbar button.
     */
    var extraFieldsToolbarButton = null;
    var jq_activeExtraFields = null;

    /**
     * Constructor: EditableTeaser
     * Create a news teaser editable in interactive preview
     *
     *  Parameters:
     *   elementDiv - The HTML DOM element that will be turned in to an editable teaser
     *
     *  Returns:
     *   Editable teaser object that can communicate with the page manager
     *
     */
    Example.EditableTeaser = function(elementDiv) {
        var teaser = Polopoly.EditableObject();

        /* The title field is given by the only H1 in the teaser */
        var titleSelector = jq(elementDiv).find('div h1').parent('div');
        var title = teaser.createEditableTitleField(titleSelector,
            {
                childSelector: 'h1',
                letterSpacing: true,
                fontResizeTitle: getLocale().fontResizer,
                letterSpacingTitle: getLocale().letterSpacer,
                ignoredFieldsSelector: jq('.interactive-ignore')
            }
        );

        /* For speed, we create a lead selector once and use it several times,
         * instead of passing it in every argument.
         */
        var leadSelector = jq(elementDiv).find('div p').parent('div');
        var lead         = teaser.createEditableLeadField(leadSelector,
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

        var originalBackgroundColor      = jq(elementDiv).css('background-color');
        var jq_extraFields               = jq(elementDiv).find('.extraFields');
        var originalTitleFontSizeSet     = jq(elementDiv).find('h1').parent("div").attr('style') &&
                                           jq(elementDiv).find('h1').parent("div").attr('style').toLowerCase().indexOf("font-size") >= 0;
        /**
         * Callback: onPageModeChange
         * Called on all elements when page changes to and from edit mode.
         *
         * Parameters:
         *  inElementEditMode - True if page entered edit mode.
         */
        teaser.bind('onPageModeChange', function(inElementEditMode) {
            if (inElementEditMode) {
                // if the title font size was not originally set,
                // we need to set the font size on the parent div to be
                // able to adjust it

                if (!originalTitleFontSizeSet) {
                   var fsh1 = jq(elementDiv).find("h1").css("font-size");

                   jq(elementDiv).find("h1").parent("div").css("font-size", fsh1);
                   jq(elementDiv).find("h1").css("font-size", "inherit");
                }

                jq(elementDiv).addClass('selectable').click(function(e) {
                    teaser.activate();
                    //
                    // workaround for chrome bug
                    // http://code.google.com/p/chromium/issues/detail?id=118587
                    // after activating teaser (setting contenteditale) we focus the 
                    // div we clicked on
                    jq(e.target).focus();
                    return false;
                });

                jq_extraFields.click(function(e) {
                    e.stopPropagation();
                });

                jq_extraFields.find('.style').change(function() {
                    jq(elementDiv).find('.breakingNewsHeader, .breakingNewsFooter').css('display', 'none');
                    jq(elementDiv).removeClass('highlighted');

                    if (jq_extraFields.find("input[class='style']:checked").val() == 'BreakingNews') {
                        jq(elementDiv).find('.breakingNewsHeader, .breakingNewsFooter').css('display', 'block');
                    } else if (jq_extraFields.find("input[class='style']:checked").val() == 'Highlighted') {
                        jq(elementDiv).addClass('highlighted');
                    }

                    teaser.changed();
                });
            } else {
                // if the title font size was not set originally and has
                // not changed, we need to reset the font sizes so that
                // the title will adjust to css when rearranging.

                if (!title.fontSize() && !originalTitleFontSizeSet) {
                  jq(elementDiv).find("h1").parent("div").css("font-size", "");
                  jq(elementDiv).find("h1").css("font-size", "");
                }

                //Disable listeners to mouse and keyboard when
                //leaving element edit mode
                jq(elementDiv).unbind('click').removeClass('selectable selected');
                if (extraFieldsToolbarButton != null) {
                    jq(extraFieldsToolbarButton).hide();
                }
            }
        });

        teaser.bind('activate', function() {
            ensureExtraFieldsToolbarButton();
            jq(extraFieldsToolbarButton).show();
            jq_extraFields.slideDown();
            jq_activeExtraFields = jq_extraFields;

            jq(elementDiv).addClass("selected");

            // Make images editable if they have the necessary attributes
            jq(elementDiv).find("img").each(function () {
                if (this.imageEditor) {
                    this.imageEditor.setEditable(true);
                } else {
                    var derivativerect = jq(this).attr('polopoly:derivativecrop');
                    var originalsrc    = jq(this).attr('polopoly:originalsrc');

                    if (derivativerect && originalsrc) {
                        this.imageEditor = new Polopoly.ImageEditor(this, teaser);
                    }
                }
            });
        });

        function getLocale() {
            return window.Polopoly.Localization.Toolbar;
        }

        function ensureExtraFieldsToolbarButton() {
            if (extraFieldsToolbarButton == null) {
                extraFieldsToolbarButton = Polopoly.preview.toolbar.getCustomToolbar().button("extraFieldsButton", "", function() {
                    if (jq_activeExtraFields != null) {
                        jq_activeExtraFields.slideToggle();
                    }
                });
            }
        }


        /**
         * Method: modelMap
         * Create a map containing data to be saved.
         *
         * Returns:
         *   Javascript object where field names are dot separated model paths
         *   and field values are string with the values to store on those paths.
         */
        teaser.bind('modelMap', function() {
            var area = undefined, derivativeName;
            jq(elementDiv).find("img").each(function () {
                if (this.imageEditor) {
                    derivativeName = jq(this).attr("polopoly:derivativename");
                    area = derivativeName + "," + this.imageEditor.getCropRectangle().toIntRect().toString();
                }
            });

            return {
            	'name'                    : title.rawValue(),
            	'titleFontSize.value'     : title.fontSize(),
            	'titleLetterSpacing.value': title.letterSpacing(),

            	'teaser.value'            : lead.rawValue(),
            	'teaserFontSize.value'    : lead.fontSize(),

            	'style.value'             : jq_extraFields.find("input[class='style']:checked").val(),

            	'image.editorAreaString'  : area
            };
        });

        /**
         * Callback: onDeactivate
         * Called when this teaser loses editing focus.
         */
        teaser.bind('onDeactivate', function() {
            jq_extraFields.slideUp();
            if (extraFieldsToolbarButton != null) {
                 jq(extraFieldsToolbarButton).hide();
            }
            jq_activeExtraFields = null;

            //Reset background color to show user that this teaser is
            //no longer in edit mode
            jq(elementDiv).removeClass("selected");
            jq(elementDiv).find("img").each(function () {
                if (this.imageEditor) {
                    this.imageEditor.setEditable(false);
                }
            });
        });

        return teaser;
    };
})();
