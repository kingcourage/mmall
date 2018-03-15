package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserServce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @author wcy
 * 2018/1/17
 */
@RestController
@RequestMapping("/manage/user/")
public class UserManageController {

    @Autowired
    private IUserServce iUserServce;

    @RequestMapping("login.do")
    public ServerResponse<User> login(String username, String password, HttpSession session){
        username = "admin";
        password = "123456";
        ServerResponse<User>  response = iUserServce.login(username,password);
        if(response.isSuccess()){
            User user = response.getData();
            if(user.getRole() == Const.Role.ROLE_ADMIN){
                //说明是管理员
                session.setAttribute(Const.CURRENT_USER,user);
                return response;
            }else{
                return ServerResponse.createByErrorMessage("不是管理员 无法登陆");
            }
        }
        return response;
    }
}
