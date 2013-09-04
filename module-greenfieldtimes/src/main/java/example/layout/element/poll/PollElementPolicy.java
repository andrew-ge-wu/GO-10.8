package example.layout.element.poll;

import com.polopoly.cm.client.CMException;
import com.polopoly.model.ModelTypeDescription;
import com.polopoly.poll.policy.QuestionPolicy;
import com.polopoly.poll.policy.SinglePollArticlePolicy;
import com.polopoly.poll.policy.SinglePollPolicy;

import example.layout.element.ElementPolicy;

/**
 * Policy for poll elements with one question. The class implements required
 * {@link com.polopoly.poll.policy.SinglePollArticlePolicy}
 */
public class PollElementPolicy extends ElementPolicy implements SinglePollArticlePolicy,
        ModelTypeDescription {
    public QuestionPolicy getQuestion() throws CMException
    {
        return (QuestionPolicy) this.getChildPolicy("questionField");
    }

    public SinglePollPolicy getSinglePoll() throws CMException
    {
        return (SinglePollPolicy) this.getChildPolicy("singlePollField");
    }
}
