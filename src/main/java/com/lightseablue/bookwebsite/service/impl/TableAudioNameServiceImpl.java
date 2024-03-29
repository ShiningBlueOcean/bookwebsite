package com.lightseablue.bookwebsite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lightseablue.bookwebsite.dao.TableAudioNameDao;
import com.lightseablue.bookwebsite.dto.TableAudioNameDTO;
import com.lightseablue.bookwebsite.entity.TableAudioName;
import com.lightseablue.bookwebsite.entity.TableUser;
import com.lightseablue.bookwebsite.service.TableAllTypesService;
import com.lightseablue.bookwebsite.service.TableAudioNameService;
import com.lightseablue.bookwebsite.service.TableAudioTypeService;
import com.lightseablue.bookwebsite.service.TableUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 书籍/专辑名名记录表   ps: 音乐:流行音乐:一个人(TableAudioName)表服务实现类
 *
 * @author LightseaBlue
 * @since 2020-12-24 17:26:27
 */
@Service("tableAudioNameService")
public class TableAudioNameServiceImpl extends ServiceImpl<TableAudioNameDao, TableAudioName> implements TableAudioNameService {
    @Autowired
    TableAudioNameDao dao;
    @Autowired
    TableUserService tableUserService;
    @Resource
    private TableAllTypesService tableAllTypesService;
    @Resource
    private TableAudioTypeService tableAudioTypeService;


    @Override
    public boolean updateAudioNameStuByUid(Integer uid, Integer audioStu) {
        UpdateWrapper<TableAudioName> tableAudioNameUpdateWrapper = new UpdateWrapper<>();
        tableAudioNameUpdateWrapper.lambda().eq(TableAudioName::getUId, uid)
                .ne(TableAudioName::getAudioNameStatus, 3)
                .set(TableAudioName::getAudioNameStatus, audioStu);
        return this.update(tableAudioNameUpdateWrapper);
    }

    @Override
    public boolean updateClickThroughRateByAudioNameId(String audioNameId, Long num) {
        UpdateWrapper<TableAudioName> tableAudioNameUpdateWrapper = new UpdateWrapper<>();
        tableAudioNameUpdateWrapper.lambda().eq(TableAudioName::getAudioNameId, audioNameId)
                .set(TableAudioName::getAudioNameCount, num);
        return this.update(tableAudioNameUpdateWrapper);
    }

    @Override
    public boolean updateByAudioNameId(String audioNameId) {
        UpdateWrapper<TableAudioName> tableAudioNameUpdateWrapper = new UpdateWrapper<>();
        tableAudioNameUpdateWrapper.lambda().eq(TableAudioName::getAudioNameId, audioNameId).set(TableAudioName::getAudioNameStatus, 3);
        return this.update(tableAudioNameUpdateWrapper);
    }

    @Override
    public boolean updateByAudioNameIdToOk(String audioNameId) {
        UpdateWrapper<TableAudioName> tableAudioNameUpdateWrapper = new UpdateWrapper<>();
        tableAudioNameUpdateWrapper.lambda().eq(TableAudioName::getAudioNameId, audioNameId).set(TableAudioName::getAudioNameStatus, 1);
        return this.update(tableAudioNameUpdateWrapper);
    }

    @Override
    public List<TableAudioNameDTO> monthTops() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -31);
        return getTops(c.getTime());
    }

    @Override
    public List<TableAudioNameDTO> weekTops() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -7);
        return getTops(c.getTime());
    }

    private List<TableAudioNameDTO> getTops(Date time) {
        QueryWrapper<TableAudioName> tableAudioNameQueryWrapper = new QueryWrapper<>();
        tableAudioNameQueryWrapper.lambda().ge(TableAudioName::getAudioNameDate, time)
                .orderByDesc(TableAudioName::getAudioNameCount).last("limit 5");
        List<TableAudioNameDTO> tableAudioNameDtoS = new ArrayList<>();
        List<TableAudioName> tableAudioNames = this.list(tableAudioNameQueryWrapper);
        tableAudioNames.forEach(e -> {
            TableUser tableUser = tableUserService.getById(e.getUId());
            TableAudioNameDTO tableAudioNameDTO = tableAudioNameToTableAudioNameDTO(e, tableUser.getUName());
            tableAudioNameDtoS.add(tableAudioNameDTO);
        });
        return tableAudioNameDtoS;
    }

    @Override
    public List<TableAudioNameDTO> getYouLike(Integer uid, int pageNum) {
        Page<TableAudioNameDTO> page = new Page<>();
        page.setSize(3);
        page.setCurrent(pageNum);
        return dao.findYouLike(page, uid).getRecords();
    }

    @Override
    public List<TableAudioNameDTO> findAllTopBook(int num) {
        Page<TableAudioNameDTO> page = new Page<>();
        page.setSize(3);
        page.setCurrent(num);
        return dao.findAllTopBook(page).getRecords();
    }

    @Override
    public List<TableAudioNameDTO> searchTopBookByType(int allTypeId) {
        return dao.searchTopBookByType(allTypeId);
    }

    @Override
    public List<TableAudioNameDTO> searchBooksByType(int allTypeId) {
        return dao.searchBooksByType(allTypeId);
    }

    @Override
    public Page<TableAudioName> searchAllBooksByType(int allTypeId, int thisPage) {
        QueryWrapper<TableAudioName> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableAudioName::getAllTypeId, allTypeId).eq(TableAudioName::getAudioNameStatus, 1);
        Page<TableAudioName> page = setBookListPage(thisPage, 6);
        return this.page(page, queryWrapper);
    }

    @Override
    public Page<TableAudioName> searchAllBooksByAudioTypeId(Integer audioTypeId, Integer thisPage) {
        QueryWrapper<TableAudioName> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableAudioName::getAudioTypeId, audioTypeId).eq(TableAudioName::getAudioNameStatus, 1);
        Page<TableAudioName> page = setBookListPage(thisPage, 6);
        return this.page(page, queryWrapper);
    }

    @Override
    public Page<TableAudioName> searchLikeBooks(String bookName, Integer thisPage, Integer size,
                                                List<Integer> uid, String allTypeId, String typeId, Integer stu) {
        QueryWrapper<TableAudioName> queryWrapper = new QueryWrapper<>();
        if (allTypeId != null && !"".equals(allTypeId)) {
            queryWrapper.lambda().eq(TableAudioName::getAllTypeId, allTypeId);
        }
        if (typeId != null && !"".equals(typeId)) {
            queryWrapper.lambda().eq(TableAudioName::getAudioTypeId, typeId);
        }
        if (stu != null) {
            queryWrapper.lambda().eq(TableAudioName::getAudioNameStatus, stu);
        }
        if (bookName != null && !"".equals(bookName)) {
            queryWrapper.lambda().eq(TableAudioName::getAudioNameStatus, 1).like(TableAudioName::getAudioNameName, bookName);
        }
        if (uid != null && uid.size() > 0) {
            queryWrapper.lambda().or().in(TableAudioName::getUId, uid);
        }

        Page<TableAudioName> tableAudioNamePage = setBookListPage(thisPage, size);
        return this.page(tableAudioNamePage, queryWrapper);
    }

    @Override
    public Page<TableAudioName> searchBooksByUid(Integer uid, Integer thisPage) {
        QueryWrapper<TableAudioName> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableAudioName::getUId, uid).eq(TableAudioName::getAudioNameStatus, 1).orderByDesc(TableAudioName::getAudioNameCount);
        Page<TableAudioName> tableAudioNamePage = setBookListPage(thisPage, 6);
        return this.page(tableAudioNamePage, queryWrapper);
    }

    private Page<TableAudioName> setBookListPage(Integer thisPage, Integer size) {
        Page<TableAudioName> page = new Page<>();
        page.setCurrent(thisPage);
        page.setSize(size);
        return page;
    }

    @Override
    public List<TableAudioNameDTO> pageToList(IPage<TableAudioName> tableAudioNamePage) {
        List<TableAudioName> tableAudioNameS = tableAudioNamePage.getRecords();
        List<TableAudioNameDTO> tableAudioNameDtoS = new ArrayList<>();
        tableAudioNameS.forEach(e -> {
            TableUser tableUser = tableUserService.getById(e.getUId());
            TableAudioNameDTO tableAudioNameDTO = tableAudioNameToTableAudioNameDTO(e, tableUser.getUName());
            tableAudioNameDtoS.add(tableAudioNameDTO);
        });
        return tableAudioNameDtoS;
    }

    @Override
    public TableAudioNameDTO tableAudioNameToTableAudioNameDTO(TableAudioName tableAudioName, String uName) {
        TableAudioNameDTO tableAudioNameDTO = new TableAudioNameDTO();
        tableAudioNameDTO.setAudioNameId(tableAudioName.getAudioNameId());
        tableAudioNameDTO.setAllTypeId(tableAudioName.getAllTypeId());
        tableAudioNameDTO.setAudioTypeId(tableAudioName.getAudioTypeId());
        tableAudioNameDTO.setAudioNameSummary(tableAudioName.getAudioNameSummary());
        tableAudioNameDTO.setAudioNameName(tableAudioName.getAudioNameName());
        tableAudioNameDTO.setAudioNameImg(tableAudioName.getAudioNameImg());
        tableAudioNameDTO.setAudioNameWriter(tableAudioName.getAudioNameWriter());
        tableAudioNameDTO.setAudioNameCount(tableAudioName.getAudioNameCount());
        tableAudioNameDTO.setUId(tableAudioName.getUId());
        tableAudioNameDTO.setAudioNameStatus(tableAudioName.getAudioNameStatus());
        tableAudioNameDTO.setAudioNameDate(tableAudioName.getAudioNameDate());
        tableAudioNameDTO.setUName(uName);
        return tableAudioNameDTO;
    }

    @Override
    public List<TableAudioNameDTO> pageToDto(IPage<TableAudioName> tableAudioNamePage) {
        List<TableAudioNameDTO> tableAudioNameDTOS = this.pageToList(tableAudioNamePage);
        tableAudioNameDTOS.forEach(e -> {
            String allTypeName = tableAllTypesService.getById(e.getAllTypeId()).getAllTypeName();
            e.setAllTypeName(allTypeName);
            //todo: 这种方法太憨
            e.setTotal(tableAudioNamePage.getTotal());
            String audioTypeName = tableAudioTypeService.getById(e.getAudioTypeId()).getAudioTypeName();
            e.setAudioTypeName(audioTypeName);
        });
        return tableAudioNameDTOS;
    }
}