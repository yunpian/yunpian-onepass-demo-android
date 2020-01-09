# **本机号码校验 Android SDK 集成文档**

一键登录是云片提供的 APP 身份验证产品。整合三大运营商的手机号网关验证服务，可以替代 APP 上的注册、登录等场景需要使用到的短信验证码，优化 APP 用户在移动设备上的使用体验。

## 1. 整体集成流程

以下为本机号码的集成流程，整个集成流程是顺序进行的，在下一个步骤开始前请确保上一个步骤的操作都已正确完成。

#### 1.1. 获取应用 AppId

1、访问[云片官网](https://www.yunpian.com/entry?method=register)进行注册账号，联系客服或者销售申请开通移动认证服务。

2、成功开通服务后，进入[管理控制台](https://www.yunpian.com/admin/main)，进入移动认证的产品管理页面。

3、选择**新增应用**，填写应用名称以及 BundleId，系统会为该产品分配 **AppId**，应用进入审核状态，并联系客服进行审核，等待审核完成；

#### 2. 集成客户端 SDK

客户端 SDK 支持 **Android**、**iOS** 两大平台，涉及到网站主的两个 API 请求以及与服务端的几个 API 请求。客户端 SDK 的业务使用流程为：

1、调用初始化接口，初始化本机号码校验 SDK，返回 SDK 实例对象；

2、调用本机号码接口，完成本机号码校验，将成功回调的 cid 返回给开发者自己的服务端。

#### 3. 从接口获取校验结果(开发者服务端)

开发者服务端从客户端请求解析到相关参数后，接口能正确返回校验结果，即代表集成成功。

[云片移动认证服务端接入文档](https://github.com/yunpian/yunpian-onepass-demo-android/blob/master/云片移动认证服务端接入文档.md)

## 2. 集成本机号码校验 SDK

#### 方式一：远程依赖集成 (推荐)
需要确保主项目 build.gradle 文件中声明了 jcenter() 配置

```
implementation 'com.yunpian:onepass:1.0.4'
```

#### 方式二：手动导入 SDK

将获取的 sdk 中的 aar 文件放到工程中的libs文件夹下，然后在 app 的 build.gradle 文件中增加如下代码

```java
repositories {
    flatDir {
        dirs 'libs'
    }
}
```

在 dependencies 依赖中增加对 aar 包的引用

```java
// aar 名称和版本号以下载下来的最新版为准
implementation(name: 'qipeng-onepass-v1.0.4', ext: 'aar')
```



## 3. 开始使用

#### 初始化

使用开发者自己的 appId 进行初始化 SDK

```java
QPOnePass.getInstance().init(context, "your appId", callback);
```

#### 权限获取

预取号和取号之前必须先授予相关权限

```java
int readPhonePermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
int accessFineLocationCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
if (readPhonePermissionCheck != PackageManager.PERMISSION_GRANTED
        || accessFineLocationCheck != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
}
```

#### 调用校验

```java
QPOnePass.getInstance().getToken(phoneNum,callback);
```

#### 打开短信回退界面

```java
QPOnePass.getInstance().requestSmsToken(callback);
```

## 4. 接口响应码（status）释义

| 响应码  | 具体描述
| ------ | ------------------------------------
| 200    | 成功
| -1     | 未知错误
| -2     | AppId 不能为空，请检查是否初始化 SDK
| -3     | 初始化失败
| -20100 | 手机号未传
| -20101 | custom未传
| -20200 | 当前网络不可用
| -20201 | 当前手机没有电话卡
| -20202 | 当前手机有电话卡但是未开启数据网络
| -20203 | ConnectivityManager不存在
| -20204 | WIFI下走数据流量出现错误
| -20205 | 检测当前走数据流量超时,请检测当前卡是否欠费
| -20206 | 开启enableHIPRI失败
| -20207 | WIFI下请求切换网络失败
| -30200 | SDK内部请求PreGateWay接口超时
| -40101 | 移动运营商获取token失败
| -40104 | 移动不支持的网络制式
| -40201 | 联通运营商获取token失败
| -40204 | 联通不支持的网络制式(不支持2G)
| -40301 | 电信运营商获取token失败
| -40305 | 电信不支持的网络制式(不支持2G, 3G)
| -50100 | SDK内部请求PreGateWay接口解密失败
| -50101 | SDK内部请求PreGateWay接口返回错误

