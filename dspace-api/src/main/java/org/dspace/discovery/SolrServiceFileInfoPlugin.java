/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.core.Context;
import org.dspace.discovery.indexobject.IndexableItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * Adds filenames and file descriptions of all files in the ORIGINAL bundle
 * to the Solr search index.
 *
 * <p>
 * To activate the plugin, add the following line to discovery.xml
 * <pre>
 * {@code <bean id="solrServiceFileInfoPlugin" class="org.dspace.discovery.SolrServiceFileInfoPlugin"/>}
 * </pre>
 *
 * <p>
 * After activating the plugin, rebuild the discovery index by executing:
 * <pre>
 * [dspace]/bin/dspace index-discovery -b
 * </pre>
 *
 * @author Martin Walk
 */
public class SolrServiceFileInfoPlugin implements SolrServiceIndexPlugin {
    private static final String BUNDLE_NAME = "ORIGINAL";
    private static final String SOLR_FIELD_NAME_FOR_FILENAMES = "original_bundle_filenames";
    private static final String SOLR_FIELD_NAME_FOR_DESCRIPTIONS = "original_bundle_descriptions";

    @Autowired
    BundleService bundleService;

    @Autowired
    BitstreamService bitstreamService;

    @Override
    public void additionalIndex(Context context, IndexableObject indexableObject, SolrInputDocument document) {
        if (indexableObject instanceof IndexableItem) {
            Item item = ((IndexableItem) indexableObject).getIndexedObject();
            List<Bundle> bundles = item.getBundles();
            if (bundles != null) {
                for (Bundle bundle : bundles) {
                    String bundleName = bundleService.getName(bundle);
                    if ((bundleName != null) && bundleName.equals(BUNDLE_NAME)) {
                        List<Bitstream> bitstreams = bundle.getBitstreams();
                        if (bitstreams != null) {
                            for (Bitstream bitstream : bitstreams) {
                                document.addField(SOLR_FIELD_NAME_FOR_FILENAMES,
                                                  bitstreamService.getName(bitstream)
                                );
                                // Add _keyword and _filter fields which are necessary to support filtering and faceting
                                // for the file names
                                document.addField(SOLR_FIELD_NAME_FOR_FILENAMES + "_keyword",
                                                  bitstreamService.getName(bitstream)
                                );
                                document.addField(SOLR_FIELD_NAME_FOR_FILENAMES + "_filter",
                                                  bitstreamService.getName(bitstream)
                                );

                                String description = bitstreamService.getDescription(bitstream);
                                if ((description != null) && !description.isEmpty()) {
                                    document.addField(SOLR_FIELD_NAME_FOR_DESCRIPTIONS, description);
                                    // Add _keyword and _filter fields which are necessary to support filtering and
                                    // faceting for the descriptions
                                    document.addField(SOLR_FIELD_NAME_FOR_DESCRIPTIONS + "_keyword", description);
                                    document.addField(SOLR_FIELD_NAME_FOR_DESCRIPTIONS + "_filter", description);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
