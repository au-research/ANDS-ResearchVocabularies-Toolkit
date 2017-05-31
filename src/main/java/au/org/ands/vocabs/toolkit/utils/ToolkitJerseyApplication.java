/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.utils;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/** The definition of the Toolkit Jersey web application.
 */
public class ToolkitJerseyApplication extends ResourceConfig {

    /** Register providers not otherwise found by Jersey.
     * That includes our own providers, as well as Swagger,
     * and Jersey's multipart support.
     */
    public ToolkitJerseyApplication() {
        // Our own providers.
        packages("au.org.ands.vocabs");

        // Swagger.
        packages("io.swagger.jaxrs.listing");

        // Jersey support for multipart data, used for file uploads.
        packages("org.glassfish.jersey.examples.multipart");
        register(MultiPartFeature.class);
    }

}