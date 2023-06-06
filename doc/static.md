## MBlog 单页应用

mblog支持使用代码的方式嵌入到各种成熟的博客系统.

### 安装之前确保以下几点

1. 博客的域名已经配置到mblog后台的 系统设置 -> 基础设置 -> 安全域名 里面,逗号分隔,如果没有配置,前端会报 跨域错误.
2. 如果需要开启匿名评论, 在 mblog后台的 系统设置 -> 功能设置 里 开放 `开放评论` 和 `开放匿名评论`,`匿名评论是否审核` 随意设置,
但是如果设置了需要审核,在博客页面是无法审核的,因为博客页面并没有登录,所以无法确认当然人是管理员,还需要到mblog去做审核.

### 安装

插入代码
```html
<div id="mblog"></div>
<script>
  const mblogConfig = {
    url: '你的mblog服务端地址,如 https://m.kingwrcy.cn',
    userId: null,//默认查所有人的,如只需要管理员的,这里改成 1
    openComment: false,//是否开放评论,需要上面的·步骤·中第二步正确设置,需要：true,不需要：false
  };
</script>
<script  type="module"  src="https://cdn.kingwrcy.cn/mblog/static/v1/static.js"></script>
```

### 预览

![](https://images.kingwrcy.cn/blog/20230606230739.png)