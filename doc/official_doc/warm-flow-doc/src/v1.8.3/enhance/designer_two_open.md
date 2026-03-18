# 设计器二开

::: tip
- 当内置的设计器和接口不满足业务需求时，可进行二开设计器

:::

## 1 下载设计器源码，改造 

如果内置的接口不满足或者不够支持实际业务开发，可在业务系统中增加接口，设计器配置该接口地址


- warm-flow
    - warm-flow-plugin:                         插件模块
        - warm-flow-plugin-ui:                     设计器插件模块
            - warm-flow-plugin-ui-core:              设计器后端核心模块
            - warm-flow-plugin-ui-sb-web:            设计器后端springboot web接口和starter启动模块
            - warm-flow-plugin-ui-solon-web:         设计器后端solon web接口和plugin启动模块
            - warm-flow-plugin-vue3-ui:              设计器前端编译后jar包引入模块
    - warm-flow-ui:                              设计器前端模块, 基于logic-flow开发

<div><img src="https://foruda.gitee.com/images/1754016507623081125/62e2f1b9_2218307.png" width="400"></div>
<div><img src="https://foruda.gitee.com/images/1730958025453602251/ae415296_2218307.png"></div>

## 2 源码调试
- 设计器需要配置业务系统的代理地址，否则无法访问业务系统
- 独立启动该设计器
- 业务系统配置设计器的地址

<div><img src="https://foruda.gitee.com/images/1754016548953059153/82d1314c_2218307.png"></div>
<div><img src="https://foruda.gitee.com/images/1730825131504921296/a17821eb_2218307.png"></div>

## 3 部署
### 3.1 先排除原依赖的前端代码

```xml
<dependency>
    <groupId>org.dromara.warm</groupId>
    <artifactId>warm-flow-plugin-ui-sb-web</artifactId>
    <exclusions>
        <exclusion>
            <artifactId>warm-flow-plugin-vue3-ui</artifactId>
            <groupId>org.dromara.warm</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

### 3.2 设计器不分离部署(部署方案一)
- 打包项目，把打包后的文件`dist`复制到业务系统`src/main/META-INF/resources`,或者`src/main/resources`目录下都可以,改名为warm-flow-ui
- 独立服务

<div><img src="https://foruda.gitee.com/images/1730822519593337466/41e4ce38_2218307.png" width="400"></div>

### 3.3 设计器独立部署/分离部署(部署方案二)
- 打包项目，把打包后的文件`dist`复制到nginx的html目录下,改名为warm-flow-ui
- 访问地址改为(请注意地址少了`/warm-flow-ui`)：http://localhost:81/index.html?id=xxx&disabled=false

**nginx配置示例**

```
server {
       listen       82;
       server_name  localhost;
	   
	   location / {
	       root   D:/software/nginx-1.16.1/html/warm-flow-ui;
	   	   try_files $uri $uri/ /index.html;
	   	   index  index.html index.htm;
	   }
	   
	   location /warm-flow-ui/ {
	       proxy_set_header Host $http_host;
	       proxy_set_header X-Real-IP $remote_addr;
	       proxy_set_header REMOTE-HOST $remote_addr;
	       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	       proxy_pass http://localhost:8080/warm-flow-ui/;
	   }

       error_page   500 502 503 504  /50x.html;
       location = /50x.html {
           root   html;
       }
}
```
