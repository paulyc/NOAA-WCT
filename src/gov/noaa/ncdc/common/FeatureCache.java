package gov.noaa.ncdc.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

/**
 * This class creates an in-memory cache of all features in a FeatureSource. The
 * cache is keyed on the value of the specified attribute. This implies that a
 * user can randomly access any feature in the cache by simply passing the value
 * of the key attribute.
 * 
 * @author Scott Melby (www.turnkey-technology.com)
 */
public final class FeatureCache {
    /**
     * The key attribute name.
     */
    private String m_keyAttr = null;

    /**
     * The feature cache.
     * 
     * key - Object: Value of the key attribute that was specified at
     * construction time. Value - Feature: The feature.
     */
    private Hashtable m_cache = new Hashtable();

    /**
     * Features that have duplicate keys.
     */
    private ArrayList m_dupKeyFeatures = new ArrayList();

    /**
     * Features that have no value for the key attribute.
     */
    private ArrayList m_noKeyFeatures = new ArrayList();

    /**
     * Creates a new FeatureCache that stores all features in memory using the
     * value of the specified attribute as the key.
     * 
     * @param source -
     *            The feature source to obtain features from.
     * @param keyAttr -
     *            The attribute that this cache will be keyed on.
     */
    public FeatureCache(FeatureSource source, String keyAttr)
            throws IOException {
        m_keyAttr = keyAttr;

        FeatureResults results = source.getFeatures();
        FeatureCollection collection = results.collection();
        FeatureIterator iterator = collection.features();
        while (iterator.hasNext()) {
            Feature feature = iterator.next();

            Object key = feature.getAttribute(keyAttr);
            if (key instanceof String) {
                String keyStr = (String) key;
                if (keyStr.trim().length() == 0) {
                    key = null;
                }
            }

            if (key != null) {
                // Found a value for the key attribute, cache the feature
                Feature test = (Feature) m_cache.put(key, feature);
                if (test != null) {
                    // cache already had a feature with the specified key,
                    // store the one we replaced off for data debugging
                    m_dupKeyFeatures.add(test);
                    
                    System.out.println("..... adding dup feature: "+test.getAttribute(keyAttr));
                }
            } else {
                // can't cache the feature since no good value for key attr
                // store the feature off for data debugging
                m_noKeyFeatures.add(feature);
            }
        }
    }

    /**
     * Returns the Feature with the specified attribute value if any feature has
     * this value, null if no features match.
     * 
     * @param attrValue -
     *            The value of the key attribute for the desired feature.
     */
    public Feature getFeature(Object attrValue) {
        return (Feature) m_cache.get(attrValue);
    }

    /**
     * Convenience method that will return the WKT feature geometry for the
     * feature with the specified attribute value if a matching feature is
     * cached, null otherwise.
     * 
     * @param attrValue -
     *            The value of the key attribute for the desired feature.
     */
    public String getFeatureGeometryWKT(Object attrValue) {
        String ret = null;

        Feature feature = getFeature(attrValue);
        if (feature != null) {
            ret = feature.getDefaultGeometry().toText();
        }

        return ret;
    }

    public void clear() {
    	m_cache.clear();
    	m_dupKeyFeatures.clear();
    	m_noKeyFeatures.clear();
    }
    
    /**
     * Returns the key attribute for this cache.
     */
    public String getKeyAttr() {
        return m_keyAttr;
    }

    /**
     * Returns the total number of features in the cache.
     */
    public int getFeatureCount() {
        return m_cache.size();
    }

    /**
     * Returns all features that were removed from the cache because another
     * feature with the same key value was added. This will be a zero length
     * array if no duplicate key values were encountered.
     */
    public Feature[] getDupKeyFeatures() {
        return (Feature[]) m_dupKeyFeatures.toArray(new Feature[0]);
    }

    /**
     * Returns all features that were not cached because they returned no value
     * (null or zero length string) for the key attribute. This will be a zero
     * length array if all Features had a valid value for the key attribute.
     */
    public Feature[] getNoKeyFeatures() {
        return (Feature[]) m_noKeyFeatures.toArray(new Feature[0]);
    }

    /**
     * Returns all cached features. Note: this does not include features that
     * did not have a value for the key attribute etc.
     */
    public Feature[] getCachedFeatures() {
        return (Feature[]) m_cache.values().toArray(new Feature[0]);
    }

    
    
    public List<String> getKeys() {
    	ArrayList<String> keyList = new ArrayList<String>();
    	Enumeration<Object> enumer = m_cache.keys();
    	while(enumer.hasMoreElements()) {
    		keyList.add(enumer.nextElement().toString());
    	}
    	return keyList;
    }
}
