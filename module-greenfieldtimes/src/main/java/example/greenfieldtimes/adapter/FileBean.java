package example.greenfieldtimes.adapter;

import com.polopoly.model.pojo.FileReference;

public class FileBean
{
    private FileReference _file;
    private String _name;
    private String _description;
    private String _byline;
    private String _contact;
    private String _licenseUrl;

    public FileReference getFile()
    {
        return _file;
    }

    public void setFile(FileReference file)
    {
        _file = file;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public String getByline()
    {
        return _byline;
    }

    public void setByline(String byline)
    {
        _byline = byline;
    }

    public String getContact()
    {
        return _contact;
    }

    public void setContact(String contact)
    {
        _contact = contact;
    }

    public String getLicenseUrl()
    {
        return _licenseUrl;
    }

    public void setLicenseUrl(String licenceUrl)
    {
        _licenseUrl = licenceUrl;
    }
}
