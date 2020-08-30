package com.shmedo.mcloudapp.deviceconfig.model;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：    查询当前公司指令列表返回实体
 */
public class CompanyCmdWrapper {


    /**
     * totalCount : 54
     * totalPage : 6
     * currentPageData : [{"id":6,"name":"传感器遥测","content":"$cmd=sample","allowDeviceTypeInfo":[],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":24,"name":"固件升级","content":"$cmd=md_upgrade&url={url}&size={size}&md5={md5}","allowDeviceTypeInfo":[],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":107,"name":"恢复设备出厂设置","content":"$cmd=md_reset","allowDeviceTypeInfo":[],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":127,"name":"获取NMEA输出内容及输出频率","content":"$cmd=md_getnmeatime","allowDeviceTypeInfo":[{"deviceTypeID":6,"deviceTypeName":"E60"},{"deviceTypeID":10,"deviceTypeName":"E40"},{"deviceTypeID":13,"deviceTypeName":"E40S"}],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":121,"name":"获取Ntrip服务相关参数","content":"$cmd=md_getntrip","allowDeviceTypeInfo":[{"deviceTypeID":6,"deviceTypeName":"E60"},{"deviceTypeID":10,"deviceTypeName":"E40"},{"deviceTypeID":13,"deviceTypeName":"E40S"}],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":123,"name":"获取RTK模式相关参数","content":"$cmd=md_getrtk","allowDeviceTypeInfo":[{"deviceTypeID":6,"deviceTypeName":"E60"},{"deviceTypeID":10,"deviceTypeName":"E40"},{"deviceTypeID":13,"deviceTypeName":"E40S"}],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":109,"name":"获取北斗数传终端指令","content":"$cmd=md_getbdterminal","allowDeviceTypeInfo":[{"deviceTypeID":4,"deviceTypeName":"DAS"},{"deviceTypeID":5,"deviceTypeName":"DAG"}],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":129,"name":"获取本地解算指令","content":"$cmd=md_getembedams","allowDeviceTypeInfo":[{"deviceTypeID":6,"deviceTypeName":"E60"},{"deviceTypeID":10,"deviceTypeName":"E40"},{"deviceTypeID":13,"deviceTypeName":"E40S"}],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":117,"name":"获取采集控制相关参数","content":"$cmd=md_getcollctrl","allowDeviceTypeInfo":[{"deviceTypeID":4,"deviceTypeName":"DAS"},{"deviceTypeID":5,"deviceTypeName":"DAG"}],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"},{"id":115,"name":"获取采集器和传感器之间的通信波特率","content":"$cmd=md_getcolluart","allowDeviceTypeInfo":[{"deviceTypeID":4,"deviceTypeName":"DAS"},{"deviceTypeID":5,"deviceTypeName":"DAG"}],"definitionType":0,"cmdType":0,"cmdTypeString":"常用指令"}]
     */

    private int totalCount;
    private int totalPage;
    private List<CurrentPageCmdBean> currentPageData;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<CurrentPageCmdBean> getCurrentPageData() {
        return currentPageData;
    }

    public void setCurrentPageData(List<CurrentPageCmdBean> currentPageData) {
        this.currentPageData = currentPageData;
    }

    public static class CurrentPageCmdBean {
        /**
         * id : 6
         * name : 传感器遥测
         * content : $cmd=sample
         * allowDeviceTypeInfo : []
         * definitionType : 0
         * cmdType : 0
         * cmdTypeString : 常用指令
         */

        private int id;
        private String name;
        private String content;
        private int definitionType;
        private int cmdType;
        private String cmdTypeString;
        private List<CmdDeviceTypeInfo> allowDeviceTypeInfo;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getDefinitionType() {
            return definitionType;
        }

        public void setDefinitionType(int definitionType) {
            this.definitionType = definitionType;
        }

        public int getCmdType() {
            return cmdType;
        }

        public void setCmdType(int cmdType) {
            this.cmdType = cmdType;
        }

        public String getCmdTypeString() {
            return cmdTypeString;
        }

        public void setCmdTypeString(String cmdTypeString) {
            this.cmdTypeString = cmdTypeString;
        }

        public List<CmdDeviceTypeInfo> getAllowDeviceTypeInfo() {
            return allowDeviceTypeInfo;
        }

        public void setAllowDeviceTypeInfo(List<CmdDeviceTypeInfo> allowDeviceTypeInfo) {
            this.allowDeviceTypeInfo = allowDeviceTypeInfo;
        }
    }
}
