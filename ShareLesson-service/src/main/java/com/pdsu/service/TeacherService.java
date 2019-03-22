package com.pdsu.service;

import com.pdsu.pojo.User;

import java.util.List;

/**
 * @Auther: http://wangjie
 * @Date: 2019/3/19
 * @Description: com.pdsu.service
 * @version: 1.0
 */
public interface TeacherService {

    /**
     * 老师查看自己的粉丝
     * @param tid
     * @return
     */
    List<User> selectFansByTid(String tid);
}
