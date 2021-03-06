package com.pdsu.web.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pdsu.mypojo.Result;
import com.pdsu.pojo.Lesson;
import com.pdsu.pojo.User;
import com.pdsu.service.CenterService;
import com.pdsu.service.ImageService;
import com.pdsu.service.LessonService;
import com.pdsu.web.base.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Auther: http://wangjie
 * @Date: 2019/3/17
 * @Description: com.pdsu.web.controller
 * @version: 1.0
 */
@Controller
@Api(tags = {"3,课程信息 相关接口"})
@RequestMapping("/lesson")
public class LessonController extends BaseController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private LessonService lessonServiceImpl;

    @Autowired
    private CenterService centerServiceImpl;

    /**
     * 添加课程时上传图片
     *
     * @param imgFile
     * @return
     */
    @ApiOperation(value = "上传图片接口")
    @RequestMapping(value = "/picUpload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> picUpload(MultipartFile imgFile) {
        Result result = new Result();
        try {
            return imageService.upload(imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据分类id查询课程
     * 分页查询
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, dataType = "String", paramType = "query", value = "分类id"),
            @ApiImplicitParam(name = "pn", required = true, dataType = "int", paramType = "query", value = "页码"),
            @ApiImplicitParam(name = "isCharge", required = true, dataType = "int", paramType = "query", value = "是否收费")
    })
    @ApiOperation(value = "根据分类id查询课程")
    @ResponseBody
    @RequestMapping(value = "/selectLessonByClassify.do", method = RequestMethod.GET)
    public Result selectLessonByClassify(String id, int isCharge
            , @RequestParam(value = "pn", defaultValue = "1") Integer pn) {
        //在查询之前需要调用，传入页码，以及每页大小
        PageHelper.startPage(pn, 5);
        //后面紧跟的这个查询就是分页查询
        List<Lesson> lessonList = centerServiceImpl.selectLessonByClassifyId(id, isCharge);
        // 封装了详细的分页信息,包括有我们查询出来的数据，传入连续显示的页数
        PageInfo page = new PageInfo(lessonList, 5);
        Result result = new Result();
        if (lessonList != null && lessonList.size() > 0) {
            result.setData(page);
            result.setCode("200");
            result.setMessage("查询成功");
        } else {
            result.setCode("201");
            result.setMessage("没要查询到课程");
        }
        return result;
    }

    /**
     * 根据父id查询课程信息
     * 分页
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pid", required = true, dataType = "String", paramType = "query", value = "父id"),
            @ApiImplicitParam(name = "pn", required = true, dataType = "int", paramType = "query", value = "页码"),
            @ApiImplicitParam(name = "isCharge", required = true, dataType = "int", paramType = "query", value = "是否收费(1:收费 0:免费 2:全部)")
    })
    @ApiOperation(value = "根据父id查询课程信息")
    @ResponseBody
    @RequestMapping(value = "selectLessonByParentClassify.do", method = RequestMethod.GET)
    public Result selectLessonByParentClassify(String pid, int isCharge
            , @RequestParam(value = "pn", defaultValue = "1") Integer pn) {
        PageHelper.startPage(pn, 5); //每页显示5条数据
        List<Lesson> lessonList = centerServiceImpl.selectLessonByParentClassifyId(pid, isCharge);
        PageInfo page = new PageInfo(lessonList, 5);
        Result result = new Result();
        if (lessonList != null && lessonList.size() > 0) {
            result.setCode("200");
            result.setData(page);
            result.setMessage("查询成功");
        } else {
            result.setCode("201");
            result.setMessage("没要查询到相关信息");
        }
        return result;
    }

    /**
     * 根据老师id获取老师已经发布的课程（分页）
     * 条件：0：未审核，1：未通过，2：未开始，3：正在进行，4：已经结束，5：未发布成功
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tid", required = true, dataType = "String", paramType = "query", value = "老师的id"),
            @ApiImplicitParam(name = "pn", required = true, dataType = "int", paramType = "query", value = "页码"),
            @ApiImplicitParam(name = "condition", required = true, dataType = "int", paramType = "query", value = "查询条件")
    })
    @ApiOperation(value = "根据老师id获取老师已经发布的课程")
    @ResponseBody
    @RequestMapping(value = "/getLessonByTeacherId.do", method = RequestMethod.GET)
    public Result getLessonByTeacherId(String tid
            , @RequestParam(value = "pn", defaultValue = "1") Integer pn, int condition) {

        Result result = new Result();
        PageHelper.startPage(pn, 5); //每页显示5条数据
        List<Lesson> lessons = lessonServiceImpl.getLessonByTeacherId(tid, condition);
        PageInfo page = new PageInfo(lessons, 5);

        if (lessons != null) {
            result.setCode("200");
            result.setMessage("查询成功");
            result.setData(page);
        } else {
            result.setCode("201");
            result.setMessage("该老师还没有发布课程");
        }
        return result;
    }

    /**
     * 发布的课程
     */
    @ApiOperation(value = "用户发布的课程")
    @ResponseBody
    @RequestMapping(value = "/publishLesson/{token}", method = RequestMethod.POST)
    public Result publishLesson(@RequestBody Lesson lesson, @PathVariable("token") String token) {
        Result result = new Result();
        User user = getUser(token); //判断用户是否登录
        if (user == null) {
            result.setCode("201");
            result.setMessage("token已过期");
            return result;
        }
        try {
            lessonServiceImpl.publishLesson(lesson, user);
            result.setCode("200");
            result.setMessage("发布成功，请等待后台审核！");
        } catch (Exception ex) {
            ex.printStackTrace();
            result.setCode("201");
            result.setMessage("发布失败！");
        }
        return result;
    }


    /**
     * 根据课程id，查询课程详细信息
     *
     * @param lid
     * @return
     */
    @ApiImplicitParam(name = "lid", required = true, dataType = "String", paramType = "query", value = "课程的id")
    @ApiOperation(value = "根据课程id，查询课程详细信息")
    @ResponseBody
    @RequestMapping(value = "/selectLessonByLid", method = RequestMethod.GET)
    public Result selectLessonByLid(String lid) {
        Result result = new Result();
        Lesson lesson = lessonServiceImpl.selectByLid(lid);
        if (lesson != null) {
            result.setCode("200");
            result.setMessage("查询成功");
            result.setData(lesson);
        } else {
            result.setCode("201");
            result.setMessage("查询失败");
        }
        return result;
    }
}
