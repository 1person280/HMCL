# HMCL-TP

<div align="center">

**Hello Minecraft! Launcher: Third Party Edition**

一款现代化、跨平台的 Minecraft 启动器

[![GitHub stars](https://img.shields.io/github/stars/1person280/HMCL?style=flat-square)](https://github.com/1person280/HMCL/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/1person280/HMCL?style=flat-square)](https://github.com/1person280/HMCL/network/members)
[![License](https://img.shields.io/github/license/1person280/HMCL?style=flat-square)](https://github.com/1person280/HMCL/blob/master/LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)](https://openjdk.org/projects/jdk/17/)

[下载](https://github.com/1person280/HMCL/releases) | [文档](./share.md) | [问题反馈](https://github.com/1person280/HMCL/issues) | [QQ 群](https://qm.qq.com/q/7VjzX3GhCU)

</div>

## 简介

HMCL-TP (Hello Minecraft! Launcher: Third Party Edition) 是一款现代化、跨平台的 Minecraft 启动器，基于 HMCL 进行二次开发。本项目由 **不怎么玩MC的MC君** 维护，致力于为玩家提供更好的游戏体验。

## 目录结构

```
HMCL-TP/
├── HMCL/                  # 主应用程序模块
│   ├── src/              # 源代码
│   │   └── main/
│   │       ├── java/     # Java 源代码（JavaFX UI、启动器逻辑）
│   │       └── resources/ # 资源文件
│   ├── image/            # 应用程序图片资源
│   └── build.gradle.kts  # Gradle 构建配置
├── HMCLBoot/             # 启动引导模块
│   ├── src/              # 源代码
│   │   └── main/
│   │       ├── java/     # Java 源代码（启动引导）
│   │       └── resources/ # 资源文件（语言包）
│   └── build.gradle.kts  # Gradle 构建配置
├── HMCLCore/             # 核心库模块
│   ├── src/              # 源代码
│   │   ├── main/         # 主代码
│   │   │   ├── java/     # Java 源代码（游戏管理、认证、下载等）
│   │   │   └── resources/ # 资源文件（语言包、游戏配置等）
│   │   └── test/         # 测试代码
│   └── build.gradle.kts  # Gradle 构建配置
├── minecraft/            # Minecraft 相关
│   └── libraries/        # Minecraft 库文件（ModLauncher、MultiMC Bootstrap）
├── buildSrc/             # Gradle 构建脚本
│   └── src/main/java/    # 自定义 Gradle 插件和任务
├── gradle/               # Gradle 包装器
├── docs/                 # 项目文档
├── config/               # 配置文件
├── .github/              # GitHub Actions 工作流
├── lib/                  # 外部依赖库
├── build.gradle.kts      # 根项目构建配置
├── settings.gradle.kts   # Gradle 设置
└── README.md             # 项目说明文档
```

**主要模块说明：**

- **HMCL**: 主应用程序模块，包含 JavaFX 用户界面、启动器核心逻辑、账户管理、主题系统等
- **HMCLBoot**: 启动引导模块，负责初始化和启动主应用程序，包含启动属性配置和桌面工具
- **HMCLCore**: 核心库模块，提供游戏管理、认证（离线/微软/OAuth）、下载、模组管理、崩溃分析等核心功能
- **minecraft/libraries**: Minecraft 相关的库文件，包括 ModLauncher 和 MultiMC Bootstrap
- **buildSrc**: 自定义 Gradle 构建脚本，包含 JavaFX 工具、本地化处理、文档生成等
- **docs**: 项目文档，包含多语言版本的 README、贡献指南、本地化指南等
## 特性

- 跨平台支持
- 多种登录方式（离线、微软、外置登录）
- 模组管理和模组包支持
- 一键安装 Fabric、OptiFine模组加载器
- 自动内存优化
- 智能崩溃分析
- 多语言支持
- 主题定制

## 系统要求

- **操作系统**：64位 Windows、macOS 10.12+、Linux
- **Java 版本**：Java 25 或更高版本
- **内存**：4GB RAM（推荐 6GB+）
- **磁盘空间**：至少 500MB 可用空间

## 快速开始

### 下载安装

访问 [GitHub Releases](https://github.com/1person280/HMCL/releases) 下载最新版本。

### 基本使用

1. 启动 HMCL-TP
2. 添加或选择一个游戏版本
3. 配置 Java 和内存设置
4. 点击"启动游戏"

## 开发

### 环境要求

- JDK 25+
- Gradle 8.x

### 构建项目

```bash
# 克隆仓库
git clone https://github.com/1person280/HMCL.git
cd HMCL

# 构建项目
./gradlew build

# 运行启动器
./gradlew run

# 运行测试
./gradlew test
```

### 开发规范

详细的开发规范请参考 [share.md](./share.md) 文档。

## 常见问题

### 为什么内存分配使用 GB 单位？

HMCL-TP 使用 GB 作为内存单位，可以减少用户的选择困难症。系统会根据可用物理内存自动推荐合适的内存大小。

### 为什么不兼容 Minecraft 1.20.4？

本项目使用 Java 25 进行编译和开发，Minecraft 1.20.4 及以下版本对 Java 25 的支持不完善。

**解决方案**：
- 建议升级到 Minecraft 1.20.5 或更高版本

更多常见问题请查看 [share.md](./share.md)。

## 贡献

我们欢迎所有形式的贡献！请查看 [share.md](./share.md) 了解详细的贡献指南。

## 许可证

本项目基于 [GPL v3](LICENSE) 协议开源。

## 致谢

- [HMCL 原项目](https://github.com/HMCL-dev/HMCL)
- 所有贡献者
- 用户社区

## 联系方式

- **GitHub Issues**：https://github.com/1person280/HMCL/issues
- **QQ 交流群**：https://qm.qq.com/q/7VjzX3GhCU
- **维护者**：不怎么玩MC的MC君

---

<div align="center">

Made with ❤️ by不怎么玩MC的MC君

</div>
