package com.lightseablue.bookwebsite.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lightseablue.bookwebsite.entity.TableAllTypes;
import com.lightseablue.bookwebsite.entity.TableAudioType;

import java.util.List;

/**
 * 大类型下的小类型   ps:    音乐:流行音乐(TableAudioType)表服务接口
 *
 * @author LightseaBlue
 * @since 2020-12-24 17:26:28
 */
public interface TableAudioTypeService extends IService<TableAudioType> {

    /**
     * 根据大类型Id获取所有该大类型的小类型
     *
     * @param allTypeId
     * @return
     */
    List<TableAudioType> getTableAudioTypes(Integer allTypeId);

}