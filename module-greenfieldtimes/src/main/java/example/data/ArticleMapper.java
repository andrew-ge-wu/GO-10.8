package example.data;

import static com.polopoly.model.ModelPathUtil.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.polopoly.cm.ContentId;
import com.polopoly.content.integration.AdapterContentId;
import com.polopoly.data.CustomModelContext;
import com.polopoly.data.ModelMapper;
import com.polopoly.data.ModelUpdater;
import com.polopoly.model.Model;
import com.polopoly.model.PojoAsModel;

import example.greenfieldtimes.adapter.ArticleBean;

/**
 * Data-api mapper between {@link ArticleBean}:s model type and the example.StandardArticle model type
 * (or anything with the same model paths, such as example.contenthub.StandardArticle).
 */
public class ArticleMapper
    implements ModelMapper, ModelUpdater
{

    @SuppressWarnings("unchecked")
    @Override
    public Model map(Model articleModel, CustomModelContext context)
    {
        ArticleBean articleBean = new ArticleBean();
        articleBean.setTitle(get(articleModel, "name", String.class));
        articleBean.setBody(get(articleModel, "body/value", String.class));
        articleBean.setLead(get(articleModel, "lead/value", String.class));
        articleBean.setByline(get(articleModel, "author", String.class));
        articleBean.setEditorialNotes(get(articleModel, "editorialNotes/value", String.class));
        articleBean.setCreatedDate(new Date(get(articleModel, "createdDate/timeMillis", Long.class)));
        articleBean.setModifiedDate(new Date(get(articleModel, "modifiedDate/timeMillis", Long.class)));
        articleBean.setPublishedDate(new Date(get(articleModel, "publishingDateTime", Long.class)));

        Integer priority = get(articleModel, "priority/intValue", Integer.class);
        if (priority != null) {
            articleBean.setPriority(String.valueOf(priority));
        }

        articleBean.setTopImages(getAdapterContentIdList(articleModel, "topimages"));
        articleBean.setImages(getAdapterContentIdList(articleModel, "images"));

        articleBean.setRelatedArticles(getAdapterContentIdList(articleModel, "related"));

        articleBean.setCustomAttributes(get(articleModel, "custom/namesAndValues", Map.class));

        return new PojoAsModel(context.getModelDomain(), articleBean);
    }

    @Override
    public void update(Model source, Model articleModel, CustomModelContext context)
    {
        Object bean = PojoAsModel.unwrapModelIfPossible(source);
        if (!(bean instanceof ArticleBean)) {
            throw new ClassCastException("Not an article bean");
        }
        ArticleBean articleBean = (ArticleBean) bean;

        setIfPresent(articleModel, "name", articleBean.getTitle());
        setIfPresent(articleModel, "body/value", articleBean.getBody());
        setIfPresent(articleModel, "lead/value", articleBean.getLead());
        setIfPresent(articleModel, "editorialNotes/value", articleBean.getEditorialNotes());
        setIfPresent(articleModel, "priority/intValue", articleBean.getPriority());
        setIfPresent(articleModel, "related", articleBean.getRelatedArticles());
        setIfPresent(articleModel, "custom/namesAndValues", articleBean.getCustomAttributes());
    }

    private List<AdapterContentId> getAdapterContentIdList(Model articleModel, String listName)
    {
        @SuppressWarnings("unchecked")
        List<Model> images = get(articleModel, listName + "/list", List.class);
        List<AdapterContentId> adaptedList = new ArrayList<AdapterContentId>(images.size());
        for (Model model : images)
        {
            String contentIdString = get(model, "contentId", ContentId.class).getContentIdString();
            adaptedList.add(new AdapterContentId(contentIdString));
        }

        return adaptedList;
    }

    private void setIfPresent(Model model, String name, Object value) {
        if (value == null) {
            return;
        }
        set(model, name, value);
    }
}
