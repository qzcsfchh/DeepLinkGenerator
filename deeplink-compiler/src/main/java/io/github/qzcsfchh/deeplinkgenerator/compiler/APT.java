package io.github.qzcsfchh.deeplinkgenerator.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

class APT {
    public Messager messager;
    public Elements elements;
    public Types typeUtils;
    public Filer filer;
    public Map<String, String> options;

    private static final class Holder {
        private static final APT INSTANCE = new APT();
    }

    private APT() {
    }

    public static void init(ProcessingEnvironment processingEnv) {
        Holder.INSTANCE.messager = processingEnv.getMessager();
        Holder.INSTANCE.elements = processingEnv.getElementUtils();
        Holder.INSTANCE.typeUtils = processingEnv.getTypeUtils();
        Holder.INSTANCE.filer = processingEnv.getFiler();
        Holder.INSTANCE.options = processingEnv.getOptions();
    }

    public static void trace(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, false);
        e.printStackTrace(pw);
        pw.flush();
        e(sw.toString());
    }

    public static void v(CharSequence log) {
        Holder.INSTANCE.messager.printMessage(Diagnostic.Kind.OTHER, log);
    }

    public static void d(CharSequence log) {
        Holder.INSTANCE.messager.printMessage(Diagnostic.Kind.NOTE, log);
    }

    public static void i(CharSequence log) {
        Holder.INSTANCE.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, log);
    }

    public static void w(CharSequence log) {
        Holder.INSTANCE.messager.printMessage(Diagnostic.Kind.WARNING, log);
    }

    public static void e(CharSequence log) {
        Holder.INSTANCE.messager.printMessage(Diagnostic.Kind.ERROR, log);
    }

    public static Messager getMessager() {
        return Holder.INSTANCE.messager;
    }

    public static Elements getElements() {
        return Holder.INSTANCE.elements;
    }

    public static Types getTypeUtils() {
        return Holder.INSTANCE.typeUtils;
    }

    public static Filer getFiler() {
        return Holder.INSTANCE.filer;
    }

    public static Map<String, String> getOptions() {
        return Holder.INSTANCE.options;
    }
}
