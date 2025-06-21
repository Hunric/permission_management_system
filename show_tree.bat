@echo off
rem show_tree.bat
rem 功能：显示当前目录或指定目录的结构树
rem 用法：show_tree.bat [目录路径]

echo generating...

set TARGET_DIR=%1

rem 如果没有提供目录路径，则使用当前目录
if not defined TARGET_DIR (
    set TARGET_DIR=.
)

rem tree 命令
rem /F: 显示每个文件夹中文件的名称。
rem /A: 使用 ASCII 字符而不是扩展字符。
tree %TARGET_DIR% /F /A

echo done.
pause