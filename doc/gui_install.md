由于有些非IT行业的用户,他们不会使用命令行基于Docker的安装.

好在现在的一些Nas品牌都提供了基于GUI界面的Docker管理面板.

这里列举一下各种Nas品牌,如何使用界面配置MBlog.

### MySQL的安装

[绿联云DH2600/DX4600使用Docker安装MySQL数据库教程](https://www.bilibili.com/video/BV1qG411F724)

[利用MySQL workbench 创建一个数据库](https://bbs.huaweicloud.com/blogs/350012)

### MBlog的安装

### 注意事项

**其中服务端的环境变量里的 MBLOG_FRONT_DOMAIN**表示的是前端的地址

格式是`http://你的nas的ip:你的前端映射的端口`,比如下图里的`http://192.168.1.2:11301`

格式必须完全一样,**结尾没有 /**

**其中前端的环境变量里的 MBLOG_SERVER_URL**表示的是前端的地址

格式是`http://你的nas的ip:你的服务端映射的端口`,比如下图里的`http://192.168.1.2:38321`

格式必须完全一样,**结尾没有 /**

### 绿联云

#### mblog-backend 服务端安装

##### 端口映射

![image](https://images.kingwrcy.cn/blog/9fcc8f3d2667974dac6622d4c4b8a51.png)

##### 环境变量

![](https://images.kingwrcy.cn/blog/96ac3b191f96cb127efc619caaf79e8.png)

#### mblog-backend 前端安装

##### 端口映射

![](https://images.kingwrcy.cn/blog/0b41564687ba0dc6996972e6da3a02f.png)

##### 环境变量

![](https://images.kingwrcy.cn/blog/53754062284884b43f974300c21706b.png)


