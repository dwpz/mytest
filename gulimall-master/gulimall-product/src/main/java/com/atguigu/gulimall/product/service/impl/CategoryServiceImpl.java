package com.atguigu.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        //1 查询所有的分类
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        //2 按取出所有的父类
        List<CategoryEntity> parentCategoryList = entityList.stream().filter(menu -> menu.getParentCid() == 0)
                //3 获取子分类
                .map(menu -> {
                    menu.setChildren(getChildrens(menu, entityList));
                    return menu;
                })
                .sorted(Comparator.comparingInt(entity -> (entity.getSort() != null ? entity.getSort() : 0)))
                .collect(Collectors.toList());
        return parentCategoryList;
    }

    @Override
    public void removeByCategoryIds(Long[] catIds) {
        //Todo 1.检查菜单是否在其他地方引用

        baseMapper.deleteBatchIds(Arrays.asList(catIds));
    }

    /**
     * 获取某一分类下边的子类
     *
     * @param root
     * @param allList
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> allList) {
      return  allList.stream().filter(categoryEntity -> root.getCatId() == categoryEntity.getParentCid())
                .map(menu -> {
                    menu.setChildren(getChildrens(menu,allList));
                    return menu;
                })
                .sorted(Comparator.comparingInt(entity -> (entity.getSort() != null ? entity.getSort() : 0)))
                .collect(Collectors.toList());
    }
}