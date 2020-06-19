package com.shmedo.mcloudapp.maps.util;

import com.amap.api.services.core.AMapException;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/19 <br/>
 * 描述：    TODO
 */
public class MapErrorUtil {

    public static String getErrorMsg(int rCode) {
        switch (rCode) {
            //服务错误码
            case 1001:
                return AMapException.AMAP_SIGNATURE_ERROR;
            case 1002:
                return AMapException.AMAP_INVALID_USER_KEY;
            case 1003:
                return AMapException.AMAP_SERVICE_NOT_AVAILBALE;
            case 1004:
                return AMapException.AMAP_DAILY_QUERY_OVER_LIMIT;
            case 1005:
                return AMapException.AMAP_ACCESS_TOO_FREQUENT;
            case 1006:
                return AMapException.AMAP_INVALID_USER_IP;
            case 1007:
                return AMapException.AMAP_INVALID_USER_DOMAIN;
            case 1008:
                return AMapException.AMAP_INVALID_USER_SCODE;
            case 1009:
                return AMapException.AMAP_USERKEY_PLAT_NOMATCH;
            case 1010:
                return AMapException.AMAP_IP_QUERY_OVER_LIMIT;
            case 1011:
                return AMapException.AMAP_NOT_SUPPORT_HTTPS;
            case 1012:
                return AMapException.AMAP_INSUFFICIENT_PRIVILEGES;
            case 1013:
                return AMapException.AMAP_USER_KEY_RECYCLED;
            case 1100:
                return AMapException.AMAP_ENGINE_RESPONSE_ERROR;
            case 1101:
                return AMapException.AMAP_ENGINE_RESPONSE_DATA_ERROR;
            case 1102:
                return AMapException.AMAP_ENGINE_CONNECT_TIMEOUT;
            case 1103:
                return AMapException.AMAP_ENGINE_RETURN_TIMEOUT;
            case 1200:
                return AMapException.AMAP_SERVICE_INVALID_PARAMS;
            case 1201:
                return AMapException.AMAP_SERVICE_MISSING_REQUIRED_PARAMS;
            case 1202:
                return AMapException.AMAP_SERVICE_ILLEGAL_REQUEST;
            case 1203:
                return AMapException.AMAP_SERVICE_UNKNOWN_ERROR;
            //sdk返回错误
            case 1800:
                return AMapException.AMAP_CLIENT_ERRORCODE_MISSSING;
            case 1801:
                return AMapException.AMAP_CLIENT_ERROR_PROTOCOL;
            case 1802:
                return AMapException.AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION;
            case 1803:
                return AMapException.AMAP_CLIENT_URL_EXCEPTION;
            case 1804:
                return AMapException.AMAP_CLIENT_UNKNOWHOST_EXCEPTION;
            case 1806:
                return AMapException.AMAP_CLIENT_NETWORK_EXCEPTION;
            case 1900:
                return AMapException.AMAP_CLIENT_UNKNOWN_ERROR;
            case 1901:
                return AMapException.AMAP_CLIENT_INVALID_PARAMETER;
            case 1902:
                return AMapException.AMAP_CLIENT_IO_EXCEPTION;
            case 1903:
                return AMapException.AMAP_CLIENT_NULLPOINT_EXCEPTION;
            //云图和附近错误码
            case 2000:
                return AMapException.AMAP_SERVICE_TABLEID_NOT_EXIST;
            case 2001:
                return AMapException.AMAP_ID_NOT_EXIST;
            case 2002:
                return AMapException.AMAP_SERVICE_MAINTENANCE;
            case 2003:
                return AMapException.AMAP_ENGINE_TABLEID_NOT_EXIST;
            case 2100:
                return AMapException.AMAP_NEARBY_INVALID_USERID;
            case 2101:
                return AMapException.AMAP_NEARBY_KEY_NOT_BIND;
            case 2200:
                return AMapException.AMAP_CLIENT_UPLOADAUTO_STARTED_ERROR;
            case 2201:
                return AMapException.AMAP_CLIENT_USERID_ILLEGAL;
            case 2202:
                return AMapException.AMAP_CLIENT_NEARBY_NULL_RESULT;
            case 2203:
                return AMapException.AMAP_CLIENT_UPLOAD_TOO_FREQUENT;
            case 2204:
                return AMapException.AMAP_CLIENT_UPLOAD_LOCATION_ERROR;
            //路径规划
            case 3000:
                return AMapException.AMAP_ROUTE_OUT_OF_SERVICE;
            case 3001:
                return AMapException.AMAP_ROUTE_NO_ROADS_NEARBY;
            case 3002:
                return AMapException.AMAP_ROUTE_FAIL;
            case 3003:
                return AMapException.AMAP_OVER_DIRECTION_RANGE;
            //短传分享
            case 4000:
                return AMapException.AMAP_SHARE_LICENSE_IS_EXPIRED;
            case 4001:
                return AMapException.AMAP_SHARE_FAILURE;

            default:
                return "查询失败：" + rCode;
        }
    }
}
