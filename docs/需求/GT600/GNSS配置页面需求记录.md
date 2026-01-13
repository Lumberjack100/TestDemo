# GNSS配置页面需求记录

## 具体实现需求

请你按照给你的原型图页面，实现 GT600 GNSS配置页面，页面为 `GT600GNSSConfigFragment.kt`。包括 "卫星信息"、"双天线参数"、"RTCM参数"、"NMEA参数" 四个分组部分的参数配置。

 1. **卫星信息部分**：目前不支持"截至高度角"这个功能配置，页面上先设计保留。
 2. IOTCommandType.kt 中"GT600 指令"中需要增加以下指令定义：
    - md_getgnssctl
    - md_setgnssctl
    - md_cfgnmeavtgout

 3. 在导航配置 gt600_graph.xml 中添加 fragment、action 节点配置，更新 GT600HomeFragment 导航配置
 4. 添加详细的注释说明

##  相关指令说明

### **双天线参数涉及的指令**

```  
（1）获取参数
发送：
$cmd=md_cfgnmeavtgout&method=0

应答：
$cmd=md_cfgnmeavtgout&method=0&switch=1&antdist=50.44&report_freq=1

（2）设置参数	
发送：
$cmd=md_cfgnmeavtgout&method=1&switch=0&antdist=50.44&report_freq=1

应答：
设置成功：$cmd=md_cfgnmeavtgout&method=1&result=succ
设置失败：$cmd=md_cfgnmeavtgout&method=1&result=fail&reason=
```

####  指令参数字段说明

| 参数 | 参数 | 数据类型 | 示例 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| method | 功能选择 | int | 0 | 0-获取参数<br>1-配置参数 |
| switch | 功能开关 | int | 0 | 0-关闭 （默认）<br>1-开启 |
| antdist | 天线距离 | float | 50.78 | 单位：厘米。数值大于零，否则要 |
| report_freq | 输出频率 | int | 1 | 单位：秒<br>支持 1、5、10、<br>15、30、60 选择 |

### **RTCM参数涉及的指令**

```  
（1）获取参数
发送：
$cmd=md_getgnssctl

应答：
$cmd=md_getgnssctl&encrypttype=0&rtcmobstime=15&rtcmephtime=60&onlybd=0

（2）设置参数	
发送：
$cmd=md_setgnssctl&encrypttype=0&rtcmobstime=15&rtcmephtime=60&onlybd=0

应答：
设置成功：$cmd=md_setgnssctl&result=succ
设置失败：$cmd=md_setgnssctl&result=fail&reason=
```

####  指令参数字段说明

| 关键字 | 说明 | 数据类型 | 示例 | 备注 |
| :--- | :--- | :--- | :--- | :--- |
| encrypttype | 是否启用加密 | int | 0 | 不加密：0<br>软加密：1<br>硬加密：2 |
| rtcmobstime | rtcm 观测数据输出频率 | int | 15 | 单位：秒，默认15，取值 1，5，10，15，30 |
| rtcmephtime | rtcm 星历数据输出频率 | int | 60 | 单位：秒，默认60，取值 1，5，10，15，30，60 |
| onlybd | 单北斗功能 | int | 0 | 不开启：0<br>开启：1 |

### **NMEA参数涉及的指令**

```  
（1）获取参数
发送：
$cmd=md_getnmeatime

应答：
$cmd=md_getnmeatime&gga=1&rmc=1&vtg=1&gsv=1&gsa=1

（2）设置参数	
发送：
$cmd=md_setnmeatime&gga=1&rmc=1&vtg=1&gsv=1&gsa=1

应答：
设置成功：$cmd=md_setnmeatime&result=succ
设置失败：$cmd=md_setnmeatime&result=fail&reason=
```

####  指令参数字段说明

| 关键字 | 说明 | 数据类型 | 示例 | 备注 |
| :--- | :--- | :--- | :--- | :--- |
| gga | 位置信息 | int | 1 | 单位：秒；<br>20Hz-0.05<br>10Hz-0.1<br>5Hz-0.2<br>1Hz-1<br>5s-5<br>10s-10<br>15s-15<br>30s-30<br>60s-60<br>关闭-0 |
| rmc | 最简导航传输信息 | int | 1 | 同上 |
| vtg | 地面速度信息 | int | 1 | 同上 |
| gsv | 可视卫星状态 | int | 1 | 同上 |
| gsa | 参与定位卫星以及 DOP 值等信息 | int | 1 | 同上 |


---

## 需求变更

请你完善 `GT600GNSSConfigFragment.kt` "截至高度角"项的配置功能。

 1.  IOTCommandType.kt 中：
    - 添加 md_elevation_mask 指令。

 2. 在 `com.shmedo.lib.cmd.base.iot_cmd.model.gt600` 、 `com.shmedo.lib.cmd.base.iot_cmd.assemble.entity.gt600` 包中新建相应的实体类，在 `com.shmedo.lib.cmd.base.iot_cmd.parser` 包下创建` 包中新建对应的解析器类，以及在 IOTParserRegistry.kt 中注册。

##  相关指令说明

### **功能开关参数涉及的指令**

```  
（1）获取参数
发送：
$cmd=md_elevation_mask&method=0

应答：
$cmd=md_elevation_mask&method=0&angle=15

（2）设置参数	
发送：
$cmd=md_elevation_mask&method=1&angle=15

应答：
设置成功：$cmd=md_elevation_mask&result=succ
设置失败：$cmd=md_elevation_mask&result=fail&reason=
```

####  指令参数字段说明

| 关键字 | 说明 | 数据类型 | 示例 | 备注 |
| :--- | :--- | :--- | :--- | :--- |
| method | 功能选择 | int | 1 | 查询参数：0 <br> 设置参数：1 |
| angle | 高度| int | 15 | 单位：°，取值5-90° <br> 间隔5°，默认15° |