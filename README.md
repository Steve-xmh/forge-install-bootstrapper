# Forge Install Bootstrapper - SteveXMH's Fork

一个基于 [bangbang93/forge-install-bootstrapper](https://github.com/bangbang93/forge-install-bootstrapper) 的改版，目的是支持全部版本安装器的自动化安装（自 1.5.2 以来的任何提供安装器的版本）

> Forge 的安装器在发布时删除了 `--installClient` 的命令行选项，所以我写了这个小程序实现 Forge 从命令行自动安装

# 使用

```
java -cp "forge-install-bootstrapper.jar:forge-xxx-installer.jar" com.bangbang93.ForgeInstaller "PathToDotMinecraft"
```

-cp参数同时指定本程序和forge的安装程序，最后一个参数是安装位置，请确保目录中有有效的`launcher_profile.json`文件

# 下载

<https://github.com/Steve-xmh/forge-install-bootstrapper/releases>

# 输出
程序输出内容和 Forge 安装器原生输出内容一致，未作任何更改。

最后会输出 `true` 和 `false` 以确认是否安装成功。
