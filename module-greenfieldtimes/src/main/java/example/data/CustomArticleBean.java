package example.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.polopoly.cm.ContentId;

public class CustomArticleBean
{
    private String title;
    private String lead;
    private String body;
    private String byline;
    private Date createdDate;
    private Date modifiedDate;
    private Date publishedDate;
    private Map<String, String> customAttributes;

    // Content lists contain different types
    private List<ContentId> topImages;
    private List<ContentId> images;
    private List<ContentId> relatedArticles;

    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getLead()
    {
        return lead;
    }
    public void setLead(String lead)
    {
        this.lead = lead;
    }
    public String getBody()
    {
        return body;
    }
    public void setBody(String body)
    {
        this.body = body;
    }
    public String getByline()
    {
        return byline;
    }
    public void setByline(String byline)
    {
        this.byline = byline;
    }
    public Date getCreatedDate()
    {
        return createdDate;
    }
    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }
    public Date getModifiedDate()
    {
        return modifiedDate;
    }
    public void setModifiedDate(Date modifiedDate)
    {
        this.modifiedDate = modifiedDate;
    }
    public Date getPublishedDate()
    {
        return publishedDate;
    }
    public void setPublishedDate(Date publishedDate)
    {
        this.publishedDate = publishedDate;
    }
    public Map<String, String> getCustomAttributes()
    {
        return customAttributes;
    }
    public void setCustomAttributes(Map<String, String> customAttributes)
    {
        this.customAttributes = customAttributes;
    }
    public List<ContentId> getTopImages()
    {
        return topImages;
    }
    public void setTopImages(List<ContentId> topImages)
    {
        this.topImages = topImages;
    }
    public List<ContentId> getImages()
    {
        return images;
    }
    public void setImages(List<ContentId> images)
    {
        this.images = images;
    }
    public List<ContentId> getRelatedArticles()
    {
        return relatedArticles;
    }
    public void setRelatedArticles(List<ContentId> relatedArticles)
    {
        this.relatedArticles = relatedArticles;
    }
}