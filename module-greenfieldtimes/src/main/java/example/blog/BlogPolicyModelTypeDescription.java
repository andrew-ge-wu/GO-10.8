package example.blog;

import com.polopoly.cm.client.CMException;

public interface BlogPolicyModelTypeDescription
{
    public String getName() throws CMException;

    public String getTitle() throws CMException;

}
