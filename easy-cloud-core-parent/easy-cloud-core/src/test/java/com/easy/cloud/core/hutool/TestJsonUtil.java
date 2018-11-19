package com.easy.cloud.core.hutool;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.level.Level;
import com.alibaba.fastjson.JSONObject;
import com.easy.cloud.core.basic.constant.error.EcBaseErrorCodeEnum;
import com.easy.cloud.core.common.json.utils.EcJSONUtils;
import com.easy.cloud.core.common.log.utils.EcLogUtils;
import com.easy.cloud.core.exception.bo.EcBaseBusinessException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author daiqi
 * @create 2018-11-19 15:14
 */
public class TestJsonUtil {
    private static Log logger = LogFactory.get();
    private Logger logger1 = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testEfficiency() {
        User user = new User("zhangsan");
        user.add("lisi").add("wangwu").add("zhaojiaren");
        user.add("lisi").add("wangwu").add("zhaojiaren");
        user.add("lisi").add("wangwu").add("zhaojiaren");
        user.addusers("zhangsan", "1234556");
        user.addusers("zhangsan1", "12345562");
        user.addusers("zhangsan2", "12345563");
//        for (int k = 0 ; k < 20; ++k) {
//            long startTime = DateUtil.current(false);
//            for (int i = 0 ; i < 500000; ++i) {
//                JSONUtil.parseObj(user).getClass();
//            }
//            long endTime = DateUtil.current(false);
//            System.out.println("第" + k + "次序列化所用的时间为：" + (endTime - startTime) + "ms");
//        }

//        String jsonStr = JSONUtil.toJsonStr(user);
    }

    @Test
    public void testFastEfficiency() {
        User user = new User("zhangsan");
        user.add("lisi").add("wangwu").add("zhaojiaren");
        user.add("lisi").add("wangwu").add("zhaojiaren");
        user.add("lisi").add("wangwu").add("zhaojiaren");
        user.addusers("zhangsan", "1234556");
        user.addusers("zhangsan1", "12345562");
        user.addusers("zhangsan2", "12345563");
        System.out.println(EcJSONUtils.parseObject(1, String.class));
        for (int k = 0; k < 20; ++k) {
            long startTime = DateUtil.current(false);
            for (int i = 0; i < 500000; ++i) {
                String json = JSONObject.toJSONString(user);
                EcJSONUtils.parseObject(json, User.class).getClass();
            }
            long endTime = DateUtil.current(false);
            System.out.println("第" + k + "次序列化所用的时间为：" + (endTime - startTime) + "ms");
        }
    }

    @Test
    public void testProps() {
        try {
            throw new EcBaseBusinessException(EcBaseErrorCodeEnum.DATA_ERROR);
        } catch (Exception e) {
            EcLogUtils.error("错误", e, logger1);;
            if (logger.isEnabled(Level.INFO)) {
                logger.info(e, e.getMessage());
            }
        }

    }

    public static class User {
        private String userName;
        private String password;
        private List<String> nickNames;
        private List<User> users;

        public User() {

        }

        public User(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getNickNames() {
            return nickNames;
        }

        public void setNickNames(List<String> nickNames) {
            this.nickNames = nickNames;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }

        public User addusers(String userName, String password) {
            if (this.users == null) {
                this.users = new ArrayList<>();
            }
            User user = new User(userName);
            user.setPassword("password");
            this.users.add(user);
            return this;
        }

        public User add(String nickName) {
            if (this.nickNames == null) {
                this.nickNames = new ArrayList<>();
            }
            this.nickNames.add(nickName);
            return this;
        }


    }
}
