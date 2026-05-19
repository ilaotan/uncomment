package com.ilaotan.uncomment;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class CleanupSwaggerRecipe extends Recipe {

    // Recipe 元信息
    @Override
    public String getDisplayName() {
        return "Swagger注解清理";
    }

    @Override
    public String getDescription() {
        return "清理Java代码中的Swagger @Schema注解，移除API文档元数据。";
    }

    // 核心：创建 Visitor 遍历并修改代码
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            // 处理注解，专门用于识别和移除@Schema注解
            @Override
            public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
                // 检查注解是否为@Schema
                if ("Schema".equals(annotation.getSimpleName())) {
                    // 直接返回null来移除整个@Schema注解
                    // 这是OpenRewrite中删除AST节点的标准方式
                    return null;
                }
                if ("ApiModelProperty".equals(annotation.getSimpleName())) {
                    // 直接返回null来移除整个@Schema注解
                    // 这是OpenRewrite中删除AST节点的标准方式
                    return null;
                }
                if ("ApiModel".equals(annotation.getSimpleName())) {
                    // 直接返回null来移除整个@Schema注解
                    // 这是OpenRewrite中删除AST节点的标准方式
                    return null;
                }
//                // 检查注解是否为@Schema
//                if ("Operation".equals(annotation.getSimpleName())) {
//                    // 直接返回null来移除整个@Schema注解
//                    // 这是OpenRewrite中删除AST节点的标准方式
//                    return null;
//                }

                // 对于其他注解，调用父类方法继续处理
                return super.visitAnnotation(annotation, ctx);
            }
        };
    }
}
