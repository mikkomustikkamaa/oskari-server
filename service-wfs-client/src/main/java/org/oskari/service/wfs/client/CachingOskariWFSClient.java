package org.oskari.service.wfs.client;

import java.util.concurrent.TimeUnit;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.cache.ComputeOnceCache;

public class CachingOskariWFSClient extends OskariWFSClient {

    private static final String CACHE_NAME = CachingOskariWFSClient.class.getName();
    private static final int CACHE_SIZE_LIMIT = 10000;
    private static final long CACHE_EXPIRATION = TimeUnit.MINUTES.toMillis(5L);

    // Consider using Redis for caching (how much does serialization/deserialization to GeoJSON add?)
    private final ComputeOnceCache<SimpleFeatureCollection> cache;

    public CachingOskariWFSClient() {
        cache = CacheManager.getCache(CACHE_NAME,
                () -> new ComputeOnceCache<>(CACHE_SIZE_LIMIT, CACHE_EXPIRATION));
    }

    @Override
    public SimpleFeatureCollection getFeatures(String endPoint, String version,
            String user, String pass, String typeName,
            ReferencedEnvelope bbox, CoordinateReferenceSystem crs,
            int maxFeatures, Filter filter) {
        if (filter != null) {
            // Don't cache requests with a Filter
            return super.getFeatures(endPoint, version, user, pass,
                    typeName, bbox, crs, maxFeatures, filter);
        }
        String key = getCacheKey(endPoint, typeName, bbox, crs, maxFeatures);
        return cache.get(key,
                __ -> super.getFeatures(endPoint, version, user, pass,
                        typeName, bbox, crs, maxFeatures, filter));
    }

    private String getCacheKey(String endPoint, String typeName, Envelope bbox,
            CoordinateReferenceSystem crs, int maxFeatures) {
        String bboxStr = bbox != null ? bbox.toString() : "null";
        String maxFeaturesStr = Integer.toString(maxFeatures);
        return String.join(",", endPoint, typeName, bboxStr,
                crs.getIdentifiers().iterator().next().toString(), maxFeaturesStr);
    }

}
