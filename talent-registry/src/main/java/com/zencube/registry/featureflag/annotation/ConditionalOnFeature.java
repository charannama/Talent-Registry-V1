package com.zencube.registry.featureflag.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to protect a Controller method or class behind a Feature Flag.
 * If the specified feature flag is disabled, the request will be intercepted
 * by the FeatureFlagAspect and a FeatureDisabledException will be thrown.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalOnFeature {
    
    /**
     * The key of the feature flag to check (e.g., "INTERVIEW_MODULE")
     */
    String value();
}
