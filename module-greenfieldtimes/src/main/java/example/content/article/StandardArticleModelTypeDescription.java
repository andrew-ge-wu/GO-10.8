package example.content.article;

import com.polopoly.cm.client.CMException;

public interface StandardArticleModelTypeDescription
{
    String getAuthor() throws CMException;

    long getPublishingDateTime();

    String getPrimaryLocation() throws CMException;

    String getPrimaryTag() throws CMException;
}
