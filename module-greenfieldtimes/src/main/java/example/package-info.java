/**
This package documentation describes example fields, layouts and widgets implemented in the Greenfield Times application.

<p>
For extensive information on fields, layouts and widgets etc. implemented in Polopoly,
see the Polopoly javadoc (package <code>com.polopoly.cm.app</code>).
</p>

<a name="fields"/>
<h2>Fields</h2>

<table border="1" cellpadding="3" cellspacing="0" width="100%">
<th>Field</th>
<th>Short description</th>
</tr>
<tr>
<td><a href="#example.blogpostingbrowser">example.BlogPostingBrowser</a></td>
<td>Field for browsing blog postings.</td>
</tr>
<tr>
<td><a href="#example.parentbloglink">example.ParentBlogLink</a></td>
<td>Field for linking to parent blog.</td>
</tr>
</table>

<hr />

<a name="example.blogpostingbrowser" />
<h3>example.BlogPostingBrowser</h3>

<p>
The BlogPostingBrowser field displays a structured view of the blog postings
contained in the blog, and links to them. Displays the complete blog posting
history.
</p>

<h4>Example definitions in input template xml</h4>

<pre>
  &lt;field name=&quot;blogPostingBrowser&quot; input-template=&quot;example.BlogPostingBrowser&quot; label=&quot;cm.template.example.Blog.BlogHistory&quot; /&gt;
</pre>

<h4>Classes in the field</h4>

<table border="1" cellpadding="3" cellspacing="0">
<tr>
<th style="text-align: left">Policy</th>
<td>com.polopoly.cm.policy.ContentPolicy</td>
</tr>
<tr>
<th>Editor</th>
<td>{@link example.content.editorialblog.OBlogPostingBrowserPolicyWidget}</td>
</tr>
<tr>
<th>Viewer</th>
<td>{@link example.content.editorialblog.OBlogPostingBrowserPolicyWidget}</td>
</tr>
</table>

<hr />

<a name="example.parentbloglink" />
<h3>example.ParentBlogLink</h3>

<p>
The ParentBlogLink field displays a link to the parent blog using the security
parent. If the security parent is not a blog, this field will not display any link.
</p>

<h4>Example definitions in input template xml</h4>

<pre>
  &lt;field name=&quot;blog&quot; input-template=&quot;example.ParentBlogLink&quot; label=&quot;cm.template.example.Blog&quot; /&gt;
</pre>

<h4>Classes in the field</h4>

<table border="1" cellpadding="3" cellspacing="0">
<tr>
<th style="text-align: left">Policy</th>
<td>com.polopoly.cm.policy.ContentPolicy</td>
</tr>
<tr>
<th>Editor</th>
<td>{@link example.content.editorialblog.ParentBlogLinkPolicyWidget}</td>
</tr>
<tr>
<th>Viewer</th>
<td>{@link example.content.editorialblog.ParentBlogLinkPolicyWidget}</td>
</tr>
</table>

<hr />
</table>
 */
package example;