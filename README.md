# hqqich工具


| 模块                       | 名称       | 版本    |
|--------------------------|----------|-------|
| hqqich-tool-all          | 引用所有工具   | 1.0.3 |
| hqqich-tool-cat          | 加解密工具    | 1.0.3 |
| hqqich-tool-csv          | csv工具    | 1.0.3 |
| hqqich-tool-id-generator | id生成器    | 1.0.3 |
| hqqich-tool-kotlin-ext   | kotlin扩展 | 1.0.3 |
| hqqich-tool-file         | 文件操作相关   | 1.0.3 |

### maven项目使用

`pom.xml`中添加如下内容：

```xml
<project>
    <repositories>
        <repository>
            <id>io.github.hqqich</id>
            <name>hqqich-tool-all</name>
            <url>https://hqqich.github.io/hqqich-tool/maven-repo/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>io.github.hqqich</groupId>
            <artifactId>hqqich-tool-all</artifactId>
            <version>1.0.3</version>
        </dependency>
    </dependencies>
</project>
```


### kotlin-script使用

```kotlin
import io.github.hqqich.kotlinext.ext.yes

USE {
    repositories {
        maven {
            url = "https://hqqich.github.io/hqqich-tool/maven-repo"
        }
        dependencies {
            implementation("io.github.hqqich:hqqich-tool-all:1.0.3")
        }
    }
}



(1 == 1).yes {
    println("调用库成功")
}


println("Hello, world!")

```

```kotlin
@file:Repository("https://hqqich.github.io/hqqich-tool/maven-repo")  // 声明私有仓库
@file:DependsOn("io.github.hqqich:hqqich-tool-all:1.0.3")  // 声明依赖

(1 == 1).yes {
    println("调用库成功")
}


println("Hello, world!")
```
