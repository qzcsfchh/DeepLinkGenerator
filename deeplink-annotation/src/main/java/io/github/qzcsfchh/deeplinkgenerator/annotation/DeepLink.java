package io.github.qzcsfchh.deeplinkgenerator.annotation;

import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface DeepLink {
    @NonNull String action() default "";
    @NonNull String scheme();
    @NonNull String host();
    boolean exported() default true;
}
