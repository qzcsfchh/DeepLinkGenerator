# 功能介绍
通过注解处理器+Gradle插件实现在manifest中为activity自动生成deeplink配置。
由于APT的执行时机是在`compile{variant}JavaWithJavac`这个Task中，而注入manifest是在`process{variant}Manifest`这个Task中，也就是说首次运行时执行注入时，配置文件deepLink.xml还未生成，因此修改DeepLink参数后需要先build一下module才能在下次run时生效。

# 实现原理
具体思路如下：
1. 自定义注解
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
然后使用`new Intent(your_action, Uri.parse("your_scheme://your_host/xxx?param1=1&param2=2"))`进行跳转。

2. 处理自定义注解
解析注解参数，生成对应配置文件，比如：deepLink.xml，后续gradle可以通过读取这些配置文件修改manifest文件生成上述deeplink配置。设计deepLink.xml的内容如下：
```xml
<deepLink>
    <item class='com.example.MyActivity' action='myActivity' scheme='myapp' host='mymodule' exported='true'/>
    <item class='com.example.MyActivity2' action='myActivity2' scheme='myapp' host='mymodule' exported='false'/>
</deepLink>
```

3. Gradle插件



# 参考资料
- [Android Gradle 实战之自动生成DeepLink配置信息](https://www.jianshu.com/p/7e29d18f5475)
