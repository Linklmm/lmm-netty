package com.lmm.mapper;

import com.lmm.pojo.Users;
import com.lmm.pojo.vo.FriendRequestVO;
import com.lmm.pojo.vo.MyFriendsVO;
import com.lmm.utils.MyMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UsersMapperCustom extends MyMapper<Users> {
    public List<FriendRequestVO> queryFriendRequestList (String acceptUserId);

    public List<MyFriendsVO> queryMyFriends(String userId);

    public void batchUpdateMsgSinged(List<String> msgIdList);
}