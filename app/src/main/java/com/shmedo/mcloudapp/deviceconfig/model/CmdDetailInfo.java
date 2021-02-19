package com.shmedo.mcloudapp.deviceconfig.model;

import com.shmedo.mcloudapp.deviceconfig.model.params.CmdParam;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/30 <br/>
 * 描述：     TODO #gh#
 */
public class CmdDetailInfo {


    /**
     * cmd : {"id":24,"companyID":-1,"chnName":"固件升级","engName":"md_upgrade","definitionType":0,"createUser":4,"createTime":"2019-12-27 10:53:00","cmdType":0}
     * parameters : [{"id":13,"cmdID":24,"parameterChnName":"固件HTTP地址","parameterEngName":"url","parameterType":"String","defaultValue":null},{"id":14,"cmdID":24,"parameterChnName":"固件大小","parameterEngName":"size","parameterType":"Int","defaultValue":null},{"id":15,"cmdID":24,"parameterChnName":"固件MD5","parameterEngName":"md5","parameterType":"String","defaultValue":null}]
     * allowTypes : null
     * cmdType : 0
     * cmdTypeString : 常用指令
     */

    private CmdBean cmd;
    private int cmdType;
    private String cmdTypeString;
    private List<CmdParam> parameters;
    private List<CmdDeviceTypeInfo> allowTypes;

    public CmdBean getCmd() {
        return cmd;
    }

    public void setCmd(CmdBean cmd) {
        this.cmd = cmd;
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

    public List<CmdParam> getParameters() {
        return parameters;
    }

    public void setParameters(List<CmdParam> parameters) {
        this.parameters = parameters;
    }

    public List<CmdDeviceTypeInfo> getAllowTypes() {
        return allowTypes;
    }

    public void setAllowTypes(List<CmdDeviceTypeInfo> allowTypes) {
        this.allowTypes = allowTypes;
    }

    public static class CmdBean {
        /**
         * id : 24
         * companyID : -1
         * chnName : 固件升级
         * engName : md_upgrade
         * definitionType : 0
         * createUser : 4
         * createTime : 2019-12-27 10:53:00
         * cmdType : 0
         */

        private int id;
        private int companyID;
        private String chnName;
        private String engName;
        private int definitionType;
        private int createUser;
        private String createTime;
        private int cmdType;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCompanyID() {
            return companyID;
        }

        public void setCompanyID(int companyID) {
            this.companyID = companyID;
        }

        public String getChnName() {
            return chnName;
        }

        public void setChnName(String chnName) {
            this.chnName = chnName;
        }

        public String getEngName() {
            return engName;
        }

        public void setEngName(String engName) {
            this.engName = engName;
        }

        public int getDefinitionType() {
            return definitionType;
        }

        public void setDefinitionType(int definitionType) {
            this.definitionType = definitionType;
        }

        public int getCreateUser() {
            return createUser;
        }

        public void setCreateUser(int createUser) {
            this.createUser = createUser;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public int getCmdType() {
            return cmdType;
        }

        public void setCmdType(int cmdType) {
            this.cmdType = cmdType;
        }
    }


}
