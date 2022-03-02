package io.github.qzcsfchh.deeplinkgenerator.buildsrc

import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeepLinkPlugin implements Plugin<Project>{

    @Override
    void apply(Project target) {
        target.afterEvaluate {
            def startTime = System.currentTimeMillis()
            target.logger.lifecycle "DeepLinkPlugin take effects."
            def android = target.extensions.getByName("android")
            android.applicationVariants.all { variant ->
                //ApplicationVariant 代表每一种构建版本,如debug，release
                //找到一个名如processDebugManifest的task,通过给他添加last闭包完成hook操作
                def variantName = variant.name.capitalize()
                def processManifestTask = target.tasks.getByName("process${variantName}Manifest")
                processManifestTask.doLast {
                    target.logger.lifecycle 'start transform manifest'
                    // 解析deepLink.xml文件
                    File deepLinkFile = new File(target.buildDir,"tmp/deepLink/deepLink.xml")
                    if (!deepLinkFile.exists()) {
                        target.logger.lifecycle "DeepLinkPlugin empty run."
                        return
                    }
                    XmlParser parser = new XmlParser()
                    Node root = parser.parse(deepLinkFile)
                    Map<String, Map<String, String>> deepLinkMaps = new LinkedHashMap<>()
                    root.children().forEach { Node node ->
                        Map<String, String> entry = new HashMap<>()
                        entry.put("action", node.attribute('action'))
                        entry.put("scheme", node.attribute('scheme'))
                        entry.put("host", node.attribute('host'))
                        entry.put("path", node.attribute('path'))
                        entry.put("exported", node.attribute('exported'))
                        deepLinkMaps.put(node.attribute('class'), entry)
                    }

                    // 获取最终的AndroidManifest.xml文件
                    String manifestPath = target.getBuildDir().getAbsolutePath() +
                            "/intermediates/merged_manifests/${variant.name}/AndroidManifest.xml"
                    File manifestFile = new File(manifestPath)
                    if (!manifestFile.exists()) {
                        throw new IOException('can not find manifestFile')
                    }
                    Node manifest = new XmlParser().parse(manifestFile)
                    Node application = manifest.get("application").get(0)
                    NodeList activities = application.get("activity")
                    activities.forEach { Node activity ->
                        target.logger.lifecycle "activity-> ${activity.attributes()}"
                        for (attr in activity.attributes()) {
                            String activityPath = attr.value
                            if (deepLinkMaps.containsKey(activityPath)) {
                                Map<String, String> deepLink = deepLinkMaps.get(activityPath)
                                NodeList intentFilters = activity.get("intent-filter")
                                if (intentFilters.size() > 0) {
                                    Node intentFilter = intentFilters.get(0)
                                    injectIntentFilter(intentFilter, deepLink)
                                    def stringConfig = XmlUtil.serialize(manifest)
                                    manifestFile.write(stringConfig)
                                    deepLinkMaps.remove(activityPath)
                                } else {
                                    application.remove(activity)
                                }
                                activity.attributes().put("android:exported", deepLink.get('exported'))
                                manifestFile.write(XmlUtil.serialize(manifest))
                                break
                            }
                        }
                    }

                    for (deepLinkEntry in deepLinkMaps) {
                        HashMap<String, String> activityMap = new HashMap<>()
                        activityMap.put("android:name", deepLinkEntry.key)
                        activityMap.put("android:exported", deepLinkEntry.value.get('exported'))
                        Node activity = application.appendNode("activity", activityMap)
                        Node intentFilter = activity.appendNode("intent-filter")

                        injectIntentFilter(intentFilter, deepLinkEntry.value)

                        manifestFile.write(XmlUtil.serialize(manifest))
                    }

                    target.logger.lifecycle "finish transform manifest, cost: ${System.currentTimeMillis() - startTime}ms"
                }

            }
        }
    }


    private static void injectIntentFilter(Node intentFilter, Map<String,String> deepLink){
        // 添加intent-filter.data
        HashMap<String, String> dataMap = new HashMap<>()
        dataMap.put("android:host", deepLink.get('host'))
        dataMap.put("android:scheme", deepLink.get('scheme'))
        def path = deepLink.get('path')
        if (path != null&&!path.isEmpty()) dataMap.put("android:path", deepLink.get('path'))
        intentFilter.appendNode("data", dataMap)

        // 添加intent-filter.action
        // 如果没有action则需要添加 <action android:name="android.intent.action.VIEW" />
        NodeList actions = intentFilter.get("action")
        if (actions.size() == 0) {
            intentFilter.appendNode("action", Collections.singletonMap("android:name", "android.intent.action.VIEW"))
        }
        intentFilter.appendNode("action", Collections.singletonMap("android:name", deepLink.get('action')))

        // 添加intent-filter.category
        NodeList categories = intentFilter.get("category")
        if (categories.size() < 2) {
            intentFilter.appendNode("category", Collections.singletonMap("android:name", "android.intent.category.DEFAULT"))
            intentFilter.appendNode("category", Collections.singletonMap("android:name", "android.intent.category.BROWSABLE"))
        }


    }
}
