package example.greenfieldtimes.adapter;

import com.polopoly.model.pojo.FileReference;

public class ImageBean
{
    private String name;
    private String description;
    private FileReference image;
    private String byline;
    private String contact;
    private String licenseUrl;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public FileReference getImage()
    {
        return image;
    }

    public void setImage(FileReference image)
    {
        this.image = image;
    }

    public String getByline()
    {
        return byline;
    }

    public void setByline(String byline)
    {
        this.byline = byline;
    }

    public String getContact()
    {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    public String getLicenseUrl()
    {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl)
    {
        this.licenseUrl = licenseUrl;
    }
}
