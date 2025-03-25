package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserServce;
import com.mmall.util.MD5Util;
import com.mmall.util.PasswordHasher;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author wcy
 * 2018/1/11
 */
@Service("iUserService")
public class UserServiceImpl implements IUserServce {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        // Handle legacy MD5 passwords
        if (PasswordHasher.isLegacyHash(user.getPassword())) {
            // Verify using old MD5 method
            String md5Password = MD5Util.MD5EncodeUtf8(password);
            if (!md5Password.equals(user.getPassword())) {
                return ServerResponse.createByErrorMessage("密码错误");
            }
            // Upgrade to new hash if old password matches
            String newHash = PasswordHasher.hashPassword(password);
            user.setPassword(newHash);
            userMapper.updateByPrimaryKeySelective(user);
        } else {
            // Verify using new secure method
            if (!PasswordHasher.verifyPassword(password, user.getPassword())) {
                return ServerResponse.createByErrorMessage("密码错误");
            }
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        // Hash password using secure PBKDF2
        user.setPassword(PasswordHasher.hashPassword(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email已存在");
                }

            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question  = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount =  userMapper.checkAnswer(username,question,answer);
        if(resultCount >0){
            //说明问题及答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        if(StringUtils.equals(forgetToken,token)){
            String securePassword = PasswordHasher.hashPassword(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, securePassword);

            if(rowCount >0){
                return ServerResponse.createBySuccess("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        // Get user's current stored password
        User currentUser = userMapper.selectByPrimaryKey(user.getId());
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        // Handle verification based on hash type
        boolean passwordValid;
        if (PasswordHasher.isLegacyHash(currentUser.getPassword())) {
            // Legacy MD5 verification
            String oldMd5Password = MD5Util.MD5EncodeUtf8(passwordOld);
            passwordValid = oldMd5Password.equals(currentUser.getPassword());
        } else {
            // New secure hash verification
            passwordValid = PasswordHasher.verifyPassword(passwordOld, currentUser.getPassword());
        }

        if (!passwordValid) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        // Update to new secure password hash
        String newSecurePassword = PasswordHasher.hashPassword(passwordNew);
        int updateCount = userMapper.updatePasswordByUsername(currentUser.getUsername(), newSecurePassword);
        
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        int resultCount  = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在，请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer id) {
        User user  = userMapper.selectByPrimaryKey(id);
        if(user != null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }else{
            return ServerResponse.createByError();
        }
    }

}
