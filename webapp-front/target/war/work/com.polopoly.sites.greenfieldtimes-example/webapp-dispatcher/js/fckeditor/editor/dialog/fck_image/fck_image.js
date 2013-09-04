/*
 * FCKeditor - The text editor for Internet - http://www.fckeditor.net
 * Copyright (C) 2003-2009 Frederico Caldeira Knabben
 *
 * == BEGIN LICENSE ==
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU General Public License Version 2 or later (the "GPL")
 *    http://www.gnu.org/licenses/gpl.html
 *
 *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
 *    http://www.gnu.org/licenses/lgpl.html
 *
 *  - Mozilla Public License Version 1.1 or later (the "MPL")
 *    http://www.mozilla.org/MPL/MPL-1.1.html
 *
 * == END LICENSE ==
 *
 * Scripts related to the Image dialog window (see fck_image.html).
 */

var dialog		= window.parent ;
var oEditor		= dialog.InnerDialogLoaded() ;
var FCK			= oEditor.FCK ;
var FCKLang		= oEditor.FCKLang ;
var FCKConfig	= oEditor.FCKConfig ;
var FCKDebug	= oEditor.FCKDebug ;
var FCKTools	= oEditor.FCKTools ;

var bImageButton = ( document.location.search.length > 0 && document.location.search.substr(1) == 'ImageButton' ) ;

// Get the selected image (if available).
var oImage = dialog.Selection.GetSelectedElement() ;

if (oImage && oImage.tagName != 'IMG' && !(oImage.tagName == 'INPUT' && oImage.type == 'image' ))
	oImage = null ;

var oImageOriginal ;

window.onload = function()
{
	var titleTextNode = parent.document.getElementById('TitleArea').childNodes[2];
	
	var uploadFormElement = document.getElementById("frmUpload");
    var blogIdElementFromParent = parent.parent.document.getElementById("blogId");

    if (blogIdElementFromParent) {
        var blogIdValue = blogIdElementFromParent.value;
        
        var blogIdElement = document.createElement("input");
        blogIdElement.setAttribute("type", "hidden");
        blogIdElement.setAttribute("name", "blogId");
        blogIdElement.setAttribute("id", "blogId");
        blogIdElement.setAttribute("value", blogIdValue);
        
        uploadFormElement.appendChild(blogIdElement);
    }

    var blogPostIdElementFromParent = parent.parent.document.getElementById("blogPostId");
    
    if (blogPostIdElementFromParent) {
        var blogPostIdValue = blogPostIdElementFromParent.value;
        
        var blogPostIdElement = document.createElement("input");
        blogPostIdElement.setAttribute("type", "hidden");
        blogPostIdElement.setAttribute("name", "blogPostId");
        blogPostIdElement.setAttribute("id", "blogPostId");
        blogPostIdElement.setAttribute("value", blogPostIdValue);
        
        uploadFormElement.appendChild(blogPostIdElement);
    }
    
	if (oImage) {
		titleTextNode.data = FCKLang.DlgImgTitle;
	} else {
		titleTextNode.data = FCKLang.DlgImgUploadImage;
	}

	// Translate the dialog box texts.
	oEditor.FCKLanguageManager.TranslatePage(document) ;

	// Load the selected element information (if any).
	LoadSelection() ;

	// Set the actual uploader URL.
	if (FCKConfig.ImageUpload) {
		GetE('frmUpload').action = FCKConfig.ImageUploadURL;
	}

	// Activate the "OK" button.
	dialog.SetOkButton(true);
	
	// If is edit, disable form fields
	if (oImage) {
        DisableUrlUploadForm();
	}
}

function LoadSelection()
{
	if (!oImage) return;

	var sUrl = oImage.getAttribute( '_fcksavedurl' ) ;
	if ( sUrl == null )
		sUrl = GetAttribute( oImage, 'src', '' ) ;

	GetE('txtUrl').value    = sUrl ;
	var attr = GetAttribute(oImage, 'class', '');
	var t = document.alignForm.cmbAlign;
	for (var i = 0; i < t.length; i++) {
		if (t[i].value == attr) {
			t[i].checked = "checked";
		}
	}

	var regexSize = /^\s*(\d+)px\s*$/i;
}

//#### The OK button was hit.
function Ok()
{
	// Upload file if not empty
	var uploadFile = CheckUpload();
	
	if (1 == uploadFile) {
		SubmitUploadForm();
	} else if (0 == uploadFile) {
		return StoreImage();
	}

	return false;
}

function SubmitUploadForm() {
	// Show animation
	window.parent.Throbber.Show( 100 );
	DisableImageUploadForm();

	GetE('frmUpload').submit();
}

function StoreImage(contentId, filePath) {
	if (GetE('txtUrl').value.length == 0) {
		GetE('txtUrl').focus();
		alert(FCKLang.DlgImgAlertUrl);
		return false ;
	}

	var bHasImage = ( oImage != null ) ;

	if ( bHasImage && bImageButton && oImage.tagName == 'IMG' )
	{
		if ( confirm( 'Do you want to transform the selected image on a image button?' ) )
			oImage = null ;
	}
	else if ( bHasImage && !bImageButton && oImage.tagName == 'INPUT' )
	{
		if ( confirm( 'Do you want to transform the selected image button on a simple image?' ) )
			oImage = null ;
	}

	oEditor.FCKUndo.SaveUndoStep() ;
	if ( !bHasImage )
	{
		if ( bImageButton )
		{
			oImage = FCK.EditorDocument.createElement( 'input' ) ;
			oImage.type = 'image' ;
			oImage = FCK.InsertElement( oImage ) ;
		}
		else
			oImage = FCK.InsertElement( 'img' ) ;
	}

	UpdateImage(oImage, contentId, filePath);

	return true ;
	
}

function GetCmbAlignValue() {
	var t = document.alignForm.cmbAlign;
	for (var i = 0; i < t.length; i++) {
		var box = t[i];
		if (box.checked) {
			return box.value;
		}
	}
}

function UpdateImage(e, contentId, filePath)
{
	e.src = GetE('txtUrl').value ;
	SetAttribute( e, "_fcksavedurl", GetE('txtUrl').value ) ;
	var align = GetCmbAlignValue();
	SetAttribute(e, "class", align);

	// If is content image, set contentid and contentfilepath
	if (contentId != null && filePath != null) {
	  SetAttribute(e, "polopoly:contentid" , contentId);
	  SetAttribute(e, "polopoly:contentfilepath" , filePath);
	}
}

function SetUrl( url, width, height, alt )
{
	GetE('txtUrl').value = url ;
}

//function OnUploadCompleted( errorNumber, fileUrl, fileName, customMsg )
function OnUploadCompleted(responseCode, contentId, filePath)
{
	if (HasError(responseCode)) {
	    // Remove animation
	    window.parent.Throbber.Hide();
	    EnableImageUploadForm();
	 	
	 	return;
	}
	
	// Remove animation
	window.parent.Throbber.Hide() ;
	GetE( 'divUpload' ).style.display  = '' ;
	
	// Create real file url
	var fileUrl = CreateFileUrl(contentId, filePath);
	
	SetUrl(fileUrl);
	var uvContentId = GetUnversionedContentId(contentId);
	StoreImage(uvContentId, filePath);
	GetE('frmUpload').reset();
	
	var blogPostEditFormFromParent = parent.parent.document.getElementById("createBlogPostForm");
	var blogPostCancelFormFromParent = parent.parent.document.getElementById("cancelBlogPostForm");
	
    var blogPostIdElementFromParent = parent.parent.document.getElementById("blogPostId");
    var blogPostIdCancelElementFromParent = parent.parent.document.getElementById("blogPostIdCancel");
    
    if (blogPostIdElementFromParent) {
        blogPostIdElementFromParent.value = contentId;
    } else {
        var recv = parent.parent.document.getElementById("inputReceiver");
        recv.innerHTML = '<input type="hidden" name="blogPostId" id="blogPostId" value="' + contentId + '" />';
    }
    
    if (blogPostIdCancelElementFromParent) {
        blogPostIdCancelElementFromParent.value = contentId;
    } else {
        var recv = parent.parent.document.getElementById("deleteReceiver");
        recv.innerHTML = '<input type="hidden" name="blogPostId" id="blogPostIdCancel" value="' + contentId + '" />';

    }

	dialog.CloseDialog();
}

function HasError(responseCode) {
    switch (responseCode) {
        case 0:
            return false;            
        case 10:
            alert(FCKLang.BlogImageUploadPrefix + FCKLang.BlogImageUploadBadInput);
            return true;
        case 11:
            alert(FCKLang.BlogImageUploadPrefix + FCKLang.BlogImageUploadPermissionDenied);
            return true;
        case 12:
            alert(FCKLang.BlogImageUploadPrefix + FCKLang.BlogImageUploadImageTooLarge);
            return true;
        case 13:
            alert(FCKLang.BlogImageUploadPrefix + FCKLang.BlogImageUploadInvalidFileExtension);
            return true;
        case 20:
            alert(FCKLang.BlogImageUploadPrefix + FCKLang.BlogImageUploadServerError);
            return true;
        default:
            alert(FCKLang.BlogImageUploadPrefix + FCKLang.BlogImageUploadUnknownError);
            return true;
    }
}

var oUploadAllowedExtRegex	= new RegExp( FCKConfig.ImageUploadAllowedExtensions, 'i' ) ;
var oUploadDeniedExtRegex	= new RegExp( FCKConfig.ImageUploadDeniedExtensions, 'i' ) ;

function CheckUpload()
{
	var sFile = GetE('txtUploadFile').value ;

	// No file selected
	if ( sFile.length == 0 )
	{
		return 0 ;
	}

	// Bad file selected
	if ( ( FCKConfig.ImageUploadAllowedExtensions.length > 0 && !oUploadAllowedExtRegex.test( sFile ) ) ||
		( FCKConfig.ImageUploadDeniedExtensions.length > 0 && oUploadDeniedExtRegex.test( sFile ) ) )
	{
		HasError(13);
		return -1;
	}
	
	// File ok for upload
	return 1 ;
}

function GetUnversionedContentId(contentId) {
	return contentId.substring(0, contentId.lastIndexOf("."));
}

function CreateFileUrl(contentId, filePath) {
	return FCKConfig.ContentFileServlet + "/" + contentId + "!" + filePath;
}

function EnableImageUploadForm() {
    SetAttribute(GetE('txtUrl'), 'disabled', 'false');
	SetAttribute(GetE('txtUrl'), 'readonly', 'false');
	GetE('imageForm').style.opacity  = '';
	GetE('imageForm').style.filter  = '';
}

function DisableImageUploadForm() {    
    SetAttribute(GetE('txtUrl'), 'disabled', 'disabled');
	SetAttribute(GetE('txtUrl'), 'readonly', 'readonly');
	GetE('imageForm').style.opacity  = '0.5';
	GetE('imageForm').style.filter  = 'alpha(opacity = 50)';
}

function EnableUrlUploadForm() {
    SetAttribute(GetE('txtUploadFile'), 'disabled', 'false');
	SetAttribute(GetE('txtUrl'), 'readonly', 'false');
	GetE('urlForm').style.opacity  = '';
	GetE('urlForm').style.filter  = '';
}

function DisableUrlUploadForm() {
    SetAttribute(GetE('txtUploadFile'), 'disabled', 'disabled');
	SetAttribute(GetE('txtUrl'), 'readonly', 'readonly');
	GetE('urlForm').style.opacity  = '0.5';
	GetE('urlForm').style.filter  = 'alpha(opacity = 50)';
}