## 起因
之前一直用的flomo,免费版的图片只有500MB空间,再加上数据在别人那里总归有点不爽,然后又看到了Memos,支持自部署,还需要个服务器.

我这最近不是才买了群晖ds920嘛,安排上.装完了才发现电信不给80/443端口,有了公网ip也无用武之地,难受.咋办?折腾内网穿透,tailscale,wireguard之类的?不想,毕竟还是希望能部署在公网,公网能直接访问.

然后又想到如果前后端分离,服务端在nas上,前端在各大支持静态网页的cdn上不就ok?cdn都支持绑定域名,想法很好,现实打脸.Memos我看了下,确实是前后分离,但是看[这里](https://github.com/usememos/memos/blob/main/server/embed_frontend.go#L24),作者把前端编译出来的dist目录直接嵌入到了生成的go的可执行文件里.

这就导致了我想前端部署在cdn,后端部署在nas上实现不了,除非自己改memos的代码,太折腾.

转念一想,就这么点microBlog的功能能有多麻烦?自己撸一个得了?(~~其实是给闲的~~).

## 项目介绍
mBlog,全称micro blog.基于java+mysql.支持自部署的前后端分离的微博,可单用户使用,也可以支持多用户.

[DEMO](https://mblog.coo.st)


![](https://images.kingwrcy.cn/blog/20230511175950.png)

![](https://images.kingwrcy.cn/blog/20230511180318.png)

![](https://images.kingwrcy.cn/blog/20230511180602.png)

- [前端代码-github](https://github.com/kingwrcy/mblog-front)
- [前端代码-gitee](https://gitee.com/kingwrcy/mblog-front)
- [前端Docker镜像](https://hub.docker.com/r/kingwrcy/mblog-front)

-----------------------------------

- [后端代码-github](https://github.com/kingwrcy/mblog-backend)
- [后端代码-gitee](https://gitee.com/kingwrcy/mblog-backend)
- [后端Docker镜像](https://hub.docker.com/r/kingwrcy/mblog-backend)

大体功能如下:
- 基本的文字/图片输入,支持标签
- 支持对历史博文/标签修改
- 支持单/多用户,支持开启/关闭注册
- 支持博文 登录用户/所有人/只有自己 可见
- 支持按照标签,作者等条件搜索
- 支持标签管理(**一键改标签**,删除无用标签等)
- 支持上传图片(目前支持本地文件存储和七牛云,后续继续添加阿里云/腾讯云等)
- 支持开发者使用api token
- 响应式设计,支持pc和手机,支持夜间模式
- 支持RSS
- 支持markdown语法
- 支持emoji表情
- 前后端分离,前端和后端域名可以不一致,可以后端部署在nas上,前端部署在cdn.

## 待开发的功能
- 输入博文时自动带入当时天气信息
- 支持更多的oss提供商
- ......

## 使用到的框架
#### 服务端
- [Springboot](https://spring.io/)
- [Mybatis-Flex](https://mybatis-flex.com/)
- [SaToken](https://sa-token.cc/index.html)

#### 前端
- [Vue3](https://cn.vuejs.org/)
- [Vite](https://cn.vitejs.dev/)
- [Unocss](https://github.com/unocss/unocss)
- [NaiveUI](https://www.naiveui.com/zh-CN/light)

## 部署

#### 服务端

***数据库记得提前建好,对应的账号得有create table的权限***

***数据库记得提前建好,对应的账号得有create table的权限***

初始用户名密码是***admin/a123456***
登录成功后可以在 用户设置 页面修改密码.


##### 源码安装
1. `git clone git@github.com:kingwrcy/mblog-backend.git`
2. 更改`application.properties`文件中的数据库相关信息和前端域名(配置跨域)
3. 在项目根目录下执行`mvn clean package`，需要java>=17和maven>=3.9.1
4. 打包出来的Jar文件在服务器执行`java -jar xxx.jar`

##### Docker安装
```
docker run --volume=${PWD}/upload:/opt/mblog/upload \
--publish=38321:38321 \
--restart=always \
--name=mblog-backend \
--detach=true \
--env MYSQL_USER=数据库用户名 \
--env MYSQL_PASS=数据库密码 \
--env MYSQL_URL=数据库地址:端口 \
--env MYSQL_DB=数据库名称 \
--env MBLOG_FRONT_DOMAIN=mblog前端地址(配置跨域使用的) \
kingwrcy/mblog-backend:latest
```

- 其中`--volume=${PWD}/upload:/opt/mblog/upload`是图片在本地存储才需要挂载的,如果是七牛云之类的,不需要挂载.
- 数据库相关的记得更改
- 映射的端口自己需要就改
- 其中`MBLOG_FRONT_DOMAIN`如果**前后端域名+端口全部一致,可以不用配置,如果不一致,哪怕端口不一致,也需要配置**,如:`https://mblog-front.com`

#### 前端安装
##### 源码安装
1. `git clone git@github.com:kingwrcy/mblog-front.git`
2. 更改`.env.docker`文件中的`VITE_BASE_URL`内容为你的服务端地址,有端口的端口也要加上,如:`https://mblog-server.com:2023`
3. `yarn i && yarn build-only --mode=docker` 需要Node版本>=v18
4. 打包出来的dist目录就可以传到cdn了

##### Docker安装
```
docker run \
--publish=80:80 \
--restart=always \
--name=mblog-front\
--detach=true \
--env MBLOG_SERVER_URL=mblog服务端地址,有端口就带上端口 \
kingwrcy/mblog-front:latest
```