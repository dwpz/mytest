package com.atguigu.gulimall.product.entity;

import com.atguigu.common.group.AddGroup;
import com.atguigu.common.group.UpdateGroup;
import com.atguigu.common.group.UpdateStatusGroup;
import com.atguigu.common.valid.ListValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 *
 * @author chenshun
 * @email 1365824378@qq.com
 * @date 2022-12-06 00:01:24
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class,UpdateStatusGroup.class})
    @Null(message = "新增不能指定id", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名必须非空", groups = {UpdateGroup.class, AddGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    //品牌logo地址不能为空,并且logo必须是一个合法的url地址
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * 介绍
     */
    //介绍不做限制
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    //自定义校验注解
    @NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;
}
