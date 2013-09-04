## Content Hub adapter for Greenfield Online

This directory contains example source plugins that can be used to
make content from one Greenfield Online installation available in
another Greenfield Online installation, using a Content Hub adapter.
There are two plugins, one for the installation that is to be adapted
and one for the installation that should access adapted content.

This example adapts two content types, standard articles and images.

### Common

The `common` module contains data beans used to transfer content from
adapter to content hub. It is used by both the plugins, on the web
service end and the content hub end.


### Adapter web service plugin

The plugin containing the code for setting up an adapter web service
on top of a Greenfield Online installation is found in the `adapter`
module.  It consists of:

 * a custom model controller, ImageModelController, that convert from
   example.Image contents to the data bean.

 * controller-mapping.xml which sets up the Data API so the controller
   is used for the corresponding input template in the "greenfield"
   variant.

 * changes-service-config.xml, which configures the changes service to
   include only major 1 and to use the input template filter.

 * input-template-filter-config.xml, which configures the input
   template filter to accept example.Image and example.StandardArticle.

The content hub must depend on this plugin in order to make content
accessible to a content hub in another system.


### Content Hub plugin

The plugin for for adding an adapter to content hub is found in the
`contenthub` module. It consists of:

 * adapter_greenfield.xml, which configures content hub to use the
   adapter as major 1000, "Greenfield". The host and port are set
   using properties when building the plugin, defaulting to localhost
   on port 9090.

 * *-type.xml, which associates the corresponding bean from the
   `common` module with...

 * *-template.xml, which is an input template for the wrapper content
   for the corresponding content type from the other system.

 * *-index-mapping.xml, which sets up index mappings for the wrapper
   contents.

 * permissions.xml, which makes the external contents from the other
   system readable to everyone.

Every module that is to use adapted content should depend on this
plugin, since it pulls in the data bean classes that are needed to
access the content.
