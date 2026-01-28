# MG301 LoRa页面需求记录

请你按照给你的原型图页面，实现 MG301 LoRa页面，在现有 LoRa 页面 `LoraSettingFragment.kt` 上扩展，兼容 MG301 的 LoRa 参数配置。
##  相关指令说明

### **LoRa涉及的指令**

```  
（1）获取参数
发送：
$cmd=md_getcqloractrl

应答：
$cmd=md_getcqloractrl&airbaud=3&chl=10&outpwr=20&netid=1&localid=10&dstid=2&loratype=1

（2）设置参数	
发送：
$cmd=md_setcqloractrl&airbaud=3&chl=10&outpwr=20&netid=1&localid=10&dstid=2&loratype=1

应答：
设置成功：$cmd=md_setcqloractrl&result=succ
设置失败：$cmd=md_setcqloractrl&result=fail&reason=
```

####  指令参数字段说明

| 关键字 | 说明 | 数据类型 | 示例 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| loratype | LORA 模组型号 | int | 1 | 1: F8L10C 2: TP1107 |
| chl | 信道 | int | chl | 取值 1~30，默认 20 |
| outpwr | 发射功率 | int | 20 | 取值 5~20，默认 20 |
| airbaud | 空中速率 | int | 3 | 取值 1~6，默认 3 |
| netid | 网络编号 | int | 1 | 取值 1~10，默认 1 |
| localid | 本机地址 | int | 2 | 取值 1~20，默认 2 |
| dstid | 目标地址 | int | 1 | 取值 1~20，默认 1 |