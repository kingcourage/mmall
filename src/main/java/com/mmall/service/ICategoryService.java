package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.List;

/**
 * @author wcy
 * 2018/1/11
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName,Integer parentId);

    ServerResponse updateCateoryName(Integer categoryId, String categoryName);

    ServerResponse getChildrenParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
