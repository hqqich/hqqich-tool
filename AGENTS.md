# 仓库指南

> 1. 跳过测试

## 项目结构与模块组织

本仓库是一个多模块 Maven 工具库项目。根目录 `pom.xml` 聚合了 `hqqich-tool-base`、`hqqich-tool-cat`、`hqqich-tool-csv`、`hqqich-tool-file`、`hqqich-tool-id-generator`、`hqqich-tool-kotlin-ext`、`hqqich-tool-cache`、`hqqich-tool-test` 和 `hqqich-tool-all` 等模块。Java 代码放在各模块的 `src/main/java`，Kotlin 代码放在 `src/main/kotlin`。测试代码应放在对应模块的 `src/test/java` 或 `src/test/kotlin`。各模块 README 用于说明使用示例。`maven-repo/` 是发布或生成的 Maven 仓库存储目录，非日常源码目录。

## 构建、测试与开发命令

- `mvn clean test`：运行整个 Maven reactor 的测试套件。
- `mvn -pl hqqich-tool-base -am test`：测试指定模块，并自动构建其依赖模块。
- `mvn clean package`：构建所有模块的 jar 和 source jar。
- `mvn deploy`：将构建产物发布到配置的本地 `maven-repo/` 路径；仅在需要更新发布产物时使用。

## 代码风格与命名规范

遵循 `.editorconfig`：UTF-8 编码、LF 换行、文件末尾保留换行、移除行尾空格。Java 和 Kotlin 使用 4 空格缩进，XML 使用 2 空格缩进。Java 类名使用 `PascalCase`，方法和字段使用 `camelCase`，包路径应位于 `io.github.hqqich.tool.<module>` 下。Kotlin 遵循父级 POM 中配置的官方 Kotlin 风格。工具类 API 应保持简洁，并尽量与所属模块职责一致。

## 测试规范

项目已配置 JUnit 5 和 `kotlin-test-junit5`。测试类建议使用 `Test` 后缀，例如 `DateUtilsTest`、`Sm4Test`。测试应放在被验证模块内部，不要把常规单元测试放入 `hqqich-tool-test`，该模块更适合示例或演示代码。新增工具方法时，应覆盖正常场景、空值或空集合、边界值，以及可能影响兼容性的行为。

## 提交与 Pull Request 规范

近期提交历史使用简短中文信息，例如 `增加cache模块`、`增加文档`。提交信息应简洁、聚焦模块，并使用命令式描述，例如 `修复csv转义处理` 或 `优化id生成器边界处理`。Pull Request 应说明受影响模块、主要 API 或行为变化、关联 issue，并列出已执行的验证命令。若公开用法发生变化，请同步更新相关 README 示例。

## 安全与配置提示

不要提交密钥、本地凭据或仅适用于个人 IDE 的运行配置。修改加解密、文件处理、ID 生成等工具时，应优先保持向后兼容，并补充边界测试。除非任务是发布或更新构建产物，否则避免直接编辑 `maven-repo/`。
