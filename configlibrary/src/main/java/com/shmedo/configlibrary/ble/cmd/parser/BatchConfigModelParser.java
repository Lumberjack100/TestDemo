package com.shmedo.configlibrary.ble.cmd.parser;


import android.text.TextUtils;

import com.shmedo.configlibrary.ble.cmd.Batch;
import com.shmedo.configlibrary.ble.cmd.Command;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.exception.DASParameterException;
import com.shmedo.configlibrary.ble.utils.StringUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by adu on 2018/1/4.
 * 解析批量配置模式
 */
public class BatchConfigModelParser {
    private static final String REBOOT_COMMAND = Command.COMMAND_HEADER + CommandType.REBOOT_DEVICE.toString();
    private static final int BATCH_COMMAND_SIZE = 30;
    private static final int BATCH_COMMAND_SIZE_WITHOUT_HEAD_TAIL=BATCH_COMMAND_SIZE-2;

    public List<Batch> parse(List<String> result) {
        checkArgument(result);
        int index = -1;
        for (int i = 0; i < result.size(); ++i) {
            String cmd = result.get(i);
            if (cmd.startsWith(REBOOT_COMMAND)) {
                index = i;
            }
        }

        return devideCommand(result, index > -1);
    }

    private List<Batch> devideCommand(List<String> cmds, boolean hasReboot) {
        List<Batch>result=new LinkedList<>();
        String rebootCmd=null;
        if (hasReboot) {
            rebootCmd=cmds.get(cmds.size()-1);
            cmds=cmds.subList(0,cmds.size()-1);
        }
        int allCount=cmds.size();
        int allPage=(allCount-1)/BATCH_COMMAND_SIZE_WITHOUT_HEAD_TAIL+1;
        for(int i=1;i<=allPage;++i)
        {
            int beginIndex=(i-1)*BATCH_COMMAND_SIZE_WITHOUT_HEAD_TAIL;
            int endIndex=i*BATCH_COMMAND_SIZE_WITHOUT_HEAD_TAIL>cmds.size()?
                    cmds.size():(i*BATCH_COMMAND_SIZE_WITHOUT_HEAD_TAIL);
            List<String>tempListString=cmds.subList(beginIndex,endIndex);
            List<String>batchListString=buildBatch(tempListString);

            Batch batch=Batch.newBuilder()
                    .setCommands(batchListString)
                    .setOrder(i-1)
                    .setReboot(false)
                    .build();
            result.add(batch);
        }
        if(rebootCmd!=null)
        {
            Batch rebootBatch=Batch.newBuilder()
                    .setCommands(buildBatch(Arrays.asList(rebootCmd)))
                    .setReboot(true)
                    .setOrder(result.size())
                    .build();
            result.add(rebootBatch);
        }
        return result;
    }


    private List<String> buildBatch(List<String> cmds) {
        if (StringUtil.isNullOrEmptyList(cmds))
            throw new IllegalArgumentException("命令不能为空");
        if(cmds.size()>BATCH_COMMAND_SIZE-2)
            throw new IllegalArgumentException("一个批次最多只能出现"+ (BATCH_COMMAND_SIZE - 2) +"指令");
        List<String>result=new LinkedList<>();
        String batchBegin= CommandManager.getInstance().getCommand(CommandType.BATCH_BEGIN,null);
        String batchEnd=CommandManager.getInstance().getCommand(CommandType.BATCH_END,null);
        result.add(batchBegin);
        for(int i=0;i<cmds.size();++i)
        {
            String iCmd=cmds.get(i);
            String seq=String.format("%03d",(i+1));
            StringBuilder builder=new StringBuilder(iCmd);
            builder.insert(2,seq);
            result.add(builder.toString());
        }
        result.add(batchEnd);
        return result;
    }

    private void checkArgument(List<String> args) {
        if (args == null || args.size() <= 0)
            throw new DASParameterException("集合为空或者不包含指令");
        int rebootCount = 0;
        for (String s : args) {
            if (TextUtils.isEmpty(s))
                throw new DASParameterException("指令为空");
            if (s.startsWith(REBOOT_COMMAND))
                rebootCount++;
        }
        if (rebootCount > 1)
            throw new DASParameterException("一个指令序列中最多只能出现一次重启指令");
    }


    public CommandType commandType() {
        return CommandType.BATCH_END;
    }


    /**
     "##335/r/n" +           //开启批量配置模式

     "##0051/r/n" +          //雨量站
     "##12150/r/n" +         //雨量计精度

     "##4011/r/n" +          //开启渗压计功能
     "##4021/r/n" +          //设置数字水位计地址 取值1~255
     "##40310,10/r/n" +      //设置数字水位计深度触发值，温度触发值
     "##404-1.2,10.5/r/n" +  //设置数字水位计深度修正值，温度修正值

     "##1001600500500/r/n" + //获取XX采集器配置--地址、待机时长、工作时长、采集间隔

     "##101120500/r/n"+      //传感器类型--地址、类型、触发值、修正值

     "##0031/r/n"+           //通讯方式GPRS
     "##2011test.shmedo.cn9001/r/n"+ //服务器地址
     "##14360/r/n"+          //数据上报间隔
     "##999336/r/n";
     * @param strs
     * @return
     */
}
