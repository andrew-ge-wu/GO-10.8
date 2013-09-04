function getTable(cols, rows) {
        var tableString = '<table style="width: 100%;">';
        if (this.Head) {
            tableString += '<tr>';
            for (var i = 0; i < cols; i++) {
                tableString += '<th>&nbsp;</th>';
            }
            tableString += '</tr>';
        }

        for (var y = 0; y < rows; y++) {
            tableString += '<tr>';
            for (var x = 0; x < cols; x++) {
                tableString += '<td>&nbsp;</td>';
            }
            tableString += '</tr>';
        }

    return tableString;
}


CKEDITOR.plugins.add('simpletable', {
    init: function(editor) {
    editor.addCommand('insertTable', {
        modes : { wysiwyg:1, source:0 },
        editorFocus : true,
        canUndo: true,
        exec: function(editor) { editor.insertHtml(getTable(2, 3)); }
    });
    editor.ui.addButton('OrchidTableEdit', {
        label: 'Insert table',
        command: 'insertTable',
        icon: this.path + 'table_new.png'
    });
    }
});
