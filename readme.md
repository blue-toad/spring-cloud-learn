# SpringCloud初级篇

**Author：大聪明小蓝**

## 前言：

**2020年的开发背景**

1. Eureka停用,可以使用zk作为服务注册中心
2. 服务调用,Ribbon准备停更,代替为LoadBalance
3. Feign改为OpenFeign

4. Hystrix停更,改为resilence4j或者阿里巴巴的sentienl

5. Zuul改为gateway

6. 服务配置Config改为  Nacos

7. 服务总线Bus改为Nacos




## 环境搭建:

一、**创建父工程**

几个注意的点

1.父工程中这一行 ==<packaging>pom</packaging>==

2.spring cloud 版本和springBoot版本需要对应

**二、创建子模块**

![](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/sc%E7%9A%843.png)



![](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/image-20201016093759732.png)





**三、重构**

新建一个模块,将重复代码抽取到一个公共模块中

**1、创建commons模块**

主要是实体entity类

**2、抽取公共pom**

（这样有可能会造成混乱 我没有导入别的依赖）

**3、使用maven,将commone模块打包**

mvn install 命令发布包

其他模块引入commons



# 第一章：服务注册与发现Eureka

 ![](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/Eureka%E7%9A%844.png)



**集群构建原理:**

​		互相注册

![](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/Eureka%E7%9A%8413.png)



### 一、构建新erueka项目

名字:cloud_eureka_server_7002

**1、pom文件:**

**2、配置文件:**

首先修改之前的7001的eureka项目,因为多个eureka需要互相注册

```yml
server:
  port: 7001

eureka:
  instance:
    hostname: eureka7001.com  #eureka服务端的实例名称
  client:
    register-with-eureka: false  #false表示不向注册中心注册自己。
    fetch-registry: false     #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url:
      #集群指向其它eureka 这个必须写两个
      #defaultZone: http://eureka7002.com:7002/eureka/
      #单机就是7001自己
      defaultZone: http://localhost:7001/eureka,http://localhost:7002/eureka
    #server:
    #关闭自我保护机制，保证不可用服务被及时踢除
    #enable-self-preservation: false
    #eviction-interval-timer-in-ms: 2000
```

然后修改7002

**3、主启动类:**

```java
@SpringBootApplication
@EnableEurekaServer
public class Eureka7001Application {
    public static void main(String[] args) {
        SpringApplication.run(Eureka7001Application.class, args);
    }

}
```

### 二、将pay,order模块注册到eureka集群中

==开启以服务名称的访问==

1、修改地址为服务

```java
@RestController
@Slf4j
public class OrderController {
    // 修改为服务名
    private static final String URL = "http://PAYMENT-SERVICE";

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/consumer/payment/create")
    public CommonResult<Payment> creatPayment(Payment payment) {
        log.info("发送请求的consumer{}", payment);
        CommonResult commonResult = restTemplate.postForObject(URL + "/payment/create", payment, CommonResult.class);
        log.info("template{}", commonResult);
        return commonResult;
    }

    @RequestMapping("/consumer/payment/get/{id}")
    public CommonResult<Payment> getPaymentByid(@PathVariable("id") Integer id) {
        log.info("发送的id={}", id);
        return restTemplate.getForObject(URL + "/payment/get/"+id , CommonResult.class);
    }

}
```

2、开启平衡 ==Ribbon==

```java
@Configuration
public class ApplicationContextConfig {

    @Bean
//    开启以下注解才会在controller的地址中找到服务的地址
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

### 三、修改服务主机名和ip在eureka的web上显示

修改配置文件:

```yml
spring:
  application:
    name: payment-service  #根据这个区分是哪个服务
eureka:
  instance:
    instance-id: payment1     #根据这个区分是运行这个服务的哪个机器
```

### 四、Eureka自我保护

**概述**

保护模式主要用于一组客户端和Eureka Server之间存在网络分区场景下的保护。一旦进入保护模式，Eureka Server将会尝试保护其服务注册表中的信息，不再删除服务注册表中的数据，也就是不会注销任何微服务。

**为什么会产生Eureka自我保护机制**

为了防止EurekaClient可以正常运行，但是与Eureka网络不通情况下，EurekaServer不会立刻将EurekaClient服务剔除。

**什么是自我保护模式**

默认情况下，如果EurekaServer在一定时间内没有接受到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒）。但是当网络分区故障发生（延时、卡顿、拥挤）时，微服务与EurekaServer之间无法正常通信，以上行为可能变得非常危险--因为微服务本身其实是健康的，此时本不应该注销这个微服务。Eureka通过“自我保护模式”来解决这个问题--当EurekaServer节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。

**自我保护机制**

默认情况下EurekaClient定时向EurekaServer端发送心跳包

如果Eureka在server端在一定时间内（默认90秒）没有收到EurekaClient发送心跳包，便会直接从服务注册列表中剔除该服务，但是在短时间（90秒中）内丢失了大量的服务实例心跳，这时候EurekaServer会开启自我保护机制，不会剔除该服务（该现象可能出现在如果网络不通，但是EurekaClient未出现宕机，此时如果换做别的注册中心如果一定时间内没有收到心跳会将剔除该服务，这样就出现了严重失误，因为客户端还能正常发送心跳，只是网络延迟问题，而保护机制是为了解决此问题而产生的）

### 五、注册中心的异同



![cap](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/cap.jpg)



cap理论

eureka属于ap

zookeeper、consul属于cp



# 第二章：服务调用

### 一、Ribbion简介

**Ribbon目前也进入维护,基本上不准备更新了**

简单的说，Ribbon是Netflix发布的开源项目， 主要功能是提供客户端的软件负载均衡算法，将Netflix的中间层服务连接在一起。Ribbon的客户端组件提供一系列完整的配置项如:连接超时、重试等等。简单的说，就是在配置文件中列出LoadBalancer (简称LB:负载均衡)后面所有的机器，Ribbon会自动的帮助你基于某种规则(如简单轮询，随机连接等等)去连接这些机器。我们也很容易使用Ribbon实现自定义的负载均衡算法!

### 二、使用Ribbon

1、pom

默认我们使用eureka的新版本时,它默认集成了ribbon:

![image-20210311150007069](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/image-20210311150007069.png)





2、对ribbion进行配置

在Eureka中已经使用过

```java
@Configuration
public class ApplicationContextConfig {

    @Bean
//    开启以下注解才会在controller的地址中找到服务的地址
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}

```

### 三、Ribbon自定义负载均衡算法

**Ribbon自身的负载均衡算法**

1. RoundRobinRule(轮询算法)
2. RandomRule(随机算法)
3. AvailabilityFilteringRule()：会先过滤由于多次访问故障而处于断路器跳闸状态的服务，还有并发的连接数量超过阈值的服务，然后对剩余的服务列表按照轮询策略进行访问
4. WeightedResponseTimeRule()：根据平均响应的时间计算所有服务的权重，响应时间越快服务权重越大被选中的概率越高，刚启动时如果统计信息不足，则使用RoundRobinRule策略，等统计信息足够会切换到WeightedResponseTimeRule
5. RetryRule()：先按照RoundRobinRule的策略获取服务，如果获取失败则在制定时间内进行重试，获取可用的服务。
6. BestAviableRule()：会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务
7. ZoneAvoidanceRule()：默认规则，符合判断server所在区域的性能和server的可用性选择服务器

**IRule接口,Riboon使用该接口,根据特定算法从所有服务中,选择一个服务**

**这个自定义的类不能放在@ComponentScan所扫描的当前包以及子包下，否则我们自定义的这个配置类就会被所有的Ribbon客户端所共享，也就是我们达不到特殊化指定的目的了。**

**==也就是不能放在主启动类所在的包及子包下==**

**1、额外创建一个包**

**2、创建配置类,指定负载均衡算法**

```java

@Configuration
public class MyRibbon {
 
	@Bean
	public IRule rule(){
		return new RandomRule();
	}
}
```

**3、在主启动类上加一个注解**

![](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/Ribbon%E7%9A%8418.png)



**表示,访问CLOUD_pAYMENT_SERVICE的服务时,使用我们指定的负载均衡算法**

==备注：从1.2版本开始 支持在application中配置==

### 四、OpenFeign简介

OpenFeign是一种声明式、模板化的HTTP客户端。在Spring Cloud中使用OpenFeign，可以做到使用HTTP请求访问远程服务，就像调用本地方法一样的，开发者完全感知不到这是在调用远程方法，更感知不到在访问HTTP请求。

==就是A要调用B,Feign就是在A中创建一个一模一样的B对外提供服务的的接口,我们调用这个接口,就可以服务到B==

### **五、使用OpenFeign**

之前的服务间调用,我们使用的是ribbon+RestTemplate，现在改为使用Feign

**1、pom文件**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**2、主启动类**

```java
@SpringBootApplication
@EnableFeignClients //启动openfeign
@EnableEurekaClient
public class FeignMain {
    public static void main(String args[]){
        SpringApplication.run(FeignMain.class, args);
    }
}
```

**3、fegin需要调用的其他的服务的接口**

```java
@FeignClient(value = "PAYMENT-SERVICE")
public interface PaymentFeignService {
    @GetMapping("payment/get/{id}")
    CommonResult<Payment> getPaymentById(@PathVariable("id") Long id);
}
```

**4、controller**

```java
@RestController
@Slf4j
public class FeignController {
    @Autowired
    private PaymentFeignService feignService;

    @GetMapping("/consumer/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id){
        return feignService.getPaymentById(id);
    }
}
```

**Feign默认使用ribbon实现负载均衡**

### 六、OpenFeign超时

OpenFeign默认等待时间是1秒,超过1秒,直接报错

**因为OpenFeign的底层是ribbon进行负载均衡,所以它的超时时间是由ribbon控制**

设置超时时间,可通过对配置文件进行配置:

1. ribbon.ReadTimeout=1000 //处理请求的超时时间，默认为1秒

2. ribbon.ConnectTimeout=1000 //连接建立的超时时长，默认1秒

3. ribbon.MaxAutoRetries=1 //同一台实例的最大重试次数，但是不包括首次调用，默认为1次

4. ribbon.MaxAutoRetriesNextServer=0 //重试负载均衡其他实例的最大重试次数，不包括首次调用，默认为0次

5. ribbon.OkToRetryOnAllOperations=false //是否对所有操作都重试，默认false

**Ribbon的注意事项：**

1. Ribbon的超时有2个：连接超时和处理超时，默认都是1秒。

2. Ribbon的默认重试也有2个：同一实例的重试次数和负载均衡的不同实例的重试次数，默认为1次和0次。也就是说，如果只有一个实例，连接超时重试1次，处理超时也重试1次。即：实际Ribbon的超时时间是 1秒×2+1秒×2=4秒。

3. Ribbon默认GET请求不论是连接失败还是处理失败都会重试，而对于非GET请求只对连接失败进行重试。

### 七、OpenFeign日志

**OpenFeign的日志级别有:**

1. NONE： 默认的，不显示任何日志

2. BASIC： 仅记录请求方法、URL、响应状态码以及执行时间

3. HEADERS：除了BASIC 中自定义的信息外，还有请求和响应的信息头

4. FULL： 除了HEADERS中定义的信息外， 还有请求和响应的正文以及元数据。

**1、先构建日志配置类**

```java

@Configuration
public class OpenFeignLogConfig {
 
    @Bean
    Logger.Level feignLoggerLeave(){
        return Logger.Level.FULL;
    }
}
```

**2、配置文件**

```yml

logging:
  level:
    # feign日志以什么级别监控哪个接口
    com.king.springcloud.service.OrderFeignService: debug
```



# 第三章：服务降级Hystrix

### 一、简介

​		分布式系统环境下，服务间类似依赖非常常见，一个业务调用通常依赖多个基础服务。如下图，对于同步调用，当库存服务不可用时，商品服务请求线程被阻塞，当有大批量请求调用库存服务时，最终可能导致整个商品服务资源耗尽，无法继续对外提供服务。并且这种不可用可能沿请求调用链向上传递，这种现象被称为雪崩效应。

### 二、hystrix中的重要概念:

**1、服务降级**

比如当某个服务繁忙,不能让客户端的请求一直等待,应该立刻返回给客户端一个备选方案

**2、服务熔断**

当某个服务出现问题,卡死了,不能让用户一直等待,需要关闭所有对此服务的访问

**然后调用服务降级**

**3、服务限流**

限流,比如秒杀场景,不能访问用户瞬间都访问服务器,限制一次只可以有多少请求

### 三、服务降级

首先在主启动类上,添加激活hystrix的注解

```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableHystrix
public class HystrixMain {
    public static void main(String[] args) {
        SpringApplication.run(HystrixMain.class, args);
    }
}
```



#### 1、方法降级:

为service的指定方法(会延迟的方法)添加@HystrixCommand注解

```java
    @GetMapping("/consumer/payment/get/{id}")
    @HystrixCommand(fallbackMethod = "hystrixHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "800")
    })
    public CommonResult<Payment> hystrixGet(@PathVariable Long id){
        //log.info("访问");
        return service.getPaymentById(id);
    }
//    方法降级
    public CommonResult<Payment> hystrixHandler(@PathVariable Long id){
        CommonResult<Payment> result = new CommonResult<Payment>(1,"系统错误或繁忙请稍后再试：方法fallback");
        return result;
    }
```

#### 2、全局降级

**配置一个全局的降级方法,所有方法都可以走这个降级方法,至于某些特殊创建,再单独创建方法**

```java
@RestController
@Slf4j
@DefaultProperties(defaultFallback = "hystrixGlobalHandler")
public class HystrixController {

    @Autowired
    private PaymentInfoService service;

    @GetMapping("/consumer/payment/get/{id}")
    @HystrixCommand
    public CommonResult<Payment> hystrixGet(@PathVariable Long id){
        //log.info("访问");
        return service.getPaymentById(id);
    }

//    全局
    public CommonResult<Payment> hystrixGlobalHandler(){
        CommonResult<Payment> result = new CommonResult<Payment>(1,"系统错误或繁忙请稍后再试：默认的fallback");
        return result;
    }
    // 注释部分可以为单个方法提供fallback
    // 现在使用的是全局fallback   有两种
    // 第一种使用 hystrix中的
    // @DefaultProperties(defaultFallback = "hystrixGlobalHandler")
    // @HystrixCommand
    // 第二种使用frign中内置的hystrix 需要修改yml
    // 在service层中实现接口来订制fallback方法
    // 感觉这个不是很好 使用第一种方法
}
```



### 四、服务熔断

**简介**

服务熔断 类似现实生活中的“保险丝“，当某个异常条件被触发，直接熔断保险丝来起到保护电路的作用，

熔断的触发条件可以依据不同的场景有所不同，比如统计一个时间窗口内失败的调用次数。

![img](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/907280-20180711164915812-924048716.png)



Closed：熔断器关闭状态（所有请求返回成功）

Open：熔断器打开状态（调用次数累计到达阈值或者比例，熔断器打开，服务直接返回错误）

Half Open：熔断器半开状态（默认时间过后，进入半熔断状态，允许定量服务请求，如果调用都成功，则认为恢复了，则关闭断路器，反之打开断路器）

#### 1、和服务降级的区别

Hystrix的服务降级，跟本篇的熔断达到的结果一样，都是返回预定的回调方法，那么服务熔断跟降级到底有什么区别呢？

个人总结：服务降级：当服务内部出现异常情况，将触发降级，这个降级是每次请求都会去触发，走默认处理方法defaultFallback

服务熔断：在一定周期内，服务异常次数达到设定的阈值或百分比，则触发熔断，熔断后，后面的请求将都走默认处理方法defaultFallback

#### 2、使用方法

```java
@RestController
@Slf4j
public class HystrixTestController {

    @GetMapping("/consumer/hystrix/test/{id}")
    @HystrixCommand(fallbackMethod = "hystrixTestFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"), //默认为true 是否启用熔断
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "20"),  //默认熔断触发的最小个数20/10s 10s内请求失败数量达到20个，断路器开。
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000"), //熔断多少秒后去尝试请求 默认值：5000
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50") //失败率达到多少百分比后熔断  默认值：50 出错百分比阈值，当达到此阈值后，开始短路。默认50%
    })
    public CommonResult<Payment> hystrixTestGet(@PathVariable Integer id){
        log.info("Test访问");
        if(id <= 0){
            throw new RuntimeException("参数不能为负数");
        }
        CommonResult<Payment> result = new CommonResult<Payment>(1,"参数大于0 正确");
        return result;
    }

    public CommonResult<Payment> hystrixTestFallback(@PathVariable Integer id){
        return new CommonResult<Payment>(2,"参数小于等于0 错误" +"id为"+ id);
    }
}
```



### 五、Hystrix服务监控

**HystrixDashboard**

Hystrix Dashboard，它主要用来实时监控Hystrix的各项指标信息。通过Hystrix Dashboard反馈的实时信息，可以帮助我们快速发现系统中存在的问题

大致效果如图

![image-20210311163739571](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/image-20210311163739571.png)







# 第四章：服务网关GateWay

### 一、前言

​		Spring Cloud Gateway 可以看做是一个 Zuul 1.x 的升级版和代替品，比 Zuul 2 更早的使用 Netty 实现异步 IO从而实现了一个简单、比 Zuul 1.x 更高效的、与 Spring Cloud 紧密配合的 API 网关。
​		Spring Cloud Gateway 里明确的区分了 Router 和 Filter，并且一个很大的特点是内置了非常多的开箱即用功能，并且都可以通过 SpringBoot 配置或者手工编码链式调用来使用。

### 二、工作原理

**三大核心概念**：

1. **Route(路由)**：路由是构建网关的基本模块，它由ID，目标URI，一系列的断言和过滤器组成，如果断言为true则匹配该路由，目标URI会被访问。
2. **Predicate(断言)**：这是一个java 8的Predicate，可以使用它来匹配来自HTTP请求的任何内容，如：请求头和请求参数。断言的输入类型是一个ServerWebExchange。
3. **Filter(过滤器)**：指的是Spring框架中GatewayFilter的实例，使用过滤器，可以在请求被路由前或者后对请求进行修改。

总结：web请求，通过一些匹配条件，定位到真正的服务节点。并在这个转发过程的前后，进行一些精细化控制。predicate就是匹配条件，而filter，就可以理解为一个无所不能的拦截器。有了这两个元素，再加上目标URI，就可以实现具体的路由了。

![在这里插入图片描述](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/1240)





### 三、断言

如果启动GateWay报错
  	**可能是GateWay模块引入了web-starter依赖,需要移除**

**1、配置文件和断言**

```yml
spring:
  application:
    name: cloud-gateway  #根据这个区分是哪个服务
  cloud:
    gateway:
      routes:
        - id: uri001  #路由的id 可随便起 建议配合服务名
          uri: lb://CONSUMER-HYSTRIX  #跳转到的地址 这里指向Hystrix消费者
          predicates:        #这里都是断言 符合特定要求即可通过
            - Path=/consumer/payment/get/**                  # 用户实际请求地址
           #- After=2020-02-21T15:51:37.485+08:00[Asia/Shanghai]   #这里表示,只有在==2020年的2月21的15点51分37秒==之后,访问才可以路由
           #- Cookie=username,zzyy   						#cookie:只有包含某些指定cookie(key,value),的请求才可以路由
           #- Header=X-Request-Id, \d+  					# 请求头要有X-Request-Id属性并且值为整数的正则表达式
          filters:
            - AddRequestHeader=X-Request-red,blue # 添加一个请求头
```

| 规则    | 实例                                                         | 说明                                                         |
| :------ | :----------------------------------------------------------- | :----------------------------------------------------------- |
| Path    | - Path=/gate/**,/rule/**                                     | 当请求的路径为gate、rule开头的时，转发到http://localhost:9023服务器上 |
| Before  | - Before=2017-01-20T17:42:47.789-07:00[America/Denver]       | 在某个时间之前的请求才会被转发到 http://localhost:9023服务器上 |
| After   | - After=2017-01-20T17:42:47.789-07:00[America/Denver]        | 在某个时间之后的请求才会被转发                               |
| Between | - Between=2017-01-20T17:42:47.789-07:00[America/Denver]<br />2017-01-21T17:42:47.789-07:00[America/Denver] | 在某个时间段之间的才会被转发                                 |
| Cookie  | - Cookie=chocolate, ch.p                                     | 名为chocolate的表单或者满足正则ch.p的表单才会被匹配到进行请求转发 |
| Header  | - Header=X-Request-Id, \d+                                   | 携带参数X-Request-Id或者满足\d+的请求头才会匹配              |
| Host    | - Host=www.hd123.com                                         | 当主机名为www.hd123.com的时候直接转发到http://localhost:9023服务器上 |
| Method  | - Method=GET                                                 | 只有GET方法才会匹配转发请求，还可以限定POST、PUT等请求方式   |
| Query   | -Query=smile                                                 | Query Route Predicate 支持传入两个参数，一个是属性名一个为属性值，属性值可以是正则表达式。<br />这样配置，只要请求中包含 smile 属性的参数即可匹配路由 |



### 三、Filter过滤器

**简介**

gateway 只有两种过滤器：“pre” 和 “post”。

1. PRE： 这种过滤器在请求被路由之前调用。
2. POST：这种过滤器在路由到微服务以后执行。

**按照作用范围分**

- 全局过滤器
- 非全局过滤器

非全局的过滤器又有两种实现方式，一种是上面的直接 实现GatewayFilter接口，

另一种是 自定义过滤器工厂（继承AbstractGatewayFilterFactory类） , 选择自定义过滤器工厂的方式，可以在配置文件中配置过滤器了。

#### **1、全局自定义过滤器**

```java
@Component
@Slf4j
public class LogFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("测试自定义全局过滤器");
        String a = String.valueOf(exchange.getRequest().getPath());
        log.info(a);
        if(a == null) {
            //如果不存在
            log.info("不能通过过滤器");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete(); //请求结束
        }
        return chain.filter(exchange);  //继续向下执行
    }

    @Override
    public int getOrder() {
//        这个表示加载过滤器的顺序 返回值越小,执行优先级越高
        return 0;
    }
}
```

#### 	**2、非全局过滤器通过（GatewayFilter接口）**

Spring Cloud Gateway内置了的过滤器工厂，足够是大部分场景使用，而且我们可以实现GatewayFilter和Ordered 这两个接口来自定义过滤器。代码如下：

```java
/**
 * 统计某个或者某种路由的处理时长
 */
public class CustomerGatewayFilter implements GatewayFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger( CustomerGatewayFilter.class );
    private static final String COUNT_START_TIME = "countStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(COUNT_START_TIME, Instant.now().toEpochMilli() );
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long startTime = exchange.getAttribute(COUNT_START_TIME);
                    long endTime=(Instant.now().toEpochMilli() - startTime);
                    log.info(exchange.getRequest().getURI().getRawPath() + ": " + endTime + "ms");
                })
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

需要将自定义的GatewayFilter 注册到router中，代码如下：

```java
@Bean
public RouteLocator customerRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
            .route(r -> r.path("/customer/**")
                    .filters(f -> f.filter(new CustomerGatewayFilter())
                            .addResponseHeader("X-Response-test", "test"))
                    .uri("http://httpbin.org:80/get")
                    .id("customer_filter_router")
            )
            .build();
}
```

使用 curl 测试，命令行输入：

```text
curl http://localhost:8080/customer/555
```

#### **3、非全局过滤器通过（继承AbstractGatewayFilterFactory类）**

**这样就可以在配置文件中配置过滤器**

过滤器工厂的顶级接口是GatewayFilterFactory，有2个两个较接近具体实现的抽象类，分别为AbstractGatewayFilterFactory和AbstractNameValueGatewayFilterFactory。

这2个类前者接收一个参数，比如它的实现类RedirectToGatewayFilterFactory；后者接收2个参数，比如它的实现类AddRequestHeaderGatewayFilterFactory类。现在需要将请求的日志打印出来，需要使用一个参数，这时可以参照RedirectToGatewayFilterFactory的写法。


```java
// 将这个bean注册
@Component
public class RequestTimeGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestTimeGatewayFilterFactory.Config> {


    private static final Log log = LogFactory.getLog(GatewayFilter.class);
    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";
    private static final String KEY = "withParams";

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(KEY);
    }

    public RequestTimeGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                        if (startTime != null) {
                            StringBuilder sb = new StringBuilder(exchange.getRequest().getURI().getRawPath())
                                    .append(": ")
                                    .append(System.currentTimeMillis() - startTime)
                                    .append("ms");
                            if (config.isWithParams()) {
                                sb.append(" params:").append(exchange.getRequest().getQueryParams());
                            }
                            log.info(sb.toString());
                        }
                    })
            );
        };
    }


    public static class Config {

        private boolean withParams;

        public boolean isWithParams() {
            return withParams;
        }

        public void setWithParams(boolean withParams) {
            this.withParams = withParams;
        }

    }
}

```

在上面的代码中 apply(Config config)方法内创建了一个GatewayFilter的匿名类，具体的实现逻辑跟之前一样，只不过加了是否打印请求参数的逻辑，而这个逻辑的开关是config.isWithParams()。静态内部类类Config就是为了接收那个boolean类型的参数服务的，里边的变量名可以随意写，但是要重写List shortcutFieldOrder()这个方法。


然后在yml中就可以直接配置。

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: elapse_route
        uri: http://httpbin.org:80/get
        filters:
        - RequestTime=false
        predicates:
        - After=2017-01-20T17:42:47.789-07:00[America/Denver]

```

==注意==

springboot约定过滤器的前缀为配置的name，而后面最好统一都是GatewayFilterFactory

- 我们在yml中配置的过滤器名称，如果你的自定义过滤器类名为：XxxGatewayFilterFactory，则名字为Xxx
- 而如果你的自定义过滤器类名中不以GatewayFilterFactory结尾，例如只为Xxx，则直接写Xxx即可。

但推荐按标准写法：自定义GatewayFilterFactory为：XxxGatewayFilterFactory，
 yml中配置name为Xxx

另：如果我们的参数只需要一个，我们可以在yml中配置时直接写成过：滤器名字=参数值即可，例如上面的StripPrefix=2



# 第五章：服务配置Spring Config

​		可以看到，每个微服务都需要一个配置文件，并且,如果有几个微服务都需要连接数据库，那么就需要配4次数据库相关配置，并且当数据库发生改动,那么需要同时修改4个微服务的配置文件才可以。

​		所以有了springconfig配置中心。

### 一、创建配置中心:

**1、使用github作为配置中心的仓库:**

最简单的配置中心，就是启动一个服务作为服务方，之后各个需要获取配置的服务作为客户端来这个服务方获取配置。

**配置git环境:**

```yml
  cloud:
    config:
      server:
        git:
          uri: git@github.com:blue-toad/springcloud-Config.git # github上面的仓库
          # 注意这里要使用老式秘钥生成方式 生成的老版秘钥
          # 新的秘钥不支持
          search-paths: /**
          # 搜索路径 根目录
          default-label: main
          # 默认分枝

# 访问路径
#          /{app}/{profile}    Configuration data for app in Spring profile (comma-separated).
#          /{app}/{profile}/{label}    Add a git label
#          /{app}/{profile}{label}/{path}  An environment-specific plain text config file (at "path")
# ******测试路径*****
# http://localhost:3344/config-center/application.yml
```

**2、主启动类**

```java
@EnableEurekaClient
@SpringBootApplication
@EnableConfigServer  //启用spring config
public class ConfigMain {
    public static void main(String[] args) {
        SpringApplication.run(ConfigMain.class,args);
    }
}
```

**3、测试运行**

测试3344是否可以从github上获取配置，启动3344和Eureka。

Spring Cloud Config 有它的一套访问规则，我们通过这套规则在浏览器上直接访问就可以。

```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

{application} 就是应用名称，对应到配置文件上来，就是配置文件的名称部分，例如我上面创建的配置文件。

{profile} 就是配置文件的版本，我们的项目有开发版本、测试环境版本、生产环境版本，对应到配置文件上来就是以 application-{profile}.yml 加以区分，例如application-dev.yml、application-sit.yml、application-prod.yml。

{label} 表示 git 分支，默认是 master 分支，如果项目是以分支做区分也是可以的，那就可以通过不同的 label 来控制访问不同的配置文件了。

上面的 5 条规则中，我们只看前三条，因为我这里的配置文件都是 yml 格式的。根据这三条规则，我们可以通过以下地址查看配置文件内容:

### 二、创建使用配置中心的客户端:

**1、配置文件**

注意这个配置文件就不是application.yml而是==bootstrap.yml==

这个配置文件的作用是,先到配置中心加载配置,然后加载到application.yml中
**1.加载顺序上的区别**
bootstrap.yml（bootstrap.properties）先加载
application.yml（application.properties）后加载
bootstrap.yml 用于应用程序上下文的引导阶段，由父Spring ApplicationContext加载。父ApplicationContext 被加载到使用application.yml的之前。
在 Spring Boot 中有两种上下文，一种是 bootstrap, 另外一种是 application, bootstrap 是应用程序的父上下文，也就是说 bootstrap 加载优先于 applicaton。bootstrap 主要用于从额外的资源来加载配置信息，还可以在本地外部配置文件中解密属性。这两个上下文共用一个环境，它是任何Spring应用程序的外部属性的来源。bootstrap 里面的属性会优先加载，它们默认也不能被本地相同配置覆盖。
**2.bootstrap/ application 的应用场。**

bootstrap.yml 和application.yml 都可以用来配置参数。
bootstrap.yml 可以理解成系统级别的一些参数配置，这些参数一般是不会变动的。
application 配置文件这个容易理解，pplication.yml 可以用来定义应用级别的，主要用于 Spring Boot 项目的自动化配置。

bootstrap 配置文件有以下几个应用场景。
使用 Spring Cloud Config 配置中心时，这时需要在 bootstrap 配置文件中添加连接到配置中心的配置属性来加载外部配置中心的配置信息；
一些固定的不能被覆盖的属性
一些加密/解密的场景；

```yml
spring:
  application:
    name: consumer-springconfig  #根据这个区分是哪个服务
  cloud:
    config:
#      配置文件名
      name: application
      #      文件后缀 dev prod 这样的 本项目中的其他文件用的后缀为2
      #      profile:
      #      分枝名
      #      label:

      discovery:
#        启动服务发现 默认为false
        enabled: true
        service-id: config-center
# 测试网址 http://localhost:804/configTest

```

### 三、问题和自动刷新

Spring Cloud Config 在项目启动时加载配置内容这一机制，导致了它存在一个缺陷，修改配置文件内容后，不会自动刷新。例如我们上面的项目，当服务已经启动的时候，去修改 github 上的配置文件内容，这时候，再次刷新页面，对不起，还是旧的配置内容，新内容不会主动刷新过来。
但是，总不能每次修改了配置后重启服务吧。如果是那样的话，还是不要用它了为好，直接用本地配置文件岂不是更快。

它提供了一个刷新机制，但是需要我们主动触发。那就是 @RefreshScope 注解并结合 actuator ，注意要引入 spring-boot-starter-actuator 包。

**1、修改3355,添加一个pom依赖:**

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

**2、修改配置文件,添加一个配置:**

```yml
management:
#暴露监控端点
  endpoints:
    web:
      exposure:
        include: "*"
```

**3、修改controller:**

在需要读取配置的类上增加 @RefreshScope 注解，我们是 controller 中使用配置，所以加在 controller 中

![](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/springconfig%E7%9A%8424.png)





**4、测试自动刷新**

**此时3355还不可以动态获取**

之后，重启 client 端，重启后，我们修改 github 上的配置文件内容，并提交更改，再次刷新页面，没有反应。没有问题。

接下来，我们发送 POST 请求到 http://localhost:3302/actuator/refresh 这个接口，用 postman 之类的工具即可，此接口就是用来触发加载新配置的。

**此时在刷新3355,发现可以获取到最新的配置文件了,这就实现了动态获取配置文件,因为3355并没有重启**

总结一下就是:

1. 我们启动好服务后

2. 运维人员,修改了配置文件,然后发送一个post请求通知3355

3. 3355就可以获取最新配置文件

**但是:**

​		如果有多个客户端怎么办(3355,3356,3357.....)虽然可以使用shell脚本,循环刷新。但是,可不可以使用广播,一次通知?这些springconfig做不到,需要使用springcloud Bus消息总线



# 第六章：消息总线Spring Bus

### 一、简介

接着上一篇，继续来讲。上一篇讲到，我们如果要去更新所有微服务的配置，在不重启的情况下去更新配置，只能依靠spring cloud config了，但是，是我们要一个服务一个服务的发送post请求，我们能受的了吗？这比之前的没配置中心好多了，那么我们如何继续避免挨个挨个的向服务发送Post请求来告知服务，你的配置信息改变了，需要及时修改内存中的配置信息。

这时候我们就不要忘记消息队列的发布订阅模型。让所有为服务来订阅这个事件，当这个事件发生改变了，就可以通知所有微服务去更新它们的内存中的配置信息。这时Bus消息总线就能解决，你只需要在springcloud Config Server端发出refresh，就可以触发所有微服务更新了。

![img](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/1202638-20180521203126866-1299643942.png)



第一种工作流程:

1. 提交代码触发post给客户端A发送bus/refresh
2. 客户端A接收到请求从Server端更新配置并且发送给Spring Cloud Bus
3. Spring Cloud bus接到消息并通知给其它客户端
4. 其它客户端接收到通知，请求Server端获取最新配置
5. 全部客户端均获取到最新的配置

第二种工作流程：

1. 提交代码触发post给Server端发送bus/refresh
2. Server端接收到请求并发送给Spring Cloud Bus
3. Spring Cloud bus接到消息并通知给其它客户端
4. 其它客户端接收到通知，请求Server端获取最新配置
5. 全部客户端均获取到最新的配置

==使用第二种方式==

### 二、使用Bus:

**1、配置rabbitmq环境:**

1. 安装erlang
2. 安装rabbitmq并且启用rabbitmq-plugins开启管理页面
3. 访问http://localhost:15672 账号密码为guest

**2、创建模块**

pom添加包

除了bus外还需要引入actuator 

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency
```

yml配置文件修改

==注意配置文件的名字,依然为bootstrap.yml==

```yml
server:
  port: 8080
spring:
  application:
    name: spring-cloud-config-server
  cloud:
    config:
      server:
      # 原来的git的配置
        git:
          uri: 
          search-paths: 
          username: 
          password: 
      # 配置rabbitmq
  rabbitmq:
    host: 217.0.0.1
    port: 5672
    username: 
    password: 
    	# actuator开启所有访问
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

**3、测试**

依次启动eureka，config-serve，config-client。

此时只需要刷新config server,即可让两个客户端动态获取最新的配置文件

```sh
curl -X POST http://localhost:8081/actuator/bus-refresh
```





# 第七章、消息驱动Spring Stream:

### 一、简单介绍

**业务流程:**

![image-20210308172450601](https://github.com/blue-toad/spring-cloud-learn/raw/main/pictures/image-20210308172450601.png)





**注解**

| 注解            | 解释                                                         |
| --------------- | ------------------------------------------------------------ |
| @Input          | 注解标识输入通道，通过该输入通道接收到的消息进入应用程序     |
| @Output         | 注解标识输出通道，发布的消息将通过该通道离开应用程序         |
| @StreamListener | 监听队列，用于消费者的队列的消息接收                         |
| @EnableBinding  | 指信道channel和exchange绑定在一起                            |
| 其他            | Binder ----方便的连接中间件，可以动态的改变消息类型（对应于Kafka的topic，RabbitMQ的exchange），这些都可以通过配置文件来实现 |



### 二、消息生产者

配置文件

```yml
  rabbitmq:
    host: localhost
    port: 5672   # 默认端口
    username: guest
    password: guest

  cloud:
    stream:

      bindings:
        myOutput:
          destination: myExchange     # 交换机exchange名称
          contentType: application/json
        myInput:
          destination: myExchange
          contentType: application/json

      binders:
        mybinder:
          type: rabbit



```

配置类，指定启用的binding

```java
@Component
public interface StreamConfig {
    // 表示通道的名称
    String input = "myExchange";
    String output = "myExchange";

    @Output(output)
    MessageChannel output();

    @Input(input)
    SubscribableChannel input();
}

```

在测试类中发送消息

```java
@SpringBootTest
public class MyStreamApplication8801Tests {
    @Autowired
    private StreamConfig stream;

    @Test
    public void send(){
        stream.output().send(MessageBuilder.withPayload("发送111消息").build());
        stream.output().send(MessageBuilder.withPayload("发送222消息").build());
        stream.output().send(MessageBuilder.withPayload("发送333消息").build());
        stream.output().send(MessageBuilder.withPayload("发送444消息").build());
    }
}

```



### 三、消息消费者:

同一个项目中

```java
@Slf4j
@Component
public class MessageReciveTest {
    @StreamListener(StreamConfig.input)
    public void handleGreetings(Message<String> message) {
        log.info("收到信息: {}", message);
    }
}

```

### 四、消费者分组

**同一个组一条消息只消费一次**

不写分组的话么默认每个消费者一个组

```yml
       bindings:
        myOutput:
          destination: myExchange     
          contentType: application/json
          group: Number1    # 添加分组信息
        myInput:
          destination: myExchange
          contentType: application/json
          group: Number1
```

**实现分组后会实现消息的持久化，未被分组消费消息会保存直到消费**



# 第八章、链路追踪Spring Sleuth

**sleuth解决的问题:**

随着业务的发展，系统规模也会越来越大，各微服务间的调用关系也越来越错综复杂。

通常一个客户端发起的请求在后端系统中会经过多个不同的微服务调用来协同产生最后的请求结果，在复杂的微服务架构系统中，几乎每一个前端请求都会形成一条复杂的分布式服务调用链路，在每条链路中任何一个依赖服务出现延迟过高或错误的时候都会引起请求最后的失败。

这时候，对于每一个请求，全链路调用的跟踪就变得越来越重要，通过实现对请求调用的跟踪可以帮助我们快速发现错误根烟以及监控分析每条链路上的性能瓶颈。

==spring-cloud-sleuth-zipkin + spring-cloud-starter-sleuth 就相当于直接引入 spring-cloud-starter-zipkin==



### 一、安装zipkin:

**官网的命令**

windows下可以使用git执行sh脚本

先下载sh脚本在用git执行实现下载

```cmd
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

**然后就可以访问web界面,  默认zipkin监听的端口是9411**

localhost:9411/zipkin/

### 二、使用sleuth:

**引入pom:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

这个包虽然叫zipkin但是,里面包含了zpikin与sleuth

**修改配置文件:**

```yml
  	#链路监控的配置
zipkin:
  base-url: http://localhost:9411
sleuth:
  sampler:
    # 采样频率 0-1 之间 1 表示采样全部
    probability: 1
```

然后正常使用SpringBoot中的Controller就可以，请求都会被zipkin监控到

