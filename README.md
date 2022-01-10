# 应用简介
Android 平台在线漫画阅读器

Online manga reader based on Android

[![Build Status](https://travis-ci.org/onlytheworld/OnlyX.svg?branch=release-tci)](https://travis-ci.com/github/onlytheworld/OnlyX)
[![codebeat badge](https://codebeat.co/badges/a22ca260-494d-4be8-9e3d-fc9c8f7d0f73)](https://codebeat.co/projects/github-com-onlytheworld-onlyx-release-tci)
[![GitHub release](https://img.shields.io/github/release/onlytheworld/OnlyX.svg)](https://github.com/onlytheworld/OnlyX/releases)
[![](https://img.shields.io/github/downloads/onlytheworld/onlyx/total.svg)](https://github.com/onlytheworld/OnlyX/releases)

> 项目迁移自 [project Cimoc](https://github.com/feilongfl/Cimoc)

## 进度
- 修复了漫源更新问题

- 增加了每个漫源单独搜索功能

- 增加了JM登录功能
  
- 修复 动漫之家、57漫画、漫画柜、JMTT 漫源

- 更换了签名，可能不再与旧版兼容

- 可能解决了报毒问题？（不太确定）

- 去除了gms

- 解决了代码混淆时的报错问题

- 解决了firebase-crashlytics打包时的报错问题

- 删除了不必要的第三方依赖库和test

- 更新了gradle版本

- 更改了漫画搜索和漫画更新的运行为多线程
  
- 删除了存储权限请求，更改了默认存储地址（注意：备份功能没有存储权限无法正常运行）

## TODO
- 重新整理UI，删减无用内容

- 独立图源，位于APP之外

- 重新实现 travis ci 发布 apk

- 夜间模式更改实现方式为主题切换

- 更改偏好设置

- 更改BindView的方式

- 更改本地漫画读取方式，使得可以阅读任意本地漫画

- 增加漫画标签功能，使得可以读取漫画标签，支持自定义标签

- 增加按照的分类、排序搜索的功能

- 增加漫画推荐功能

- 增加不同漫源的相同漫画合并功能

- 增加漫画更新重排序和标志功能

- 增加评论阅读功能