package com.fangxuele.tool.push.logic.msgthread;

import cn.hutool.json.JSONUtil;
import com.fangxuele.tool.push.logic.InfinityTaskRunThread;
import com.fangxuele.tool.push.logic.PushData;
import com.fangxuele.tool.push.logic.msgsender.IMsgSender;
import com.fangxuele.tool.push.logic.msgsender.SendResult;
import com.fangxuele.tool.push.ui.form.InfinityForm;
import com.fangxuele.tool.push.util.ConsoleUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * <pre>
 * 消息异步发送服务线程
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">RememBerBer</a>
 * @since 2019/7/7.
 */
public class MsgInfinitySendThread extends Thread {

    private IMsgSender iMsgSender;

    private InfinityTaskRunThread infinityTaskRunThread;

    public MsgInfinitySendThread(IMsgSender msgSender) {
        this.iMsgSender = msgSender;
        PushData.activeThreadConcurrentLinkedQueue.offer(this.getName());
        PushData.threadStatusMap.put(this.getName(), true);
    }

    public MsgInfinitySendThread(IMsgSender msgSender, InfinityTaskRunThread infinityTaskRunThread) {
        this.iMsgSender = msgSender;
        infinityTaskRunThread.activeThreadConcurrentLinkedQueue.offer(this.getName());
        infinityTaskRunThread.threadStatusMap.put(this.getName(), true);
        this.iMsgSender = msgSender;
        this.infinityTaskRunThread = infinityTaskRunThread;
    }

    @Override
    public void run() {

        while (infinityTaskRunThread.running && infinityTaskRunThread.threadStatusMap.get(this.getName()) && !infinityTaskRunThread.toSendConcurrentLinkedQueue.isEmpty()) {
            String[] msgData = PushData.toSendConcurrentLinkedQueue.poll();
            if (msgData == null) {
                continue;
            }
            try {
                SendResult sendResult = iMsgSender.send(msgData);
                if (sendResult.isSuccess()) {
                    infinityTaskRunThread.increaseSuccess();
                    // 保存发送成功
//                    ConsoleUtil.pushLog(infinityTaskRunThread.getLogWriter(),Thread.currentThread().getName() + "：发送成功：" + msgData[0]);
                    infinityTaskRunThread.sendSuccessList.add(msgData);
                } else {
                    infinityTaskRunThread.increaseFail();
                    InfinityForm.getInstance().getPushFailCount().setText(String.valueOf(PushData.failRecords));
                    // 保存发送失败
                    infinityTaskRunThread.sendFailList.add(msgData);
                    ConsoleUtil.pushLog(infinityTaskRunThread.getLogWriter(), "发送失败:" + sendResult.getInfo() + ";msgData:" + JSONUtil.toJsonPrettyStr(msgData));
                }
            } catch (Exception e) {
                infinityTaskRunThread.increaseFail();
                ConsoleUtil.pushLog(infinityTaskRunThread.getLogWriter(), "发送异常：" + ExceptionUtils.getStackTrace(e));
                // 保存发送失败
                infinityTaskRunThread.sendFailList.add(msgData);
            }
            // 已处理+1
            infinityTaskRunThread.increaseProcessed();
        }
        infinityTaskRunThread.activeThreadConcurrentLinkedQueue.remove(this.getName());
        infinityTaskRunThread.threadStatusMap.put(this.getName(), false);

    }
}