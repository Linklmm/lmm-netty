package com.lmm.netty;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 用户id和channel的关联关系处理
 */
public class UserChannelRel {
    private static HashMap<String , Channel> manager = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(UserChannelRel.class);

    public static void put(String senderId,Channel channel){
        manager.put(senderId,channel);
    }

    public static Channel get(String senderId){
        return manager.get(senderId);
    }

    public static void output(){
        for (HashMap.Entry<String,Channel> entry :manager.entrySet()){
            LOGGER.info("UserChannelRel 的 output:userId:{},channelId:{}",entry.getKey(),entry.getValue().id().asLongText());
        }
    }
}
