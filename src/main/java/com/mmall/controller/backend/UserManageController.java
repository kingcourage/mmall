package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserServce;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest){
        username = "admin";
        password = "123456";
        ServerResponse<User>  response = iUserServce.login(username,password);
        if(response.isSuccess()){
            User user = response.getData();
            if(user.getRole() == Const.Role.ROLE_ADMIN){
                //说明是管理员
                //session.setAttribute(Const.CURRENT_USER,user);
                CookieUtil.writeLoginToken(httpServletResponse,session.getId());
                CookieUtil.readLoginToken(httpServletRequest);
                CookieUtil.delLoginToken(httpServletRequest,httpServletResponse);
                RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_TIME);
                return response;
            }else{
                return ServerResponse.createByErrorMessage("不是管理员 无法登陆");
            }
        }
        return response;
    }
}
