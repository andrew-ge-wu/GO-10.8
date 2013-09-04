package example.data;

import static com.polopoly.model.ModelPathUtil.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.polopoly.cm.ContentId;
import com.polopoly.data.CustomModelContext;
import com.polopoly.data.ModelMapper;
import com.polopoly.model.Model;
import com.polopoly.model.PojoAsModel;

/**
 * Data-api mapper between {@link CustomArticleBean}:s model type and the example.StandardArticle model type
 * (or anything with the same model paths).
 */
public class CustomArticleMapper implements ModelMapper
{
    @SuppressWarnings("unchecked")
    @Override
    public Model map(Model articleModel, CustomModelContext context)
    {
        CustomArticleBean articleBean = new CustomArticleBean();
        articleBean.setTitle(get(articleModel, "name", String.class));
        articleBean.setBody(get(articleModel, "body/value", String.class));
        articleBean.setLead(get(articleModel, "lead/value", String.class));
        articleBean.setByline(get(articleModel, "author", String.class));
        articleBean.setCreatedDate(new Date(get(articleModel, "createdDate/timeMillis", Long.class)));
        articleBean.setModifiedDate(new Date(get(articleModel, "modifiedDate/timeMillis", Long.class)));
        articleBean.setPublishedDate(new Date(get(articleModel, "publishingDateTime", Long.class)));

        articleBean.setTopImages(getContentIdList(articleModel, "topimages"));
        articleBean.setImages(getContentIdList(articleModel, "images"));

        articleBean.setRelatedArticles(getContentIdList(articleModel, "related"));

        articleBean.setCustomAttributes(get(articleModel, "custom/namesAndValues", Map.class));

        return new PojoAsModel(context.getModelDomain(), articleBean);
    }

    private List<ContentId> getContentIdList(Model articleModel, String listName)
    {
        @SuppressWarnings("unchecked")
        List<Model> images = get(articleModel, listName + "/list", List.class);
        List<ContentId> contentIds = new ArrayList<ContentId>(images.size());
        for (Model model : images)
        {
            contentIds.add(get(model, "contentId", ContentId.class));
        }

        return contentIds;
    }
}
