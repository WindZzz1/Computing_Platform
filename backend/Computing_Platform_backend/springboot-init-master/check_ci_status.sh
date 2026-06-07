#!/bin/bash

# CI/CD 状态检查脚本
# 用于本地验证 CI/CD 配置是否正确

echo "🔍 开始检查 CI/CD 配置..."

# 检查必要文件
check_file() {
  if [ -f "$1" ]; then
    echo "✅ $1 存在"
  else
    echo "❌ $1 缺失"
  fi
}

echo "📁 检查 CI/CD 配置文件..."
check_file ".github/workflows/ci.yml"
check_file ".github/workflows/codeql-analysis.yml"
check_file ".github/workflows/project-status.yml"
check_file "checkstyle.xml"
check_file ".sonarqube.properties"

# 检查文档
echo "📝 检查项目文档..."
check_file "README.md"
check_file "CONTRIBUTING.md"
check_file "SECURITY.md"
check_file "CODE_OF_CONDUCT.md"

# 检查模板文件
echo "📋 检查模板文件..."
check_file ".github/PULL_REQUEST_TEMPLATE.md"
check_file ".github/ISSUE_TEMPLATE/bug_report.md"
check_file ".github/ISSUE_TEMPLATE/feature_request.md"

# 检查 Maven 配置
echo "🏗️  检查构建配置..."
if grep -q "jacoco-maven-plugin" pom.xml; then
    echo "✅ JaCoCo 插件已配置"
else
    echo "❌ JaCoCo 插件未配置"
fi

if grep -q "maven-checkstyle-plugin" pom.xml; then
    echo "✅ Checkstyle 插件已配置"
else
    echo "❌ Checkstyle 插件未配置"
fi

# 检查环境变量示例文件
echo "🔧 检查环境配置..."
check_file ".env.example"

echo ""
echo "🎯 运行测试构建..."
mvn clean compile -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ 项目构建成功"
else
    echo "❌ 项目构建失败"
fi

echo ""
echo "🚀 CI/CD 配置检查完成！"
echo "请根据上述结果完善缺失的配置。"
