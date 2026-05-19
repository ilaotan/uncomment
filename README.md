# uncomment

清理代码中注释，提升代码**可读性**。

## 项目信息

| 配置项 | 值 |
|--------|-----|
| groupId | `com.ilaotan` |
| artifactId | `uncomment` |
| JDK 版本 | 1.8 |

## 核心类

- `com.ilaotan.uncomment.CleanupJavaCommentsRecipe` - 清理代码中大部分的 `//`、`/* */` 等注释
- `com.ilaotan.uncomment.CleanupSwaggerRecipe` - 清理 Swagger 注解

## 使用方式

### 1. 安装依赖到本地仓库

```bash
mvn clean install -Dmaven.test.skip=true
```

### 2. 在目标项目中配置插件

编辑目标项目的 `pom.xml`，在 `<plugins>` 标签内增加以下插件配置：

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>6.24.0</version>
    <configuration>
        <activeRecipes>
            <!-- 删除代码中大部分的 // /**/ 等注释 -->
            <recipe>com.ilaotan.uncomment.CleanupJavaCommentsRecipe</recipe>
            <!-- 清理swagger注解 -->
            <recipe>com.ilaotan.uncomment.CleanupSwaggerRecipe</recipe>
        </activeRecipes>
        <!-- mvn rewrite:dryRun 将在 target/site/rewrite/ 下生成一个 HTML 页面，展示所有变更的详细信息 -->
        <exportDatatables>true</exportDatatables>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.ilaotan</groupId>
            <artifactId>uncomment</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.openrewrite</groupId>
            <artifactId>rewrite-java</artifactId>
            <version>8.67.1</version>
        </dependency>
        <dependency>
            <groupId>org.openrewrite</groupId>
            <artifactId>rewrite-core</artifactId>
            <version>8.67.1</version>
        </dependency>
        <dependency>
            <groupId>org.openrewrite</groupId>
            <artifactId>rewrite-java-8</artifactId>
            <version>8.67.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.20.0</version>
        </dependency>
    </dependencies>
</plugin>
```

### 3. 执行清理

**预览模式**（不实际修改文件，生成变更报告）：

```bash
mvn rewrite:dryRun -Dmaven.test.skip=true
```

执行后在 `target/site/rewrite/` 目录下会生成 HTML 页面，展示所有变更的详细信息。

**执行模式**（实际修改文件）：

```bash
mvn rewrite:run -Dmaven.test.skip=true
```

### 4. 验证

执行打包命令确认代码没有报错：

```bash
mvn clean package -Dmaven.test.skip=true
```
