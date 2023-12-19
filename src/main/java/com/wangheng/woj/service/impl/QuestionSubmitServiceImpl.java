package com.wangheng.woj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.wangheng.woj.common.ErrorCode;
import com.wangheng.woj.constant.CommonConstant;
import com.wangheng.woj.exception.BusinessException;
import com.wangheng.woj.mapper.QuestionSubmitMapper;
import com.wangheng.woj.model.dto.questionsubmit.JudgeInfo;
import com.wangheng.woj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wangheng.woj.model.entity.QuestionSubmit;
import com.wangheng.woj.model.entity.User;
import com.wangheng.woj.model.vo.QuestionSubmitVO;
import com.wangheng.woj.model.vo.UserVO;
import com.wangheng.woj.service.QuestionSubmitService;
import com.wangheng.woj.service.UserService;
import com.wangheng.woj.utils.SqlUtils;
import jdk.nashorn.internal.scripts.JD;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author WangH
* @description 针对表【questionSubmit_submit(题目提交)】的数据库操作Service实现
* @createDate 2023-12-17 20:58:41
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{
    private final static Gson GSON = new Gson();
    @Resource
    private UserService userService;

    @Override
    public void validQuestionSubmit(QuestionSubmit questionSubmit) {
        String code = questionSubmit.getCode();
        if (StringUtils.isNotBlank(code) && code.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码过长");
        }
    }

    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User user, HttpServletRequest request) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        if(user==null){
            User loginUser = userService.getLoginUser(request);
            questionSubmitList=questionSubmitList.stream().map(questionSubmit->{
                if(loginUser==null|| !Objects.equals(loginUser.getId(), questionSubmit.getUserId()))
                    questionSubmit.setCode(null);
                return questionSubmit;
            }).collect(Collectors.toList());
        }
        // 填充信息
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> getQuestionSubmitVO(questionSubmit,user)).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit,User user) {
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();

        BeanUtils.copyProperties(questionSubmit,questionSubmitVO);
        String judgeInfo = questionSubmit.getJudgeInfo();
        if(judgeInfo!=null){
            questionSubmitVO.setJudgeInfo(GSON.fromJson(judgeInfo,JudgeInfo.class));
        }
        Long userId = questionSubmit.getUserId();
        if(userId!=null){
            questionSubmitVO.setUser(userService.getUserVO(user));
        }
        return questionSubmitVO;
    }
}




