package example.greenfieldtimes.adapter;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ArticleBean
{
    private String title;
    private String lead;
    private String body;
    private String byline;
    private String editorialNotes;
    private Date createdDate;
    private Date modifiedDate;
    private Date publishedDate;
    private Map<String, String> customAttributes;
    private String priority;

    // Content lists contain different types
    private List<com.polopoly.content.integration.AdapterContentId> topImages;
    private List<com.polopoly.content.integration.AdapterContentId> images;
    private List<com.polopoly.content.integration.AdapterContentId> relatedArticles;

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
    public String getEditorialNotes()
    {
        return editorialNotes;
    }
    public void setEditorialNotes(String editorialNotes)
    {
        this.editorialNotes = editorialNotes;
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
    public String getPriority()
    {
        return priority;
    }
    public void setPriority(String priority)
    {
        this.priority = priority;
    }
    public List<com.polopoly.content.integration.AdapterContentId> getTopImages()
    {
        return topImages;
    }
    public void setTopImages(List<com.polopoly.content.integration.AdapterContentId> topImages)
    {
        this.topImages = topImages;
    }
    public List<com.polopoly.content.integration.AdapterContentId> getImages()
    {
        return images;
    }
    public void setImages(List<com.polopoly.content.integration.AdapterContentId> images)
    {
        this.images = images;
    }
    public List<com.polopoly.content.integration.AdapterContentId> getRelatedArticles()
    {
        return relatedArticles;
    }
    public void setRelatedArticles(List<com.polopoly.content.integration.AdapterContentId> relatedArticles)
    {
        this.relatedArticles = relatedArticles;
    }
}
