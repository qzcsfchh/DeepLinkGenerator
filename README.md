# 功能介绍
通过注解处理器+Gradle插件实现在manifest中为activity自动生成deeplink配置。使用方法如下：

引入依赖：
```groovy
implementation 'io.github.qzcsfchh.deeplink:deeplink-annotation:1.0.0'
annotationProcessor 'io.github.qzcsfchh.deeplink:deeplink-compiler:1.0.0'
```

引入插件：
```groovy
plugins {
    id 'io.github.qzcsfchh.deeplink'
}
```

为需要使用deeplink的activity打上注解：
```java
@DeepLink(exported = false, host = "test2", scheme = "native")
public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("我是新的页面");
        Button button = new Button(this);
        button.setText("点击");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        button.setLayoutParams(lp);
        setContentView(button);
        button.setOnClickListener(view ->{
            startActivity(new Intent().setData(Uri.parse("native://test/mainActivity")));
        });
    }
}
```
然后make一下module，再运行即可。

# 实现原理
具体思路如下：
## 1. 自定义注解
属性包含：class、action、scheme、host、exported，分别对应deeplink相关属性。我们知道deeplink需要我们在manifest中生成如下配置：
```xml
<activity android:name=".MyActivity" android:exported="false">
    <intent-filter>
        <action android:name="your_action"/>
        <data android:host = "your_host" android:scheme="your_scheme"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</activity>
```
然后使用`new Intent(your_action, Uri.parse("your_scheme://your_host/xxx?param1=1&param2=2"))`或者`new Intent().setData(Uri.parse("your_scheme://your_host/xxx?param1=1&param2=2"))`进行跳转。

## 2. 处理自定义注解
解析注解参数，生成对应配置文件，比如：deepLink.xml，后续gradle可以通过读取这些配置文件修改manifest文件生成上述deeplink配置。设计deepLink.xml的内容如下：
```xml
<deepLink>
    <item class='com.example.MyActivity' action='myActivity' scheme='myapp' host='mymodule' exported='true' path=''/>
    <item class='com.example.MyActivity2' action='myActivity2' scheme='myapp' host='mymodule' exported='false' path=''/>
</deepLink>
```

## 3. Gradle插件
插件部分主要做三件事情：
1. hook`process{variant}Manifest`这个Task，在这个Task执行结束后对最终的manifest进行注入，而这个最终的manifest文件位于：
```groovy
// 获取最终的AndroidManifest.xml文件
String manifestPath = project.buildDir.getAbsolutePath() +
        "/intermediates/merged_manifests/${variant.name}/AndroidManifest.xml"
```
2. 读取上个步骤中生成的deepLink.xml配置文件
3. 根据配置文件对AndroidManifest.xml进行修改

> 已知问题：
> 1. 由于APT的执行时机是在`compile{variant}JavaWithJavac`这个Task中，而注入manifest是在`process{variant}Manifest`这个Task中，从构建过程看，`process{variant}Manifest`是在`compile{variant}JavaWithJavac`之前执行的，也就是说Gradle执行manifest注入时，配置文件deepLink.xml还未生成或更新，因此修改DeepLink参数后需要先make一下module才能在下次run时生效。
2. 由于增量编译的关系，`compile{variant}JavaWithJavac`不是每次run都会执行的，导致注入的逻辑不执行，此时手动删一下`build\intermediates\merged_manifests`这个目录即可。

# 参考资料
- [Android Gradle 实战之自动生成DeepLink配置信息](https://www.jianshu.com/p/7e29d18f5475)
