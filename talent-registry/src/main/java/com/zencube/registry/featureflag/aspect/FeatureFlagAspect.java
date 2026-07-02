package com.zencube.registry.featureflag.aspect;

import com.zencube.registry.common.exception.FeatureDisabledException;
import com.zencube.registry.featureflag.annotation.ConditionalOnFeature;
import com.zencube.registry.featureflag.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class FeatureFlagAspect {

    private final FeatureFlagService featureFlagService;

    @Before("@annotation(com.zencube.registry.featureflag.annotation.ConditionalOnFeature)")
    public void checkFeatureFlag(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        ConditionalOnFeature annotation = method.getAnnotation(ConditionalOnFeature.class);
        if (annotation != null) {
            String flagKey = annotation.value();
            
            if (!featureFlagService.isEnabled(flagKey)) {
                log.warn("Access Denied: Feature {} is currently disabled. Target Method: {}", flagKey, method.getName());
                throw new FeatureDisabledException("The feature '" + flagKey + "' is currently disabled.");
            }
        }
    }
}
