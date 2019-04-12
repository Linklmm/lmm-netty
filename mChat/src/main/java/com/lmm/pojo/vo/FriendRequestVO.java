package com.lmm.pojo.vo;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * 好友请求发送方的信息
 */
public class FriendRequestVO {
    @Id
    private String sendUserId;

    private String sendUsername;

    @Column(name = "face_image")
    private String sendFaceImage;

    private String sendNickname;

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUsername() {
        return sendUsername;
    }

    public void setSendUsername(String sendUsername) {
        this.sendUsername = sendUsername;
    }

    public String getSendFaceImage() {
        return sendFaceImage;
    }

    public void setSendFaceImage(String sendFaceImage) {
        this.sendFaceImage = sendFaceImage;
    }

    public String getSendNickname() {
        return sendNickname;
    }

    public void setSendNickname(String sendNickname) {
        this.sendNickname = sendNickname;
    }
}