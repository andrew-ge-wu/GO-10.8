package example.collection;

import java.util.List;
import java.util.ListIterator;

import com.atex.plugins.baseline.collection.ContentListLazy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;

/**
 * Content list lazy that can indicate if the list is complete or not.
 */
public class ContentListLazyImpl
    implements ContentListLazy
{
    private final ContentList contentList;
    private final boolean isComplete;

    public ContentListLazyImpl(ContentList contentList, boolean isComplete)
    {
        super();
        this.contentList = contentList;
        this.isComplete = isComplete;
    }

    @Deprecated
    public ContentId add(int index,
            ContentId referredContentId,
            boolean createTocEntry)
        throws CMException
    {
        return contentList.add(index, referredContentId, createTocEntry);
    }

    public void add(int index, ContentReference contentRef)
        throws CMException
    {
        contentList.add(index, contentRef);
    }

    @Deprecated
    public List<ContentId> add(int index, List<ContentId> referredContentIds, boolean createTocEntries)
        throws CMException
    {
        return contentList.add(index, referredContentIds, createTocEntries);
    }

    public void add(int index, List<ContentReference> contentRefs)
        throws CMException
    {
        contentList.add(index, contentRefs);
    }

    public boolean allowAddToFullList()
        throws CMException
    {
        return contentList.allowAddToFullList();
    }

    public String getContentListStorageGroup()
        throws CMException
    {
        return contentList.getContentListStorageGroup();
    }

    public ContentReference getEntry(int index)
        throws CMException
    {
        return contentList.getEntry(index);
    }

    public ListIterator<ContentReference> getListIterator()
    {
        return contentList.getListIterator();
    }

    public int getMaxSize()
        throws CMException
    {
        return contentList.getMaxSize();
    }

    @Deprecated
    public ContentId getReferredContentId(int index)
        throws CMException
    {
        return contentList.getReferredContentId(index);
    }

    @Deprecated
    public List<ContentId> getReferredContentIds()
        throws CMException
    {
        return contentList.getReferredContentIds();
    }

    @Deprecated
    public ContentId getTocEntryId(int index)
        throws CMException
    {
        return contentList.getTocEntryId(index);
    }

    @Deprecated
    public List<ContentId> getTocEntryIds()
        throws CMException
    {
        return contentList.getTocEntryIds();
    }

    @Deprecated
    public int indexOf(ContentId contentId)
        throws CMException
    {
        return contentList.indexOf(contentId);
    }

    public boolean isReadOnly()
    {
        return contentList.isReadOnly();
    }

    public void move(int oldIndex, int newIndex)
        throws CMException
    {
        contentList.move(oldIndex, newIndex);
    }

    public void rearrange(int[] newOrder)
        throws CMException
    {
        contentList.rearrange(newOrder);
    }

    public Object remove(int index)
    {
        return contentList.remove(index);
    }

    public void remove(int[] indices)
        throws CMException
    {
        contentList.remove(indices);
    }

    public void rename(String newContentReferenceGroupName)
        throws CMException
    {
        contentList.rename(newContentReferenceGroupName);
    }

    public void setAllowAddToFullList(boolean addToFullList)
        throws CMException
    {
        contentList.setAllowAddToFullList(addToFullList);
    }

    public void setEntry(int index, ContentReference contentRef)
        throws CMException
    {
        contentList.setEntry(index, contentRef);
    }

    public void setMaxSize(int maxSize)
        throws CMException
    {
        contentList.setMaxSize(maxSize);
    }

    public int size()
    {
        return contentList.size();
    }

    public boolean isComplete()
    {
        return isComplete;
    }
}
