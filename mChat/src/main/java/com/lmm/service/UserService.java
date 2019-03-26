package com.lmm.service;
import com.lmm.pojo.Users;

public interface UserService {
    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    public boolean queryUsernameIsExist(String username);

    /**
     * 用户登录
     * @param username
     * @param pwd
     * @return
     */
    public Users queryUserForLogin(String username, String pwd);


    /**
     * 用户注册
     * @param user
     * @return
     */
    public Users saveUser(Users user);
}
