# 概要

Windows Subsystem for Linux (WSL) の [関数](https://msdn.microsoft.com/en-us/library/windows/desktop/mt826875.aspx) を使って、Windows 上で WSL 上の [Git](https://git-scm.com/) を叩くものです。

# 試してみる

## 注意

* WSL の Linux 環境がインストールされており、そこに Git がインストールされていることが前提です
* `git` をフルパス指定したい場合は [wslgit/wslgit.cpp](wslgit/wslgit.cpp) の `GIT_CMD` の定義をそれなりのものに変えてリビルドしてください。

## ビルド

Visual Studio 2017 以上で `wslgit.sln` を開いてビルドしてください

## 使用

PowerShell なりコマンドプロンプトなりで、 `wslgit.exe` に続いて普通に Git コマンドを実行してください。

