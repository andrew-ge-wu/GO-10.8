package example.blog;

public interface HTMLValidator {

    String getCleanHTML(String dirtyHTML) throws InvalidHTMLException;
    String stripAllHTML(String dirtyHTML) throws InvalidHTMLException;

}