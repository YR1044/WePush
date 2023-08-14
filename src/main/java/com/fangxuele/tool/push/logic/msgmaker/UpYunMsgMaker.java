package com.fangxuele.tool.push.logic.msgmaker;

import com.alibaba.fastjson.JSON;
import com.fangxuele.tool.push.bean.TemplateData;
import com.fangxuele.tool.push.domain.TMsg;
import com.fangxuele.tool.push.domain.TMsgSms;
import com.fangxuele.tool.push.ui.form.msg.UpYunMsgForm;
import com.fangxuele.tool.push.util.TemplateUtil;
import lombok.Getter;
import org.apache.commons.compress.utils.Lists;
import org.apache.velocity.VelocityContext;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 又拍云模板短信加工器
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">Zhou Bo</a>
 * @since 2019/6/14.
 */
@Getter
public class UpYunMsgMaker extends BaseMsgMaker implements IMsgMaker {

    private String templateId;

    private List<String> paramList;

    public UpYunMsgMaker(TMsg tMsg) {
        TMsgSms tMsgSms = JSON.parseObject(tMsg.getContent(), TMsgSms.class);
        this.templateId = tMsgSms.getTemplateId();

        paramList = new ArrayList<>();
        for (TemplateData templateData : tMsgSms.getTemplateDataList()) {
            paramList.add(templateData.getValue());
        }
    }

    /**
     * 准备(界面字段等)
     */
    @Override
    public void prepare() {
        templateId = UpYunMsgForm.getInstance().getMsgTemplateIdTextField().getText();

        if (UpYunMsgForm.getInstance().getTemplateMsgDataTable().getModel().getRowCount() == 0) {
            UpYunMsgForm.initTemplateDataTable();
        }

        DefaultTableModel tableModel = (DefaultTableModel) UpYunMsgForm.getInstance().getTemplateMsgDataTable().getModel();
        int rowCount = tableModel.getRowCount();
        paramList = Lists.newArrayList();
        for (int i = 0; i < rowCount; i++) {
            String value = ((String) tableModel.getValueAt(i, 1));
            paramList.add(value);
        }
    }

    /**
     * 组织又拍云短信消息
     *
     * @param msgData 消息信息
     * @return String[]
     */
    @Override
    public String[] makeMsg(String[] msgData) {

        VelocityContext velocityContext = getVelocityContext(msgData);
        for (int i = 0; i < paramList.size(); i++) {
            paramList.set(i, TemplateUtil.evaluate(paramList.get(i), velocityContext));
        }
        String[] paramArray = new String[paramList.size()];
        return paramList.toArray(paramArray);
    }
}
