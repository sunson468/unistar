# Unistar微服务开发SDK

### SDK介绍

Unistar SDK是提供给SpringCloud项目，使其能够快速集成到Unistar中心，SDK实现SpringCloud提供的一些标准规范实现，所以可以无缝地从现有的SpringCloud体系中切换过来。

### SDK功能

SDK包含了内置核心、SpringCloud能力以及扩展功能。

- 内置核心包含了远程连接、心跳同步、通讯等
- SpringCloud能力实现了配置中心、服务注册和发现、限流
- 扩展功能实现了实时服务链路跟踪、任务调度、日志打印控制

### Maven引用

```xml
<dependency>
    <groupId>com.up1234567</groupId>
    <artifactId>unistar-springcloud</artifactId>
    <version>1.0.RC</version>
</dependency>
```

### SpringCloud能力说明

SpringCloud能力保留原生，以下是一个简单的启动类

```java
// 启动服务发现，但不注册为服务
@EnableDiscoveryClient(autoRegister = false)
// 启用OpenFeign
@EnableFeignClients("com.xxx.xxx")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Qualifier("restTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new UnistarRestTemplate();
        restTemplate.getMessageConverters().forEach(m -> {
            if (m instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) m).setDefaultCharset(StandardCharsets.UTF_8);
            }
        });
        return restTemplate;
    }
}
```

#### 配置中心

配置功能的启用不需要任何注解。

节点启动时会自动读取配置信息，读取配置会依赖两个参数，应用名称以及Profiles(ActiveProfiles|DefaultProfiles)。

不同于现有的文件化配置，Unistar采用Map结构化数据实时传递，也不会在客户端本地生成缓存。

#### 服务注册和发现

服务注册和发现的机制是完全继承于SpringCloud原生规则。

#### 限流

Unistar的限流是基于拦截器的，一般情况下在客户端无需做任何配置，只需要到Unistar控制台上配置相应的限流规则就好了。

如果你的项目中有直接采用RestTemplate的，则需要采用UnistarRestTemplate(参考上面的启动类)，否则系统检测不到这类请求，也无法进行限流控制。


### 扩展功能

#### 实时服务链路跟踪

不同于日志方案的链路追踪，Unistar的实时链路是在线上运行的时候，根据选中的路径，随机截取一个请求，然后准实时的反馈调用链路，时效等，这个功能可用于实时分析线上功能。

#### 任务调度

任何节点都可以作为任务调度器，要激活任务调度器除基本配置外，需要继承Unistar的任务处理接口。

```java
// 必须注册为Bean，因为任务调度器是从SpringContext内获取任务执行类的
@Component
public class AppNodeTask implements IUnistarTasker {
    @Override
    public String task() {
        // 为避免任务的滥用，需管理员在后台配置该名称后才会起作用
        return "任务标记名称";
    }
    @Override
    public Map<String, Object> handle(Map<String, Object> params) {
        // 返回数据会在控制台上展示
        // 如有后继任务，返回数据将会作为入参传递给后继任务
        return null;
    }
}
```

#### 日志打印控制

生产环境中的日志通常都是ERROR级别，而我们的埋点日志通常都是INFO、DEBUG级别，当我们需要调查某个问题的时候就需要修改级别来打印日志，但是一般来说要么就是改配种重启，如果采用的日志打印组件是可以动态调整级别的，那就需要额外实现一个接口。

为了解决这个问题，Unistar提供了一个在线修改日志打印级别的功能。

为了适配多种日志打印器，Unistar并没有借助日志打印组件，而是在日志打印组件上包装了一个自己的日志类工厂以及日志级别控制器。

这样做的目的就是引导开发者意识到日志打印是需要设计的，促使大家对日志进行必要的分类。

当然也考虑到了易用性，所以日志通过Lombok的@CustomLog，并且打印接口也完全实现了常用的日志打印方法。

使用@CustomLog就需要在工程目录下添加一个Lombok配置文件[lombok.config](../doc/lombok/lombok.config)，直接复制到项目的根目录里即可。


### 配置一览

以下是集成SDK客户端的所有配置

```yaml
spring:
  cloud:
    unistar:
      server: 127.0.0.1:36524 # Unistar中心地址，如果多个中心，请前置一个反向代理，比如Nginx
      token: # 节点接入授权码，非必须
      namespace: DEFAULT_NS # 节点所属空间，不同空间之间不互通，非必须
      group: DEFAULT_GROUP # 节点所属分组，任务调度可按组调度，非必须
      name: # 节点应用名称，唯一标识，一般不需要填写，自动获取${spring.application.name}，请勿上线后修改
      host: # 节点地址，一般不需要填写，自动获取本机IPv4地址，非必须
      port: # 节点端口，一般不需要填写，自动获取${server.port}
      registry: # 非必须
        enabled: true # 是否注册为服务，其作用也受@EnableDiscoveryClient(autoRegister = false)影响
        available: true # 是否立刻对外服务，false则不出现在服务发现的可用列表中
        weight: 1 # 服务发现按照权重比进行随机分配
      discover:  # 非必须
        feign: 
          connectTimeout: 3_000 # 微服务调用连接超时毫秒
          readTimeout: 15_000 # 微服务调用读取超时毫秒
          writeTimeout: 15_000 # 微服务调用发送超时毫秒
      task: # 非必须
        available: true # 任务执行器是否可用
        parallel: 2 # 客户端接受调度的线程池大小
```
  
