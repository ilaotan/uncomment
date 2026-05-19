
package com.ilaotan.uncomment;

import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;

import java.util.Optional;
import java.util.stream.Stream;

public class TestCleanupRecipe {


    public static void main(String[] args) {
        // 创建测试用的Java代码，包含各种注释
        String testCode ="/* 这是一个文件级别的多行注释 */\n" +
                "            package com.example;\n" +
                "            \n" +
                "            import io.swagger.v3.oas.annotations.media.Schema;\n" +
                "            import java.util.Arrays;\n" +
                "            import java.util.List;\n" +
                "            \n" +
                "            /**\n" +
                "             * 这是一个类的Javadoc注释\n" +
                "             */\n" +
                "            public class TestClass {\n" +
                "                private static final byte START_SYMBOL = 0x63;        // 起始符\n" +
                "                private static final byte HEADER_LENGTH = 0x20;       // 报文头长度\n" +
                "                private static final byte PROTOCOL_VERSION = 0x03;    // 协议版本号\n" +
                "                private static final byte ENCODING_TYPE = 0x01;       // 消息体编码类型(Protobuf)\n" +
                "               \n" +
                "    \n" +
                "                /* 这是一个字段的多行注释 */\n" +
                "                @Schema(name = \"TotalPower\", description = \"累计充电量\", requiredMode = Schema.RequiredMode.REQUIRED)\n" +
                "                private String field1;\n" +
                "                \n" +
                "                // 这是一个字段的单行注释\n" +
                "                private int field2;\n" +
                "                \n" +
                "                /**\n" +
                "                 * 这是一个方法的Javadoc注释\n" +
                "                 */\n" +
                "                public void testMethod() {\n" +
                "                    /* 这是方法内的多行注释 */\n" +
                "                    // 这是方法内的单行注释\n" +
                "                    //这是方法内的单行注释2\n" +
                "                    int x = 10; // 这是行内注释\n" +
                "                    //这里是if判断\n" +
                "                    if(1=1) {\n" +
                "                        //if判断的内部\n" +
                "                        try{\n" +
                "                            // try的内部\n" +
                "                            int a = 10; // 这是行内注释\n" +
                "                        }catch(Exception e){\n" +
                "                            // 出异常了\n" +
                "                            e.printStackTrace();\n" +
                "                        }\n" +
                "                    }else {\n" +
                "                        //else判断的内部\n" +
                "                        int b = 10; // 这是行内注释\n" +
                "                    }  \n" +
                "                    List<String> items = Arrays.asList(\"Apple\", \"Banana\", \"Cherry\");\n" +
                "                   items.forEach(one -> {\n" +
                "                       // 这是注释\n" +
                "                       System.out.println(one);\n" +
                "                   });\n" +
                "                    \n" +
                "                }\n" +
                "            }";

        System.out.println("原始代码:");
        System.out.println(testCode);
        System.out.println("\n清理注释后的代码:");

        try {
            // 使用OpenRewrite解析Java代码
            JavaParser parser = JavaParser.fromJavaVersion().build();

            // 解析代码
            try (Stream<SourceFile> sourceFiles = parser.parse(testCode)) {
                // 找到第一个CompilationUnit
                Optional<J.CompilationUnit> cuOpt = sourceFiles
                    .filter(file -> file instanceof J.CompilationUnit)
                    .map(file -> (J.CompilationUnit) file)
                    .findFirst();

                if (!cuOpt.isPresent()) {
                    System.out.println("无法解析Java代码为CompilationUnit");
                    return;
                }

                J.CompilationUnit cu = cuOpt.get();

                // 创建并应用我们的CleanupJavaCommentsRecipe
                CleanupJavaCommentsRecipe recipe = new CleanupJavaCommentsRecipe();
                ExecutionContext ctx = new InMemoryExecutionContext();

                // 获取Recipe的visitor并应用到编译单元
                J.CompilationUnit cleanedCu = (J.CompilationUnit) recipe.getVisitor().visit(cu, ctx);

                // 输出生成的代码
                String cleanedCode = cleanedCu.printAll();
                System.out.println(cleanedCode);
            }

        } catch (Exception e) {
            System.err.println("处理代码时出错:");
            e.printStackTrace();
        }
    }
}
