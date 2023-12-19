package com.wangheng.woj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangheng.woj.common.BaseResponse;
import com.wangheng.woj.common.ErrorCode;
import com.wangheng.woj.common.ResultUtils;
import com.wangheng.woj.exception.BusinessException;
import com.wangheng.woj.exception.ThrowUtils;
import com.wangheng.woj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wangheng.woj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wangheng.woj.model.entity.Question;
import com.wangheng.woj.model.entity.QuestionSubmit;
import com.wangheng.woj.model.entity.User;
import com.wangheng.woj.model.enums.QuestionSubmitLanguageEnum;
import com.wangheng.woj.model.enums.QuestionSubmitStatusEnum;
import com.wangheng.woj.model.vo.QuestionSubmitVO;
import com.wangheng.woj.service.QuestionService;
import com.wangheng.woj.service.QuestionSubmitService;
import com.wangheng.woj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;


    @Resource
    private UserService userService;

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum 题目提交数
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                              HttpServletRequest request) {
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        if (questionSubmitAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if(question==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if(languageEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitAddRequest, questionSubmit);

        questionSubmitService.validQuestionSubmit(questionSubmit);
        questionSubmit.setUserId(loginUser.getId());
        // 设置初始status
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());

        boolean result = questionSubmitService.save(questionSubmit);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionSubmitId = questionSubmit.getId();
        return ResultUtils.success(newQuestionSubmitId);
    }

    /**
     * 获取我的提交列表
     *
     * @param questionSubmitQueryRequest
     * @param request
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listMyQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
            HttpServletRequest request) {
        if (questionSubmitQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 只查询当前用户的提交
        questionSubmitQueryRequest.setUserId(loginUser.getId());
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        User user = userService.getLoginUser(request);
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, user,request));
    }

    /**
     * 获取所有用户提交列表
     *
     * @param questionSubmitQueryRequest
     * @param request
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
            HttpServletRequest request) {
        if (questionSubmitQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, null,request));
    }
}
