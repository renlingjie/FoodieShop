package com.rlj.controller;

import com.rlj.pojo.Users;
import com.rlj.pojo.vo.UsersVO;
import com.rlj.service.UserService;
import com.rlj.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

//该注解让返回的所有请求都是json对象
@Controller
public class SSOController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisOperator redisOperator;
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_TICKET = "redis_user_ticket";
    public static final String REDIS_TMP_TICKET = "redis_tmp_ticket";
    public static final String COOKIE_USER_TICKET = "cookie_user_ticket";


    @ResponseBody
    @GetMapping("/hello")
    public Object hello(){
        return "hello world~";
    }
    //返回的是一个路径，所以不加注解@ResponseBody
    @GetMapping("/login")
    public String login(String returnUrl, Model model, HttpServletRequest request, HttpServletResponse response){
        model.addAttribute("returnUrl",returnUrl);
        // 获取userTicket门票，如果cookie中能获取到，证明用户登录过，此时直接创建临时门票
        String userTicket = getCookie(request,COOKIE_USER_TICKET);
        boolean isVerified = verifyUserTicket(userTicket);
        if (isVerified){
            //验证成功，就不需要返回login页面，直接创建临时票据返回
            String tmpTicket = createTmpTicket();
            return "redirect:"+returnUrl+"?tmpTicket="+tmpTicket;
        }
        //认证失败，说明用户从未登录过，跳转到CAS的统一登录页面
        return "login";
    }

    private boolean verifyUserTicket(String userTicket){
        //1、验证CAS的cookie中的value不能为空
        if (StringUtils.isBlank(userTicket)){
            return false;
        }
        //2、验证该vlaue是否有效（能不能根据这个为key拿到全局门票）
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)){
            return false;
        }
        //3、验证全局门票对应的user会话是否存在
        String userRedis = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userRedis)){
            return false;
        }
        return true;
    }

    @PostMapping("/doLogin")//登录页面的form表单中的action是doLogin，所以提交后的请求的路由就是doLogin，在这里一定要保持一致
    public String doLogin(String username,String password,String returnUrl,Model model,
                          HttpServletRequest request, HttpServletResponse response)throws Exception{
        model.addAttribute("returnUrl",returnUrl);
        //判断用户名和密码不能为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            //失败我们要返回错误信息，之前是return IMOOCJSONResult.errorMsg，但现在因为要根据情况跳转至不同页面，故类型是String
            //而不是Result，但是我们在前端设置了一个<span style="color: red" th:text="${errmsg}"></span>，所以用域
            model.addAttribute("errmsg","用户名或密码不能为空");
            return "login";
        }
        //1、实现登录(需要将将结果加密)
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));
        if (userResult == null){//说明根据用户名密码在数据库查询不到这样的一个Users，返回的肯定是一个空集
            model.addAttribute("errmsg","用户名或密码不正确");
            return "login";
        }
        //2、登录成功之后，是需要创建用户的Redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        redisOperator.set(REDIS_USER_TOKEN+":"+userResult.getId(),JsonUtils.objectToJson(usersVO));

        //3、生成ticket全局门票、临时门票，代表用户在CAS端登录过
        String userTicket = UUID.randomUUID().toString().trim();
        //3.1、之后还要将全局门票放到CAS端的Cookie中，因为下次某个功能模块再去验证登录，首先是要看对应CAS端的Cookie中是
        //否有门票，只有有了，才会继续在Redis中验证全局门票是否正确。否则如果在Cookie中都没有，则可以直接创建全局门票了
        setCookie(COOKIE_USER_TICKET,userTicket,response);
        //3.2、userTicket是需要关联用户ID，并放入到Redis中，从而能标识该用户有门票了，可以在各个功能模块中的登录认证中通过
        redisOperator.set(REDIS_USER_TICKET+":"+userTicket,userResult.getId());

        //3.3、生成临时票据，回跳到调用端网站，是由CAS端所签发的一个一次性的临时ticket
        String tmpTicket = createTmpTicket();
        //携带临时票据一同返回给之前的功能模块
        return "redirect:"+returnUrl+"?tmpTicket="+tmpTicket;
        //return "login"; //如果测试的时候可以用这个，先跳转到这个CAS登录页面，我们可以检测上面的Cookie是否已生成
    }
    //创建临时票据(除了创建，因为还要携带该临时票据回跳到某个功能模块，所以我们将它保存到Redis中)
    private String createTmpTicket(){
        String tmpTicket = UUID.randomUUID().toString().trim();
        //为了区分各个临时票据，我们就直接将上面的临时票据拼接 后作为名字，同时加密后作为值(同时设置600s的过期时间)
        try {
            redisOperator.set(REDIS_TMP_TICKET+":"+tmpTicket,MD5Utils.getMD5Str(tmpTicket),600);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpTicket;
    }

    //在CAS端创建全局门票的Cookie（这个实际上是为了不同域名之间的）cookie跨域的时候，两个不同域名之间的cookie想要进行共享，那么我们来一个专门存放cookie
    //的公共网站，当我们都没有检测到cookie的时候，我们将cookie的验证跳转到这个公共的网站上进行验证，这个时候，实际上就实现了cood 跨域
    //所以这个设置的一定是公共所用的那个域名
    private void setCookie(String key,String value,HttpServletResponse response){
        Cookie cookie = new Cookie(key,value);
        cookie.setDomain("sso.com");//设置CAS端的Cookie，所以使用CAS端的域名（www.sso.com:8090/login）
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    //通过临时票据认证成功后，需要获取已经存入到CAS端的Cookie
    private String getCookie(HttpServletRequest request,String key){
        //获取的思路就是，因为Cookie可能有很多，我们要获取到那个和我们传入名称相匹配的那个
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || StringUtils.isBlank(key)){
            return null;
        }
        String cookieValue = null;
        for (int i = 0;i < cookieList.length;i ++){
            if (cookieList[i].getName().equals(key)){
                cookieValue = cookieList[i].getValue();
                break;
            }
        }
        return cookieValue;
    }

    @PostMapping("/verifyTmpTicket")
    @ResponseBody//这次我们返回的不再是一个String类型的页面的数据，而是用户的会话信息，所以是要@ResponseBody
    public IMOOCJSONResult verifyTmpTicket(String tmpTicket,
                          HttpServletRequest request, HttpServletResponse response)throws Exception{
        //使用一次性临时票据来验证用户是否登录，如果登录过，把用户会话信息返回给站点。使用完毕后，需要销毁临时票据
        String tmpTicketValue = redisOperator.get(REDIS_TMP_TICKET+":"+tmpTicket);
        if (StringUtils.isBlank(tmpTicketValue)){
            return IMOOCJSONResult.errorUserTicket("用户临时票据获取值为空异常");
        }
        //如果临时票据没有问题，则需要销毁，并拿到CAS端cookie的全局userTicket，以此再获取用户会话
        if (!tmpTicketValue.equals(MD5Utils.getMD5Str(tmpTicket))){
            return IMOOCJSONResult.errorUserTicket("用户临时票据不相等异常");
        }else {
            //1、临时票据校验成功，销毁之
            redisOperator.del(REDIS_TMP_TICKET+":"+tmpTicket);
        }
        //2、此时我们通过临时票据认证已经成功，接下来就要拿到用户会话
        //2.1、因为全局门票Redis的值是我们用户会话Redis的键名的一部分。所以要拿到全局门票。那么先先拿到存储在Cookie中的全局门票的Redis的键名
        String userTicket = getCookie(request,COOKIE_USER_TICKET);
        //2.2、根据全局门票的键名的一部分userTicket拼接得到全局门票的完整键名
        String userId = redisOperator.get(REDIS_USER_TICKET+":"+userTicket);
        if (StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorUserTicket("用户全局门票获取值为空异常");
        }
        //2.3、全局门票的Redis中的值userId是用户会话Redis的键名的一部分，拼接得到会话完整键名，拿到Redis中的会话
        String userRedis = redisOperator.get(REDIS_USER_TOKEN+":"+userId);
        if (StringUtils.isBlank(userRedis)){
            return IMOOCJSONResult.errorUserTicket("用户会话异常");
        }
        //3、验证成功，携带用户会话返回
        return IMOOCJSONResult.ok(JsonUtils.jsonToPojo(userRedis,UsersVO.class));
    }

    @PostMapping("/logout")
    @ResponseBody//这次我们返回的不再是一个String类型的页面的数据，而是用户的会话信息，所以是要@ResponseBody
    public IMOOCJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response)throws Exception{
        //1、获取userTicket门票，如果cookie中能获取到，证明用户登录过
        String userTicket = getCookie(request,COOKIE_USER_TICKET);
        //2、清除userTicket对应的票据（包括前端的cookie以及Redis中的全局门票）
        deleteCookie(COOKIE_USER_TICKET,response);
        redisOperator.del(REDIS_USER_TICKET+":"+userTicket);
        //3、清除用户全局会话
        redisOperator.del(REDIS_USER_TOKEN+":"+userId);
        return IMOOCJSONResult.ok();
    }
    private void deleteCookie(String key,HttpServletResponse response){
        Cookie cookie = new Cookie(key,null);
        cookie.setDomain("sso.com");//设置CAS端的Cookie，所以使用CAS端的域名（www.sso.com:8090/login）
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }
}
