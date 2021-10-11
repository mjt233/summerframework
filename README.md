# Summer框架
一个Spring框架赝品

## 主要特性
- IoC容器
- HTTP服务器
- HTTP Cookie
- HTTP Session（可编写Session提供者拓展实现Redis作为Session后端）
- 基于注解的Web控制器编写与路由映射
- 自定义控制器方法返回值转HTTP消息
- 支持前后置处理的请求拦截器链

## 用法
### HTTP服务器
1. 简单用法，创建一个空的HTTP服务器并启动，此时这个HTTP服务器仅仅只是个Java运行目录作为根目录的静态文件服务器
    ```
    HttpServerBuilder
        .getInstance()
        .build()
        .start();
    ```
2. 定义路由映射，通过BindingMapping类可以添加自定义的路由映射规则，拦截器和设置默认的HttpHandler（默认为文件资源Handler，你可以通过设置为一个自定义的HttpHandler实现找不到路由时的默认动作）
    ```
    BindingMapping mapping = new BindingMapping();
    
    HttpServerBuilder
        .getInstance()
        .setBindingMapping(mapping)
        .build()
        .start();
    ```
3. 通过HttpServerBuilder定义Http服务器参数
   ```java
   class Test {
       public static void main(String[] args){
           HttpServerBuilder.getInstance()
                    .setIp("127.0.0.1") // 设置监听IP为127.0.0.1，仅主机内部访问，外部访问使用0.0.0.0或多网卡情况下指定网卡IP
                    .setPort(12345) // 设置服务端口号
                    .setSessionProvider(new NativeHttpSessionProvider()) // 设置Session提供者
                    .addConverter(new EnumHttpMessageConverter()) // 添加控制器方法返回值的HTTP消息转换器 
                    .setBindingMapping(mapping) // 设置请求路由映射器
                    .build() // 构建
                    .start(); // 运行
       }
   }
   ```
