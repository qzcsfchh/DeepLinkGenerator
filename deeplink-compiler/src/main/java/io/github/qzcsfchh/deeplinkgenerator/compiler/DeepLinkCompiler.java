package io.github.qzcsfchh.deeplinkgenerator.compiler;

import com.google.auto.service.AutoService;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import io.github.qzcsfchh.deeplinkgenerator.annotation.DeepLink;
import io.github.qzcsfchh.deeplinkgenerator.annotation.DeepLinkEntity;

@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.qzcsfchh.deeplinkgenerator.annotation.DeepLink")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DeepLinkCompiler extends AbstractProcessor {
    private final List<DeepLinkEntity> mDeepLinks = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        APT.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new LinkedHashSet<String>(){{
            add(DeepLink.class.getCanonicalName());
        }};
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 采集所有注解
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(DeepLink.class);
        for (Element element : elements) {
            TypeMirror type = element.asType();
            DeepLink deepLink = element.getAnnotation(DeepLink.class);
            DeepLinkEntity entity = new DeepLinkEntity();
            entity.setAction(deepLink.action());
            if (entity.getAction() == null || entity.getAction().isEmpty()) {
                String simpleName = element.getSimpleName().toString();
                entity.setAction(simpleName.substring(0,1).toLowerCase()+simpleName.substring(1));
            }
            entity.setExported(deepLink.exported());
            entity.setFullClass(type.toString());
            entity.setHost(Objects.requireNonNull(deepLink.host()));
            entity.setScheme(Objects.requireNonNull(deepLink.scheme()));
            mDeepLinks.add(entity);
        }
        // 生成xml
        if (!mDeepLinks.isEmpty()) {
            generateXml();
        } else {
            APT.w("no deepLink config found.");
        }
        return true;
    }



    private void generateXml(){
        APT.v(">>> start generating deepLink.xml");
        SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try {
            // 获取指定目录
            FileObject fo = APT.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", "deepLink");
            URI uri = fo.toUri();
            String path = uri.getPath();
            String rootPath = path.substring(1, path.indexOf("/build") + "/build".length());
            rootPath += "/tmp/deepLink";
            File dir = new File(rootPath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("failed to create deepLink directory: " + rootPath);
            }
            File file = new File(rootPath, "deepLink.xml");
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("failed to create deepLink.xml");
            }
            APT.d("uri  = " + uri);
            APT.d("rootPath  = " + rootPath);

            // 处理xml写逻辑
            TransformerHandler handler = factory.newTransformerHandler();
            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            Result result = new StreamResult(new FileOutputStream(file));
            handler.setResult(result);

            handler.startDocument();
            AttributesImpl attr = new AttributesImpl();
            handler.startElement("","","deepLink",attr);
            attr.clear();

            for (DeepLinkEntity deepLink : mDeepLinks) {
                attr.addAttribute("", "", "class", "", deepLink.getFullClass());
                attr.addAttribute("", "", "action", "", deepLink.getAction());
                attr.addAttribute("","","scheme","",deepLink.getScheme());
                attr.addAttribute("","","host","",deepLink.getHost());
                attr.addAttribute("","","exported","",String.valueOf(deepLink.isExported()));
                handler.startElement("", "", "item", attr);
                handler.endElement("","","item");
                attr.clear();
            }

            handler.endElement("","","deepLink");
            handler.endDocument();
            APT.v("<<< finish generating deepLink.xml");
        } catch (TransformerConfigurationException | IOException | SAXException e) {
            APT.trace(e);
            throw new RuntimeException(e);
        }
    }
}