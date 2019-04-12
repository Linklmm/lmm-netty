package com.lmm.controller;

//import com.lmm.mChat.pojo.Users;
//import com.lmm.mChat.pojo.vo.UsersVO;

import com.lmm.enums.OperatorFriendRequestTypeEnum;
import com.lmm.enums.SearchFriendsStatusEnum;
import com.lmm.pojo.Users;
import com.lmm.pojo.bo.UsersBO;
import com.lmm.pojo.vo.MyFriendsVO;
import com.lmm.pojo.vo.UsersVO;
import com.lmm.service.UserService;
import com.lmm.utils.FastDFSClient;
import com.lmm.utils.FileUtils;
import com.lmm.utils.IMoocJSONResult;
import com.lmm.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("u")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private FastDFSClient fastDFSClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    /**
     * 登录注册接口
     *
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("/registerOrLogin")
    public IMoocJSONResult registerOrLogin(@RequestBody Users user) throws Exception {
        LOGGER.info("用户登录或注册入参Users:{}", user.toString());
        //判断用户名和密码不能为空
        if (StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())) {
            LOGGER.error("用户名或密码不能为空....");
            return IMoocJSONResult.errorMsg("用户名或密码不能为空....");
        }
        //1.判断用户名是否存在，如果存在就登录，如果不存在则登录
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
        Users userResult = null;
        if (usernameIsExist) {
            //1.1登录
            userResult = userService.queryUserForLogin(user.getUsername(),
                    MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return IMoocJSONResult.errorMap("用户名或密码不正确!");
            }
        } else {
            //1.2注册
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));

            userResult = userService.saveUser(user);
        }

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult, usersVO);

        LOGGER.info("登录或注册接口的入参为usersVO:{}", usersVO);
        return IMoocJSONResult.ok(usersVO);
    }

    @PostMapping("/uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) throws Exception {
        LOGGER.info("上传头像的对象usersBo:{}", usersBO);
        //获取前端传过来的base64字符串，然后转换为文件对象再上传
        String base64Data = usersBO.getFaceData();
        String userFacePath = "F:\\" + usersBO.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath, base64Data);

        //上传文件到fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        LOGGER.error("头像url：{}", url);

        // 获取缩略图的url
        String thump = "_80x80.";
        String arr[] = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];
        LOGGER.error("缩略图imgURL:{}", thumpImgUrl);

        //更新用户头像
        Users user = new Users();
        user.setId(usersBO.getUserId());
        //头像缩略图
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);

        Users result = userService.updateUserInfo(user);

        LOGGER.info("上传头像的出参 result:{}", result);
        return IMoocJSONResult.ok(result);
    }

    /**
     * 设置用户昵称
     *
     * @param usersBO
     * @return
     * @throws Exception
     */
    @PostMapping("/setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBO usersBO) throws Exception {
        LOGGER.info("设置用户昵称的入参usersBO:{}", usersBO);
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setNickname(usersBO.getNickname());

        Users result = userService.updateUserInfo(user);

        LOGGER.info("设置用户昵称的出参result:{}", result);
        return IMoocJSONResult.ok(result);
    }

    /**
     * 搜索好友接口,根据账号做匹配查询，不是模糊查询
     *
     * @param myUserId
     * @param friendUserName
     * @return
     * @throws Exception
     */
    @PostMapping("/search")
    public IMoocJSONResult searchUser(String myUserId, String friendUserName) throws Exception {
        LOGGER.error("搜索好友的入参myUserId:{},friendUserName:{}", myUserId, friendUserName);
        //判断入参不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUserName)) {
            LOGGER.error("搜索好友入参不能为空，myUserId:{}，friendUserName:{}", myUserId, friendUserName);
            return IMoocJSONResult.errorMsg("");

        }

        //前置条件，1.搜索的用户如果不存在，返回【无此用户】
        //前置条件，2.搜索账号是用户自己，返回【不能添加自己】
        //前置条件，3.搜索的账号用户已添加好友，返回【该用户已经是你的好友】
        Integer status = userService.preconditionSearchFriends(myUserId, friendUserName);

        if (status == SearchFriendsStatusEnum.SUCCESS.getStatus()) {
            Users user = userService.queryUserInfoByUsername(friendUserName);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(user, usersVO);
            LOGGER.error("搜索好友的出参为usersVO:{}", usersVO.toString());
            return IMoocJSONResult.ok(usersVO);
        } else {
            String erroMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            LOGGER.error("搜索好友错误信息：erroMsg:{}", erroMsg);
            return IMoocJSONResult.errorMsg(erroMsg);
        }

    }

    /**
     * 发送添加好友的请求
     *
     * @param myUserId
     * @param friendUserName
     * @return
     * @throws Exception
     */
    @PostMapping("/addFriendRequest")
    public IMoocJSONResult addFriendRequest(String myUserId, String friendUserName) throws Exception {
        LOGGER.error("发送添加好友的请求的入参myUserId:{},friendUserName:{}", myUserId, friendUserName);
        //判断入参不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUserName)) {
            LOGGER.error("发送添加好友的请求的入参不能为空，myUserId:{}，friendUserName:{}", myUserId, friendUserName);
            return IMoocJSONResult.errorMsg("");

        }

        //前置条件，1.搜索的用户如果不存在，返回【无此用户】
        //前置条件，2.搜索账号是用户自己，返回【不能添加自己】
        //前置条件，3.搜索的账号用户已添加好友，返回【该用户已经是你的好友】
        Integer status = userService.preconditionSearchFriends(myUserId, friendUserName);

        if (status == SearchFriendsStatusEnum.SUCCESS.getStatus()) {
            userService.sendFriendRequest(myUserId, friendUserName);
        } else {
            String erroMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            LOGGER.error("发送添加好友的请求错误信息：erroMsg:{}", erroMsg);
            return IMoocJSONResult.errorMsg(erroMsg);
        }
        return IMoocJSONResult.ok();
    }

    @PostMapping("/queryFriendRequestList")
    public IMoocJSONResult queryFriendRequestList(String acceptUserId) throws Exception {
        //判断入参不能为空
        if (StringUtils.isBlank(acceptUserId)) {
            LOGGER.error("查询用户接受到的好友申请的入参不能为空，acceptUserId:{}", acceptUserId);
            return IMoocJSONResult.errorMsg("");

        }

        //1.查询用户接受到的好友申请
        return IMoocJSONResult.ok(userService.queryFriendRequestList(acceptUserId));
    }

    /**
     * 用户是否通过好友请求
     *
     * @param acceptUserId 是否同意通过好友请求的用户
     * @param sendUserId   发送好友请求用户
     * @param operType
     * @return
     */
    @PostMapping("/operatorFriendRequest")
    public IMoocJSONResult operatorFriendRequest(String acceptUserId, String sendUserId,
                                                 Integer operType) {

        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null) {
            LOGGER.error("参数不能为空 入参 acceptUserId:{},sendUserId:{},operType:{}"
                    , acceptUserId, sendUserId, operType);
            return IMoocJSONResult.errorMsg("");
        }

        //如果operType没有对应的枚举值，则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return IMoocJSONResult.errorMsg("");
        }

        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            //2.判断好友请求如果是忽略，则直接删除好友请求的数据库记录
            LOGGER.info("用户忽略好友请求");
            userService.delFriendRequest(sendUserId, acceptUserId);

        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type) {
            //3.判断好友请求是否通过，通过则数据库添加好友记录
            //删除好友请求的数据库记录
            LOGGER.info("用户通过好友请求");
            userService.passFriendRequest(sendUserId, acceptUserId);
        }

        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
        return IMoocJSONResult.ok(myFriends);
    }

    /**
     * 查询用户好友列表
     * @param userId
     * @return
     */
    @PostMapping("/myFriends")
    public IMoocJSONResult myFriends(String userId) {
        //判断userId不能为空
        LOGGER.info("查询用户好友列表的入参userId:{}",userId);
        if (StringUtils.isBlank(userId)){
            LOGGER.error("用户ID不能为空！ userId:{}",userId);
            return IMoocJSONResult.errorMsg("用户id不能为空！");
        }

        //1.查询用户好友列表

        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);

        return IMoocJSONResult.ok(myFriends);
    }
}
