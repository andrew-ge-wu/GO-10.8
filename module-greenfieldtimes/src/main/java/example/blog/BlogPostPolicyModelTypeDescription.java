package example.blog;

import com.polopoly.cm.client.CMException;

public interface BlogPostPolicyModelTypeDescription {

    public String getName() throws CMException;

    public String getText() throws CMException;
}
