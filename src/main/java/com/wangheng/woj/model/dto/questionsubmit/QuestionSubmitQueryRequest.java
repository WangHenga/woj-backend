package com.wangheng.woj.model.dto.questionsubmit;

import com.wangheng.woj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子收藏查询请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;


    /**
     * 判题状态，0-待判题，1-判题中，2-成功，3-失败
     */
    private Integer status;

    /**
     * 问题id
     */
    private Long questionId;

    /**
     * 提交者id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}