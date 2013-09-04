CKEDITOR.addStylesSet( 'simpletablestyle', [
    { name: 'Paragraph', element: 'p' },
    { name: 'Heading 2', element: 'h2' },
    { name: 'Heading 3', element: 'h3' },
    { name: 'Heading 4', element: 'h4', styles : { 'padding' : '5px'} },
    { name: 'Citation', element: 'cite' },
    { name: 'Acronym', element: 'abbr' },
    { name: 'Abbreviation', element: 'abbr' },
  ]);

// Skipping cut, copy and paste since they do not normally work due to browser security
//'Cut','Copy','Paste',
CKEDITOR.editorConfig = function( config )
{
  config.stylesCombo_stylesSet = 'simpletablestyle';
    
  config.toolbar = 'TableEdit';
  config.toolbar_TableEdit =
    [
     ['OrchidTableEdit','-','Undo','Redo','-','Bold','Italic','Underline']
    ];

  //stylesheet for editor
  config.contentCss = '/css/preview.css'

  config.extraPlugins = 'simpletable';
};