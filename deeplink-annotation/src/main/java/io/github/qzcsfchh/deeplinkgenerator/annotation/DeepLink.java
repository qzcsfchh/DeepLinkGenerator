package io.github.qzcsfchh.deeplinkgenerator.annotation;

import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface DeepLink {
    /**
     * 对应<code>Intent.action</code>，非必填
     * @return
     */
    @NonNull String action() default "";

    /**
     * 对应<code>intent-filter.data.scheme</code>，必填
     * @return
     */
    @NonNull String scheme();

    /**
     * 对应<code>intent-filter.data.host</code>，必填
     * @return
     */
    @NonNull String host();
    /**
     * 对应<code>intent-filter.data.path</code>，非必填
     * @return
     */
    @NonNull String path() default "";
    /**
     * 对应<code>activity.exported</code>，非必填
     * @return
     */
    boolean exported() default true;
}
