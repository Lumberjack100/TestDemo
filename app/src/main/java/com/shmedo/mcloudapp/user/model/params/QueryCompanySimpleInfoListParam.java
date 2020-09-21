package com.shmedo.mcloudapp.user.model.params;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/9/1 <br/>
 * 描述：    查询用户在其中具有权限的公司接口入参
 */
public class QueryCompanySimpleInfoListParam {

    private String companyName;
    private int pageSize;
    private int currentPage;
    private boolean includeSubCompany=false;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean isIncludeSubCompany() {
        return includeSubCompany;
    }

    public void setIncludeSubCompany(boolean includeSubCompany) {
        this.includeSubCompany = includeSubCompany;
    }
}
