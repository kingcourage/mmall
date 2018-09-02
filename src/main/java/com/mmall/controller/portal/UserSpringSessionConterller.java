package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserServce;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author wcy
 * 2018/1/11
 */
@RestController
@RequestMapping("/user/springsession/")
public class UserSpringSessionConterller {

    @Autowired
    private IUserServce iUserServce;

    @RequestMapping(value ="login.do", method = RequestMethod.GET)
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){
        ServerResponse<User> response = iUserServce.login(username,password);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
            return response;
        }
        return response;
    }

    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
    public ServerResponse<String> logout(HttpSession session,HttpServletRequest request,HttpServletResponse response){
       session.removeAttribute(Const.CURRENT_USER);
       return ServerResponse.createBySuccess();
    }


    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    public ServerResponse<User> getUserInfo(HttpSession session, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
    }


}
