package com.lmm.service.Impl;

import com.lmm.enums.SearchFriendsStatusEnum;
import com.lmm.mapper.FriendsRequestMapper;
import com.lmm.mapper.MyFriendsMapper;
import com.lmm.mapper.UsersMapper;
import com.lmm.mapper.UsersMapperCustom;
import com.lmm.pojo.FriendsRequest;
import com.lmm.pojo.MyFriends;
import com.lmm.pojo.Users;
import com.lmm.pojo.vo.FriendRequestVO;
import com.lmm.pojo.vo.MyFriendsVO;
import com.lmm.service.UserService;
import com.lmm.utils.FastDFSClient;
import com.lmm.utils.FileUtils;
import com.lmm.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private QRCodeUtils qrCodeUtils;
    @Autowired
    private FastDFSClient fastDFSClient;
    @Autowired
    private MyFriendsMapper myFriendsMapper;
    @Autowired
    private FriendsRequestMapper friendsRequestMapper;
    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);
        Users result = usersMapper.selectOne(user);


        return result != null ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {

        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", pwd);

        Users result = usersMapper.selectOneByExample(userExample);

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) {

        String userId = sid.nextShort();
        //为每个用户生成唯一的二维码,加密
        //mchat_qrcode:[username]

        String qrCodePath = "F://user" + userId + "qrcode.png";
        //二维码信息为 mchat_qrcode：user.getUsername()
        qrCodeUtils.createQRCode(qrCodePath, "mchat_qrcode:" + user.getUsername());
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);

        String qrCodeUrl = "";
        try {
            qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        user.setQrcode(qrCodeUrl);
        user.setId(userId);

        usersMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        usersMapper.updateByPrimaryKeySelective(user);
        return queryUserById(user.getId());
    }


    //    @Transactional(propagation = Propagation.SUPPORTS)
    private Users queryUserById(String id) {
        return usersMapper.selectByPrimaryKey(id);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {
        Users user = queryUserInfoByUsername(friendUserName);

        //1.搜索的用户如果不存在，返回【无此用户】

        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
//        2.搜索账号是用户自己，返回【不能添加自己】
        if (user.getId().equals(myUserId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

//        3.搜索的账号用户已添加好友，返回【该用户已经是你的好友】
        Example friend = new Example(MyFriends.class);
        Example.Criteria mfriend = friend.createCriteria();
        mfriend.andEqualTo("myUserId", myUserId);
        mfriend.andEqualTo("myFriendUserId", user.getId());
        MyFriends myFriendsRel = myFriendsMapper.selectOneByExample(friend);

        if (myFriendsRel != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUsername(String username) {
        Example user = new Example(Users.class);
        Example.Criteria uc = user.createCriteria();
        uc.andEqualTo("username", username);
        return usersMapper.selectOneByExample(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

        //根据用户名把朋友信息查询出来
        Users friend = queryUserInfoByUsername(friendUsername);

        //1.查询发送好友请求记录表
        Example user = new Example(FriendsRequest.class);
        Example.Criteria uc = user.createCriteria();
        uc.andEqualTo("sendUserId", myUserId);
        uc.andEqualTo("acceptUserId", friend.getId());
        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(user);

        if (friendsRequest == null) {
            //2.如果不是你的好友，并且好友记录没有添加，则新增好友记录
            String requestId = sid.nextShort();

            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }

    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void delFriendRequest(String sendUserId, String acceptUserId) {
        Example friend = new Example(FriendsRequest.class);
        Example.Criteria mfriend = friend.createCriteria();
        mfriend.andEqualTo("sendUserId", sendUserId);
        mfriend.andEqualTo("acceptUserId", acceptUserId);

        friendsRequestMapper.deleteByExample(friend);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId, sendUserId);
        delFriendRequest(sendUserId, acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void saveFriends(String sendUserId, String acceptUserId) {

        MyFriends myFriends = new MyFriends();

        String recordId = sid.nextShort();

        myFriends.setId(recordId);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {

        return usersMapperCustom.queryMyFriends(userId);
    }
}
