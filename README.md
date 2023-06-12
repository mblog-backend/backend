## 起因
之前一直用的flomo,免费版的图片只有500MB空间,再加上数据在别人那里总归有点不爽,然后又看到了Memos,支持自部署,还需要个服务器.

我这最近不是才买了群晖ds920嘛,安排上.装完了才发现电信不给80/443端口,有了公网ip也无用武之地,难受.咋办?折腾内网穿透,tailscale,wireguard之类的?不想,毕竟还是希望能部署在公网,公网能直接访问.

然后又想到如果前后端分离,服务端在nas上,前端在各大支持静态网页的cdn上不就ok?cdn都支持绑定域名,想法很好,现实打脸.Memos我看了下,确实是前后分离,但是看[这里](https://github.com/usememos/memos/blob/main/server/embed_frontend.go#L24),作者把前端编译出来的dist目录直接嵌入到了生成的go的可执行文件里.

这就导致了我想前端部署在cdn,后端部署在nas上实现不了,除非自己改memos的代码,太折腾.

转念一想,就这么点microBlog的功能能有多麻烦?自己撸一个得了?(~~其实是给闲的~~).

[更新记录](https://github.com/kingwrcy/mblog-backend/blob/main/release.md)

## 项目介绍
mBlog,全称micro blog.基于java+mysql.支持自部署的前后端分离的微博,可单用户使用,也可以支持多用户.


DEMO:

[mblog.coo.st](https://mblog.coo.st)

[mblog.cooolr.online(开放注册)](https://mblog.cooolr.online/) 基于java+mysql,前后分离

[m.kingwrcy.cn(开放注册,匿名评论等)](https://m.kingwrcy.cn) 基于java+sqlite,前后不分离,内存128MB


## 嵌入其他博客系统

比如wordpress,hugo,[教程看这里](https://github.com/mblog-backend/backend/tree/main/doc/static.md)

## tg机器人

感谢[@cooolr](https://github.com/cooolr),开源地址在[这里](https://github.com/mblog-backend/mblog-telegram-bot)

## 浏览器插件

[浏览器插件](https://github.com/mblog-backend/browser-plugin)

目前只支持Chrome浏览器

支持一键转发文本

#### 特性

1. 支持图文混发，支持多图
2. 支持telegram格式链接

#### [tg频道](https://t.me/mblog_backend)

#### [Nas安装教程](https://post.smzdm.com/p/agqerg43/?zdm_ss=Android_3635293780_&send_by=3635293780&from=singlemessage&invite_code=zdm4tzvf9ainv)

#### 微信交流群

![](https://images.kingwrcy.cn/blog/4f324db9e241d29d38262c27c8bd34b.jpg)

<details><summary>图片预览</summary>

![](https://images.kingwrcy.cn/blog/20230511175950.png)

![](https://images.kingwrcy.cn/blog/20230511180318.png)

![](https://images.kingwrcy.cn/blog/20230511180602.png)

</details>


<details><summary>Docker镜像</summary>

- [前后不分离镜像](https://hub.docker.com/r/kingwrcy/mblog)

- [前端代码](https://github.com/kingwrcy/mblog-front)

- [前端Docker镜像](https://hub.docker.com/r/kingwrcy/mblog-front)

-----------------------------------

- [后端代码](https://github.com/kingwrcy/mblog-backend)

- [后端Docker镜像](https://hub.docker.com/r/kingwrcy/mblog-backend)
</details>

大体功能如下:
- 基本的文字/图片/文件等输入,支持标签
- 支持对历史博文/标签修改
- 支持单/多用户,支持开启/关闭注册/评论/点赞
- 支持博文 登录用户/所有人/只有自己 可见
- 支持按照标签,时间,内容,可见性进行搜索
- 支持标签管理(**一键改标签**,删除无用标签等)
- 支持上传图片(目前支持本地文件存储和七牛云,S3)
- 支持开发者使用api token
- 响应式设计,支持pc和手机,支持夜间模式
- 支持RSS
- 支持markdown语法
- 支持emoji表情
- 前后端分离,前端和后端域名可以不一致,可以后端部署在nas上,前端部署在cdn.
- 同时也支持前后不分离,支持使用MySQL或者Sqlite

## 待开发的功能
- ~~支持sqlite~~ v1.0.8已支持
- ~~争取提供前后不分离版本~~ v1.0.8已支持
- 提供chrome插件,一键转发到mblog
- ~~提供可自部署的tg机器人,一键转发到mblog~~ 已经由[@cooolr](https://github.com/cooolr)帮忙实现了,欢迎试用
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

[不会命令行安装的看这里,通过Nas的Docker管理界面安装](https://github.com/kingwrcy/mblog-backend/blob/main/doc/gui_install.md)

#### 服务端

***因为一些原因,mysql需要5.7以上***

***数据库记得提前建好,对应的账号得有create table的权限***

***数据库记得提前建好,对应的账号得有create table的权限***

#### 需要注意的

- 初始用户名密码是***admin/a123456***

- 登录成功后可以在 用户设置 页面修改密码.

- 在设置 - 系统设置中,需要设置服务端域名,安全域名(也就是前端域名,逗号分割,只有配置在这里的,才会允许跨域调用.没配置的,不支持跨域.前端会报错)

- 其中服务端域名是在生成RSS内容,和本地上传文件时启用.

- 写内容时,只有**第一行的 以 `#` 开头**的才会被解析为 标签,第二行以及后面的,不解析.

- AllInOne 安装可以参看 `根目录下的docker-compose.yml`,里面包含了前端,后端和MySQL 8.

#### MYSQL 5.7注意事项

因为开发是在8.0.33的版本上开发的,没想到5.7里mysql要求timestamp类型必须有值,所以直接在5.7上运行,会报错.

解决办法:

1. Docker启动mysql的,看[这里](https://github.com/kingwrcy/mblog-backend/blob/main/docker-compose.yml),在command中加上 ` '--explicit_defaults_for_timestamp=ON' `
2. 非Docker启动的,找到MYSQL 5.7的配置文件,在[mysqld]下方加入`explicit_defaults_for_timestamp = 1`
3. 删掉已存在的数据里的所有的表和数据,再次启动后端,会自己重建的,**记得数据库不能删除,是所有的表和数据删除**.

##### 宝塔面板安装

感谢 @lzhang ,[教程在此](https://laozhang.org/archives/3387.html) ,图文并茂,大家有需要的参考下.

##### 源码安装
1. `git clone git@github.com:kingwrcy/mblog-backend.git`
2. 更改`application.properties`文件中的数据库相关信息和前端域名(配置跨域)
3. 在项目根目录下执行`mvn clean package`，需要java>=17和maven>=3.9.1
4. 打包出来的Jar文件在服务器执行`java -jar xxx.jar`

##### Docker安装
```
docker run --volume=${PWD}/mblog:/opt/mblog \
--publish=你要映射的后端端口,必填:38321 \
--restart=always \
--name=mblog-backend \
--detach=true \
--env MYSQL_USER=数据库用户名,必填 \
--env MYSQL_PASS=数据库密码,必填 \
--env MYSQL_URL=数据库地址:端口,必填,前面没有http(s) \
--env MYSQL_DB=数据库名称,必填) \
--env MBLOG_FRONT_DOMAIN=mblog前端地址(配置跨域使用的,带http(s),有端口带端口,docker启动的这里是宿主机的IP,必填) \
kingwrcy/mblog-backend:latest
```

前后不分离,并且使用sqlite的版本:

```
docker run --volume=${PWD}/mblog:/opt/mblog \
--publish=你要映射的后端端口,必填:38321 \
--restart=always \
--name=mblog-backend \
--detach=true \
--env DB_TYPE=-sqlite \
kingwrcy/mblog:latest
```

- 其中`--volume=${PWD}/upload:/opt/mblog/upload`是图片在本地存储才需要挂载的,如果是七牛云之类的,不需要挂载.
- 其中sqlite版本的数据库位置在镜像的`/opt/mblog/data.sqlite`,需要映射出来,不然重启数据就丢失了
- 数据库相关的记得更改
- 映射的端口自己需要就改
- 其中`MBLOG_FRONT_DOMAIN`如果**前后端域名+端口全部一致,可以不用配置,如果不一致,哪怕端口不一致,也需要配置**,如:`https://mblog-front.com`
- 开启了API文档的,API文档访问地址为`http://服务端IP:服务端端口/api.html`


| 非必填环境变量 | 默认值                 | 解释                                                                    |
|---------|---------------------|-----------------------------------------------------------------------|
| ENABLE_SWAGGER     | false               | 需要开启API文档的才配置,否则不需要配置,选填                                              |
| JAVA_OPTS     | "-Xms512m -Xmx512m" | 内存设置,建议最低不要低于256m,默认512m                                              |
| DB_TYPE     | 无                   | 数据库类型,可选 为空时默认mysql,`-sqlite`:sqlite,注意前面有`-`                         |
| MYSQL_USER     | 无                   | 数据库用户名,DB_TYPE为空时必填                                                   |
| MYSQL_PASS     | 无                   | 数据库密码,DB_TYPE为空时必填                                                    |
| MYSQL_URL     | 无                   | 格式:`数据库地址:端口`,前面没有http(s) ,DB_TYPE为空时必填                               |
| MYSQL_DB     | 无                   | 数据库名称 ,DB_TYPE为空时必填                                                   |
| MBLOG_FRONT_DOMAIN     | 无                   | mblog前端地址(配置跨域使用的,带http(s),有端口带端口,docker启动的这里是宿主机的IP,必填),前后不分离版本忽略此参数 |
| AUTO_EXECUTE_SQL     | true                | 自动执行SQL脚本,默认自动执行,如果已经提前执行了,这里配置为false                                 |

#### 前端安装
##### 源码安装
1. `git clone git@github.com:kingwrcy/mblog-front.git`
2. 更改`.env.docker`文件中的`VITE_BASE_URL`内容为你的服务端地址,有端口的端口也要加上,如:`https://mblog-server.com:2023`
2. 更改`.env.docker`文件中的`VITE_MBLOG_VERSION`内容版本号,自定义,如`v1.0.5`
3. `yarn i && yarn build-only --mode=docker` 需要Node版本>=v18
4. 打包出来的dist目录就可以传到cdn了

### 自己部署前端的注意事项

由于使用了vue-router的 [HTML5 Mode](https://router.vuejs.org/guide/essentials/history-mode.html#html5-mode),所以需要在服务器的nginx上写入一条配置.
详情参见[HTML5 Mode](https://router.vuejs.org/guide/essentials/history-mode.html)

如果不配置的话,直接访问详情页,登录页等页面会404,站内跳转的不受影响.

用Docker安装的不需要这个步骤,我已经在Dockerfile中处理过了.

##### Docker安装
```
docker run \
--publish=你要映射的前端端口,必填:80 \
--restart=always \
--name=mblog-front \
--detach=true \
--env MBLOG_SERVER_URL=mblog服务端地址,带http(s),有端口带端口,docker启动的这里是宿主机的IP,必填 \
kingwrcy/mblog-front:latest
```