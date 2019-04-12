package com.lmm.pojo;

import java.util.Date;
import javax.persistence.*;

@Table(name = "friends_request")
public class FriendsRequest {
    @Id
    private String id;

    /**
     * 发送者Id
     */
    @Column(name = "send_user_id")
    private String sendUserId;

    @Column(name = "accept_user_id")
    private String acceptUserId;

    @Column(name = "request_date_time")
    private Date requestDateTime;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取发送者Id
     *
     * @return send_user_id - 发送者Id
     */
    public String getSendUserId() {
        return sendUserId;
    }

    /**
     * 设置发送者Id
     *
     * @param sendUserId 发送者Id
     */
    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    /**
     * @return accept_user_id
     */
    public String getAcceptUserId() {
        return acceptUserId;
    }

    /**
     * @param acceptUserId
     */
    public void setAcceptUserId(String acceptUserId) {
        this.acceptUserId = acceptUserId;
    }

    /**
     * @return request_date_time
     */
    public Date getRequestDateTime() {
        return requestDateTime;
    }

    /**
     * @param requestDateTime
     */
    public void setRequestDateTime(Date requestDateTime) {
        this.requestDateTime = requestDateTime;
    }
}