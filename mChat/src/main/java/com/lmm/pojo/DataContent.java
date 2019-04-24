package com.lmm.pojo;

import com.lmm.netty.ChatMsg;

import java.io.Serializable;

public class DataContent implements Serializable {


    private static final long serialVersionUID = -2713485838373056975L;

    //动作类型
    private Integer action;
    //用户的聊天内容entity
    private ChatMsg chatMsg;
    // 扩展字段
    private String extand;

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsg getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMsg chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtand() {
        return extand;
    }

    public void setExtand(String extand) {
        this.extand = extand;
    }

    @Override
    public String toString() {
        return "DataContent{" +
                "action=" + action +
                ", chatMsg=" + chatMsg +
                ", extand='" + extand + '\'' +
                '}';
    }
}
