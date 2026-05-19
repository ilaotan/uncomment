package com.ilaotan.uncomment;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CleanupJavaCommentsRecipe extends Recipe {
    // 配置：是否清理空注释（默认开启）
    private final boolean cleanEmptyComments;
    // 配置：是否清理冗余模板注释（默认开启）
    private final boolean cleanTemplateComments;
    // 配置：是否清理重复代码注释（默认开启）
    private final boolean cleanDuplicateCodeComments;

    // 默认构造（开启所有清理规则）
    public CleanupJavaCommentsRecipe() {
        this(true, true, true);
    }

    // 自定义构造（支持关闭特定规则）
    public CleanupJavaCommentsRecipe(boolean cleanEmptyComments,
                                     boolean cleanTemplateComments,
                                     boolean cleanDuplicateCodeComments) {
        this.cleanEmptyComments = cleanEmptyComments;
        this.cleanTemplateComments = cleanTemplateComments;
        this.cleanDuplicateCodeComments = cleanDuplicateCodeComments;
    }

    // Recipe 元信息
    @Override
    public String getDisplayName() {
        return "Java 代码注释清理";
    }

    @Override
    public String getDescription() {
        return "清理 Java 代码中的空注释、冗余模板注释和重复代码注释，提升代码可读性。";
    }


    // 核心：创建 Visitor 遍历并修改代码
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            // 添加 visitSpace 方法实现，用于清理空间中的注释，同时保持代码格式
            @Override
             public Space visitSpace(Space space, Space.Location location, ExecutionContext ctx) {
                 // 调用父类方法处理基本逻辑
                 space = super.visitSpace(space, location, ctx);

                 // 如果space为空或没有注释，直接返回
                 if (space == null || space.getComments().isEmpty()) {
                     return space;
                 }

                 // 移除所有注释但保持原有的空白字符
                 return space.withComments(Collections.emptyList());
             }
            // 清理编译单元的所有注释
            @Override
            public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                // 清理编译单元级别的注释
                cu = cu.withComments(Collections.emptyList());
                return super.visitCompilationUnit(cu, ctx);
            }

            // 清理类声明的注释
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                // 清理类声明的前缀注释（包括Javadoc）
                classDecl = classDecl.withPrefix(classDecl.getPrefix().withComments(Collections.emptyList()));
                classDecl = super.visitClassDeclaration(classDecl, ctx);

                // 使用标准API处理类声明的后缀，避免反射
                classDecl = classDecl.withPrefix(visitSpace(classDecl.getPrefix(), Space.Location.CLASS_DECLARATION_PREFIX, ctx));

                return classDecl;
            }

            // 清理方法声明的注释
            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDecl, ExecutionContext ctx) {
                // 清理方法声明的前缀注释（包括Javadoc）
                methodDecl = methodDecl.withPrefix(methodDecl.getPrefix().withComments(Collections.emptyList()));

                // 清理方法声明的所有注释
                methodDecl = methodDecl.withComments(Collections.emptyList());

                // 特别处理方法体中的注释
                if (methodDecl.getBody() != null) {
                    J.Block body = methodDecl.getBody();

                    // 清理方法体的前缀注释
                    body = body.withPrefix(body.getPrefix().withComments(Collections.emptyList()));

                    // 清理方法体的所有注释
                    body = body.withComments(Collections.emptyList());

                    // 更新方法体
                    methodDecl = methodDecl.withBody(body);
                }

                // 调用父类方法确保递归处理所有子节点
                methodDecl = super.visitMethodDeclaration(methodDecl, ctx);

                // 使用标准API处理方法声明的后缀，避免反射
                methodDecl = methodDecl.withPrefix(visitSpace(methodDecl.getPrefix(), Space.Location.METHOD_DECLARATION_PREFIX, ctx));

                return methodDecl;
            }

            // 处理变量声明的命名变量部分，这里可能包含行内注释
            @Override
            public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable variable, ExecutionContext ctx) {
                // 清理变量名的前缀注释
                variable = variable.withPrefix(variable.getPrefix().withComments(Collections.emptyList()));

                // 清理变量相关的所有注释
                variable = variable.withComments(Collections.emptyList());

                // 处理变量初始化表达式
                if (variable.getInitializer() != null) {
                    // 链式调用清理初始化表达式的注释
                    variable = variable.withInitializer(
                        variable.getInitializer()
                            .withPrefix(variable.getInitializer().getPrefix().withComments(Collections.emptyList()))
                            .withComments(Collections.emptyList())
                    );
                }

                return variable;
            }

            // 处理变量声明，清理变量声明中的注释
            @Override
            public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations variableDecls, ExecutionContext ctx) {
                // 先调用父类方法确保递归处理所有子节点
                variableDecls = super.visitVariableDeclarations(variableDecls, ctx);

                // 清理变量声明的前缀注释
                Space prefix = variableDecls.getPrefix();
                if (prefix != null) {
                    variableDecls = variableDecls.withPrefix(prefix.withComments(Collections.emptyList()));
                }

                // 清理变量声明的所有注释
                variableDecls = variableDecls.withComments(Collections.emptyList());

                // 清理变量声明的每个变量名和初始化表达式的注释
                if (variableDecls.getVariables() != null) {
                    List<J.VariableDeclarations.NamedVariable> cleanedVariables = new ArrayList<>();
                    for (J.VariableDeclarations.NamedVariable var : variableDecls.getVariables()) {
                        if (var != null) {
                            // 清理变量名的前缀和主体中的注释
                            var = var.withPrefix(var.getPrefix().withComments(Collections.emptyList()));
                            var = var.withComments(Collections.emptyList());

                            // 清理初始化表达式的注释
                            if (var.getInitializer() != null) {
                                var = var.withInitializer(
                                    var.getInitializer()
                                        .withPrefix(var.getInitializer().getPrefix().withComments(Collections.emptyList()))
                                        .withComments(Collections.emptyList())
                                );
                            }

                            cleanedVariables.add(var);
                        }
                    }
                    variableDecls = variableDecls.withVariables(cleanedVariables);
                }

                return variableDecls;
            }

            // 简化实现，移除不支持的ExpressionStatement处理

            // 处理块语句，清理块内注释，确保语句间保留适当的换行符
            @Override
            public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
                // 清理块的前缀注释
                block = block.withPrefix(block.getPrefix().withComments(Collections.emptyList()));

                // 清理块中的所有注释
                block = block.withComments(Collections.emptyList());

                //、这句 会造成多行挤一起
                // 先调用父类方法确保递归处理所有子节点，包括嵌套块
                block = super.visitBlock(block, ctx);

                // 使用标准API处理块的后缀，避免反射
                block = block.withPrefix(visitSpace(block.getPrefix(), Space.Location.BLOCK_PREFIX, ctx));

                return block;
            }

            // 处理If语句
            @Override
            public J.If visitIf(J.If ifStmt, ExecutionContext ctx) {
                // 清理If语句的前缀注释
                ifStmt = ifStmt.withPrefix(ifStmt.getPrefix().withComments(Collections.emptyList()));

                // 清理If语句的所有注释
                ifStmt = ifStmt.withComments(Collections.emptyList());

                // 处理条件表达式
                if (ifStmt.getIfCondition() != null) {
                    ifStmt = ifStmt.withIfCondition(
                        ifStmt.getIfCondition()
                            .withPrefix(ifStmt.getIfCondition().getPrefix().withComments(Collections.emptyList()))
                            .withComments(Collections.emptyList())
                    );
                }

                // 处理then部分
                if (ifStmt.getThenPart() != null) {
                    Statement thenPart = ifStmt.getThenPart();
                    // 清理then部分的注释
                    thenPart = thenPart.withPrefix(thenPart.getPrefix().withComments(Collections.emptyList()));
                    thenPart = thenPart.withComments(Collections.emptyList());

                    // 如果then部分是块，使用visitBlock处理确保行内注释被清理
                    if (thenPart instanceof J.Block) {
                        thenPart = visitBlock((J.Block) thenPart, ctx);
                    }

                    ifStmt = ifStmt.withThenPart(thenPart);
                }

                // 处理else部分
                if (ifStmt.getElsePart() != null) {
                    J.If.Else elsePart = ifStmt.getElsePart();
                    // 清理else部分的注释
                    elsePart = elsePart.withPrefix(elsePart.getPrefix().withComments(Collections.emptyList()));
                    elsePart = elsePart.withComments(Collections.emptyList());

                    // 处理else部分的body
                    if (elsePart.getBody() != null) {
                        Statement body = elsePart.getBody();
                        body = body.withPrefix(body.getPrefix().withComments(Collections.emptyList()));
                        body = body.withComments(Collections.emptyList());

                        // 如果else的body是块，使用visitBlock处理确保行内注释被清理
                        if (body instanceof J.Block) {
                            body = visitBlock((J.Block) body, ctx);
                        }

                        elsePart = elsePart.withBody(body);
                    }

                    ifStmt = ifStmt.withElsePart(elsePart);
                }

                // 特别处理行内注释 - 检查是否有行尾的空格和注释模式
            // 使用标准API处理后缀，避免反射

                return ifStmt;
            }

            // 处理For语句
            @Override
            public J.ForLoop visitForLoop(J.ForLoop forLoop, ExecutionContext ctx) {
                // 清理For语句的前缀注释
                forLoop = forLoop.withPrefix(forLoop.getPrefix().withComments(Collections.emptyList()));

                // 清理For语句的所有注释
                forLoop = forLoop.withComments(Collections.emptyList());

                // 继续处理For语句的子部分
                forLoop = super.visitForLoop(forLoop, ctx);

                // 使用标准API处理后缀，避免反射
                return forLoop;
            }

            // 处理While语句
            @Override
            public J.WhileLoop visitWhileLoop(J.WhileLoop whileLoop, ExecutionContext ctx) {
                // 清理While语句的前缀注释
                whileLoop = whileLoop.withPrefix(whileLoop.getPrefix().withComments(Collections.emptyList()));

                // 清理While语句的所有注释
                whileLoop = whileLoop.withComments(Collections.emptyList());

                // 继续处理While语句的子部分
                return super.visitWhileLoop(whileLoop, ctx);
            }

            // 处理DoWhile语句
            @Override
            public J.DoWhileLoop visitDoWhileLoop(J.DoWhileLoop doWhileLoop, ExecutionContext ctx) {
                // 清理DoWhile语句的前缀注释
                doWhileLoop = doWhileLoop.withPrefix(doWhileLoop.getPrefix().withComments(Collections.emptyList()));

                // 清理DoWhile语句的所有注释
                doWhileLoop = doWhileLoop.withComments(Collections.emptyList());

                // 继续处理DoWhile语句的子部分
                return super.visitDoWhileLoop(doWhileLoop, ctx);
            }

            // 处理Lambda表达式
            @Override
            public J.Lambda visitLambda(J.Lambda lambda, ExecutionContext ctx) {
                // 清理Lambda表达式的前缀注释
                lambda = lambda.withPrefix(lambda.getPrefix().withComments(Collections.emptyList()));

                // 清理Lambda表达式的所有注释
                lambda = lambda.withComments(Collections.emptyList());

                // 处理Lambda表达式的参数
                if (lambda.getParameters() != null && !lambda.getParameters().getParameters().isEmpty()) {
                    List<J> parameters = new ArrayList<>();
                    for (J parameter : lambda.getParameters().getParameters()) {
                        if (parameter != null) {
                            // 清理参数的注释
                            if (parameter instanceof J.VariableDeclarations) {
                                J.VariableDeclarations varDecls = (J.VariableDeclarations) parameter;
                                varDecls = varDecls.withPrefix(varDecls.getPrefix().withComments(Collections.emptyList()));
                                varDecls = varDecls.withComments(Collections.emptyList());
                                parameters.add(varDecls);
                            } else {
                                parameter = parameter.withPrefix(parameter.getPrefix().withComments(Collections.emptyList()));
                                parameter = parameter.withComments(Collections.emptyList());
                                parameters.add(parameter);
                            }
                        } else {
                            parameters.add(parameter);
                        }
                    }
                    lambda = lambda.withParameters(lambda.getParameters().withParameters(parameters));
                }

                // 处理Lambda表达式的主体
                if (lambda.getBody() != null) {
                    // 如果Lambda主体是一个块，使用visitBlock处理确保注释被清理
                    if (lambda.getBody() instanceof J.Block) {
                        J.Block body = (J.Block) visitBlock((J.Block) lambda.getBody(), ctx);
                        lambda = lambda.withBody(body);
                    }
                    // 如果Lambda主体是一个表达式，递归处理表达式
                    else {
                        lambda = lambda.withBody((Expression) visitExpression((Expression) lambda.getBody(), ctx));
                    }
                }

                // 继续处理Lambda表达式的子部分
                return super.visitLambda(lambda, ctx);
            }

            // 处理Try-Catch语句
            @Override
            public J.Try visitTry(J.Try tryStmt, ExecutionContext ctx) {
                // 清理try语句的前缀注释
                tryStmt = tryStmt.withPrefix(tryStmt.getPrefix().withComments(Collections.emptyList()));

                // 清理try语句的所有注释
                tryStmt = tryStmt.withComments(Collections.emptyList());

                // 处理try块
                if (tryStmt.getBody() != null) {
                    J.Block body = tryStmt.getBody();
                    body = (J.Block) visitBlock(body, ctx); // 使用visitBlock处理确保注释被清理
                    tryStmt = tryStmt.withBody(body);
                }

                // 处理catch块
                if (tryStmt.getCatches() != null) {
                    List<J.Try.Catch> catches = new ArrayList<>();
                    for (J.Try.Catch catchClause : tryStmt.getCatches()) {
                        // 清理catch子句的前缀注释
                        catchClause = catchClause.withPrefix(catchClause.getPrefix().withComments(Collections.emptyList()));

                        // 清理catch子句的所有注释
                        catchClause = catchClause.withComments(Collections.emptyList());

                        // 处理catch参数
                        if (catchClause.getParameter() != null) {
                            J.ControlParentheses<J.VariableDeclarations> param = catchClause.getParameter();
                            J.VariableDeclarations paramTree = param.getTree();
                            paramTree = (J.VariableDeclarations) visitVariableDeclarations(paramTree, ctx); // 使用visitVariableDeclarations处理参数
                            param = param.withTree(paramTree);
                            catchClause = catchClause.withParameter(param);
                        }

                        // 处理catch块体
                        if (catchClause.getBody() != null) {
                            J.Block catchBody = catchClause.getBody();
                            catchBody = (J.Block) visitBlock(catchBody, ctx); // 使用visitBlock处理确保注释被清理
                            catchClause = catchClause.withBody(catchBody);
                        }

                        catches.add(catchClause);
                    }
                    tryStmt = tryStmt.withCatches(catches);
                }

                // 处理finally块
                if (tryStmt.getFinally() != null) {
                    J.Block finallyBlock = tryStmt.getFinally();
                    finallyBlock = (J.Block) visitBlock(finallyBlock, ctx); // 使用visitBlock处理确保注释被清理
                    tryStmt = tryStmt.withFinally(finallyBlock);
                }

                // 特别处理行内注释 - 检查是否有行尾的空格和注释模式
                // 使用标准API处理后缀，避免反射
                tryStmt = tryStmt.withPrefix(visitSpace(tryStmt.getPrefix(), Space.Location.TRY_PREFIX, ctx));

                return tryStmt;
            }

            // 处理Try-Catch语句中的注释已通过前面的visitBlock方法实现

            // 处理表达式，确保表达式之间保留适当的换行符
            @Override
            public Expression visitExpression(Expression expression, ExecutionContext ctx) {
                return (Expression)super.visitExpression(expression, ctx);
            }

            // 处理赋值表达式，专门清理行内注释
            @Override
            public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext ctx) {
                // 调用父类方法处理子节点
                assignment = super.visitAssignment(assignment, ctx);

                // 清理赋值语句的前缀注释
                assignment = assignment.withPrefix(assignment.getPrefix().withComments(Collections.emptyList()));

                // 清理赋值语句的所有注释
                assignment = assignment.withComments(Collections.emptyList());

                // 处理左侧表达式的注释
                if (assignment.getVariable() instanceof J.Identifier) {
                    J.Identifier left = (J.Identifier) assignment.getVariable();
                    left = left.withPrefix(left.getPrefix().withComments(Collections.emptyList()));
                    left = left.withComments(Collections.emptyList());
                    assignment = assignment.withVariable(left);
                }

                // 处理右侧表达式的注释
                if (assignment.getAssignment() != null) {
                    Expression right = assignment.getAssignment();
                    right = right.withPrefix(right.getPrefix().withComments(Collections.emptyList()));
                    right = right.withComments(Collections.emptyList());
                    assignment = assignment.withAssignment(right);
                }

                // 特别处理行内注释 - 检查是否有行尾的空格和注释模式
                // 使用标准API处理后缀，避免反射
                assignment = assignment.withPrefix(visitSpace(assignment.getPrefix(), Space.Location.ASSIGNMENT_PREFIX, ctx));

                return assignment;
            }
        };
    }



}
