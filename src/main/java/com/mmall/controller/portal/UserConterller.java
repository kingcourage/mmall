package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserServce;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author wcy
 * 2018/1/11
 */
@RestController
@RequestMapping("/user/")
public class UserConterller {

    @Autowired
    private IUserServce iUserServce;

    @RequestMapping(value ="login.do", method = RequestMethod.GET)
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){
        ServerResponse<User> response = iUserServce.login(username,password);
        if(response.isSuccess()){
           // session.setAttribute(Const.CURRENT_USER,response.getData());
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_TIME);
            return response;
        }
        return response;
    }

    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
    public ServerResponse<String> logout(HttpSession session,HttpServletRequest request,HttpServletResponse response){
       // session.removeAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        CookieUtil.delLoginToken(request,response);
        RedisPoolUtil.del(loginToken);
        return ServerResponse.createBySuccess();
    }


    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    public ServerResponse<String> register(User user){
        return iUserServce.register(user);
    }

    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    public ServerResponse<String> checkValid(String str,String type){
        return  iUserServce.checkValid(str,type);
    }

    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    public ServerResponse<User> getUserInfo(HttpSession session, HttpServletRequest request){
        //User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken  =CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
    }

    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    public ServerResponse<String> forgetGetQuestion(String username){
        return  iUserServce.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return  iUserServce.checkAnswer(username,question,answer);
    }


    @RequestMapping(value = "forget_reset_password,do",method = RequestMethod.POST)
    public ServerResponse<String> forgetRestPassword(String username,String password,String forgetToken){
        return iUserServce.forgetRestPassword(username,password,forgetToken);
    }


    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
       return iUserServce.resetPassword(passwordOld,passwordNew,user);
    }

    @RequestMapping(value="update_information.do")
    public ServerResponse<User> updateInformation(HttpSession session,User user){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserServce.updateInformation(user);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value = "get_information.do")
    public ServerResponse<User> getInformation(HttpSession session){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null ){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        return iUserServce.getInformation(currentUser.getId());
    }
}
