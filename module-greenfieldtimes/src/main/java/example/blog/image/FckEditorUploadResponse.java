package example.blog.image;

import com.polopoly.cm.ContentId;

public class FckEditorUploadResponse
{
    private Status _status = Status.OK;
    
    private ContentId _blogPostContentId;
    private String _imageFilePath;
    
    /**
     * @throws IllegalArgumentException if status is null.
     */
    public void setStatus(Status status)
    {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        _status = status;
    }
    
    public Status getStatus() {
        return _status;
    }

    public void setBlogPostContentId(ContentId blogPostContentId)
    {
        _blogPostContentId = blogPostContentId;
    }

    public void setImageFilePath(String imageFilePath)
    {
        _imageFilePath = imageFilePath;
    }

    /**
     * Assembles the JavaScript method for the user callback.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(400);
        
        sb.append("<script type=\"text/javascript\">\n");
        sb.append("(function(){var d=document.domain;while (true){try{var A=window.parent.document.domain;break;}catch(e) {};d=d.replace(/.*?(?:\\.|$)/,'');if (d.length==0) break;try{document.domain=d;}catch (e){break;}}})();\n");
        sb.append("window.parent.OnUploadCompleted(");
        sb.append(_status.getCode());
        sb.append(",");
        sb.append("'");
        sb.append(_blogPostContentId == null ? "null" : _blogPostContentId.getContentIdString());
        sb.append("'");
        sb.append(",");
        sb.append("'");
        sb.append(_imageFilePath == null ? "null" : _imageFilePath);
        sb.append("'");
        sb.append(");\n");
        sb.append("</script>");

        return sb.toString();
    }
    
    public static enum Status
    {
        OK(0),
        BAD_INPUT(10),
        PERMISSION_DENIED(11),
        IMAGE_TOO_LARGE(12),
        INVALID_FILE_EXTENSION(13),
        SERVER_ERROR(20);
        
        private final int _code;

        private Status(int code)
        {
            _code = code;
        }

        public int getCode()
        {
            return _code;
        }
    }
}
