(function() {
    var CK_DND = function() {
        return {
            dndHandlerChain: function() {
                var handlers = {
                    "image": {
                        render: function(resourceData) {
                            return CK_DND.renderImage(resourceData);
                        }
                    }
                };

                return {
                    execute: function(resourceData) {
                        if (resourceData) {
                            if (this.canRender(resourceData)) {
                                return handlers[resourceData.resourceType].render(resourceData);
                            } else {
                                return CK_DND.renderLink(resourceData);
                            }
                        }
                    },
                    canRender: function(resourceData) {
                        return handlers.hasOwnProperty(resourceData.resourceType);
                    }
                }
            },

            renderLink: function(resourceData) {
                return $("<a />")
                    .attr("polopoly:contentid", resourceData.contentId)
                    .attr("href", "javascript:window.parent.actionEventData({$contentId:&quot;"+ resourceData.contentId +"&quot;, $action: &quot;view&quot;, $target: &quot;work&quot;})")
                    .text(resourceData.name);
            },
            renderImage: function(resourceData) {
                return $("<img />")
                    .attr("src", resourceData.defaultFileURL)
                    .attr("alt", resourceData.imgAlt)
                    .attr("polopoly:contentid", resourceData.contentId)
                    .attr("polopoly:contentfilepath", resourceData.contentFilePath);
            }
        };
    }();

    var insertResourceTag = function(editor, resourceElement) {
        editor.insertElement(CKEDITOR.dom.element.createFromHtml(resourceElement.get(0).outerHTML));
    };

    var initDragAndDrop = function(editor) {
        editor.on('instanceReady', function(event) {
            event.editor.document.on('dragover', function(event) {
                event.data.preventDefault(true);
            });

            event.editor.document.on('drop', function(event) {
                event.data.preventDefault(true);

                var jsondata = event.data.$.dataTransfer.getData("application/x-resource-data-json");

                if (jsondata) {
                  var json = JSON.parse(jsondata);
                  var resourceElement = CK_DND.dndHandlerChain().execute(json);

                  insertResourceTag(editor, resourceElement);
                }
            });
        });
    };

    CKEDITOR.plugins.add('droprecieve', {
        init: function(editor) {
            initDragAndDrop(editor);
        }
    });
})();
