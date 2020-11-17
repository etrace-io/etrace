package io.etrace.api.service.yellowpage;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.etrace.api.consts.SearchListTypeEnum;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.po.yellowpage.SearchKeyWord;
import io.etrace.api.model.po.yellowpage.SearchList;
import io.etrace.api.model.po.yellowpage.SearchRecord;
import io.etrace.api.model.po.yellowpage.SearchRecordKeyWordMapping;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.yellowpage.SearchRecordMapper;
import io.etrace.api.service.UserActionService;
import io.etrace.api.service.base.FavoriteAndViewInterface;
import io.etrace.common.constant.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class SearchRecordService implements FavoriteAndViewInterface {

    @Autowired
    private SearchRecordMapper searchRecordMapper;

    @Autowired
    private SearchRecordKeyWordMappingService searchRecordKeyWordMappingService;

    @Autowired
    private SearchKeyWordService searchKeyWordService;

    @Autowired
    private SearchListService searchListService;

    @Autowired
    private UserActionService userActionService;

    public Long create(SearchRecord searchRecord) {
        List<Long> keywordIdList = createKeyword(searchRecord.getKeywordList());
        searchRecordMapper.save(searchRecord);

        //创建映射关系
        searchRecordKeyWordMappingService.createMapping(searchRecord.getId(), keywordIdList);
        return searchRecord.getId();
    }

    public List<Long> createKeyword(List<SearchKeyWord> keywordList) {
        if (keywordList == null) {
            keywordList = Collections.emptyList();
        }
        List<Long> keywordIdList = new ArrayList<>(keywordList.size());
        for (SearchKeyWord searchKeyWord : keywordList) {
            if (null == searchKeyWord.getId()) {
                Long keywordId = searchKeyWordService.create(searchKeyWord);
                keywordIdList.add(keywordId);
            } else {
                keywordIdList.add(searchKeyWord.getId());
            }
        }
        return keywordIdList;
    }

    public void update(SearchRecord searchRecord) {
        //创建关键字
        List<Long> keywordIdList = createKeyword(searchRecord.getKeywordList());

        searchRecordMapper.save(searchRecord);
        //更新映射关系
        searchRecordKeyWordMappingService.updateMapping(searchRecord.getId(), keywordIdList);
    }

    public void updateStatus(Long id, String status) {
        searchRecordMapper.findById(id).ifPresent(re -> {
            re.setStatus(status);
            searchRecordMapper.save(re);
        });
    }

    public SearchResult<SearchRecord> findBasicInfoByParams(Long listId, String name, String status, int pageNum,
                                                            int pageSize) {
        int total = searchRecordMapper.countByIdAndNameAndStatus(listId, name, status);
        int start = (pageNum - 1) * pageSize;
        SearchResult<SearchRecord> result = new SearchResult<>();
        result.setTotal(total);
        if (start > total) {
            return result;
        }
        // todo: not implement
        //result.setResults(searchRecordMapper.findByParams(listId, name, status, start, pageSize));
        return result;
    }

    public SearchResult<SearchRecord> findByParams(Long listId, String name, String status, int pageNum, int pageSize
        , ETraceUser user) {
        SearchResult<SearchRecord> result = findBasicInfoByParams(listId, name, status, pageNum, pageSize);
        List<SearchRecord> recordList = result.getResults();
        if (CollectionUtils.isEmpty(recordList)) {
            return result;
        }
        findRecordExtendInfo(result.getResults(), null, user);
        return result;
    }

    public SearchRecord findById(Long id) throws BadRequestException {
        Optional<SearchRecord> op = searchRecordMapper.findById(id);
        if (!op.isPresent()) {
            throw new BadRequestException("could not find record");
        }
        SearchRecord searchRecord = op.get();
        //Long fileRecordId = searchRecord.getFileRecordId();
        //if (null != fileRecordId && fileRecordId != 0) {
        //    OssFileRecord ossFileRecord = ossFileUploadService.findById(fileRecordId);
        //    if (null != ossFileRecord) {
        //        searchRecord.setIcon(ossFileRecord.getUrl());
        //    }
        //}
        List<SearchRecordKeyWordMapping> mappingList = searchRecordKeyWordMappingService.findKeyWordIdListByRecordId(
            id);
        if (!CollectionUtils.isEmpty(mappingList)) {
            List<Long> keywordIdList = mappingList.stream().map(mapping -> mapping.getKeywordId()).collect(
                Collectors.toList());
            List<SearchKeyWord> keyWordList = searchKeyWordService.findByIdList(keywordIdList);
            if (null != keywordIdList) {
                List<SearchKeyWord> filterKeyWordList = keyWordList.stream().filter(
                    searchKeyWord -> Status.Active.name().equals(searchKeyWord.getStatus())).collect(
                    Collectors.toList());
                searchRecord.setKeywordList(filterKeyWordList);
            }
        }
        return searchRecord;
    }

    public List<SearchRecord> findByIdListWithPage(List<Long> idList, String status, Integer pageNo, Integer pageSize) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        return searchRecordMapper.findAllByIdInAndStatus(idList, status, PageRequest.of(pageNo - 1, pageSize));
    }

    /**
     * 根据关键字查询searchRecord记录
     *
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     */
    public SearchResult<SearchRecord> findByKeyword(String keyword, int pageNum, int pageSize, ETraceUser user) {
        SearchResult<SearchRecord> result = new SearchResult<>();
        if (Strings.isNullOrEmpty(keyword)) {
            return result;
        }
        // 查询关键字列表
        List<SearchKeyWord> keyWordList = searchKeyWordService.findKeywordList(keyword);
        if (CollectionUtils.isEmpty(keyWordList)) {
            return result;
        }

        List<Long> idList = keyWordList.stream().map(searchKeyWord -> searchKeyWord.getId()).collect(
            Collectors.toList());
        // 查询映射关系
        List<SearchRecordKeyWordMapping> mappingList = searchRecordKeyWordMappingService.findRecordIdListByKeyWordId(
            idList);
        Map<Long, List<Long>> recordKeyWordIdMapping = new HashMap<>();
        convertKeywordMapping(mappingList, recordKeyWordIdMapping);
        List<Long> recordIdList = newArrayList(recordKeyWordIdMapping.keySet());
        if (CollectionUtils.isEmpty(recordIdList)) {
            return result;
        }
        int total = recordIdList.size();
        result.setTotal(total);
        int start = (pageNum - 1) * pageSize;
        if (start > total) {
            return result;
        }
        // 分页查询record
        List<SearchRecord> recordList = searchRecordMapper.findAllByIdInAndStatus(idList, Status.Active.name(),
            PageRequest.of(pageNum - 1, pageSize));
        findRecordExtendInfo(recordList, mappingList, user);
        result.setResults(recordList);
        return result;
    }

    public void convertKeywordMapping(List<SearchRecordKeyWordMapping> mappingList,
                                      Map<Long, List<Long>> recordKeyWordIdMapping) {
        mappingList.stream().forEach(mapping -> {
            long recordId = mapping.getRecordId();
            long keywordId = mapping.getKeywordId();
            List<Long> keywordIdList = recordKeyWordIdMapping.get(recordId);
            if (null == keywordIdList) {
                keywordIdList = new ArrayList<>();
                recordKeyWordIdMapping.put(recordId, keywordIdList);
            }
            keywordIdList.add(keywordId);
        });
    }

    public List<SearchRecord> findTopNFavorite(int n, ETraceUser user) {
        List<SearchRecord> searchRecordList = searchRecordMapper.findAllByOrderByFavoriteIndex(PageRequest.of(0, n));
        findRecordExtendInfo(searchRecordList, user);
        return searchRecordList;
    }

    private void findRecordExtendInfo(List<SearchRecord> searchRecordList, ETraceUser user) {
        findRecordExtendInfo(searchRecordList, null, user);
    }

    public List<SearchRecord> findTopNClick(int n, ETraceUser user) {
        List<SearchRecord> searchRecordList = searchRecordMapper.findAllByOrderByClickIndex(PageRequest.of(0, n));
        findRecordExtendInfo(searchRecordList, user);
        return searchRecordList;
    }

    public List<SearchRecord> findTopNRecommond(int n, ETraceUser user) {
        List<SearchList> searchLists = searchListService.findByType(SearchListTypeEnum.RECOMMEND.getCode());
        List<Long> listIdList = searchLists.stream().map(searchList -> searchList.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(listIdList)) {
            return Collections.emptyList();
        }
        // todo: not implement
        //List<SearchRecord> searchRecordList = searchRecordMapper.findTopNewNByListIdList(listIdList, n);
        List<SearchRecord> searchRecordList = Lists.newArrayList();
        findRecordExtendInfo(searchRecordList, user);
        return searchRecordList;
    }

    public List<SearchRecord> findByUrl(String url) {
        return searchRecordMapper.findByUrl(url);
    }

    @Override
    public void updateUserFavorite(long id) {
        searchRecordMapper.updateUserFavorite(id, 1);
    }

    @Override
    public void updateUserView(long id) {
        searchRecordMapper.updateUserView(id, 1);
    }

    @Override
    public void deleteUserFavorite(long id) {
        searchRecordMapper.updateUserFavorite(id, -1);
    }

    private void findRecordExtendInfo(List<SearchRecord> searchRecordList,
                                      List<SearchRecordKeyWordMapping> mappingList, ETraceUser user) {
        if (CollectionUtils.isEmpty(searchRecordList)) {
            return;
        }
        List<Long> recordIdList = new ArrayList<>(searchRecordList.size());
        List<Long> fileRecordIdList = new ArrayList<>(searchRecordList.size());
        for (SearchRecord searchRecord : searchRecordList) {
            recordIdList.add(searchRecord.getId());
            //Long fileRecordId = searchRecord.getFileRecordId();
            //if (null != fileRecordId && 0 != fileRecordId) {
            //    fileRecordIdList.add(fileRecordId);
            //}
        }
        //// 查询ossfile 信息
        //List<OssFileRecord> ossFileRecords = ossFileUploadService.findByIdList(fileRecordIdList);
        //Map<Long, String> fileIdUrlMapping = new HashMap<>(ossFileRecords.size());
        //ossFileRecords.stream().forEach(
        //    ossFileRecord -> fileIdUrlMapping.put(ossFileRecord.getId(), ossFileRecord.getUrl()));
        //
        // 查询关键字信息
        List<SearchKeyWord> keyWordList;
        if (null == mappingList) {
            mappingList = searchRecordKeyWordMappingService.findRecordIdListByKeyWordId(recordIdList);
        }
        Map<Long, List<Long>> recordKeyWordIdMapping = new HashMap<>();
        convertKeywordMapping(mappingList, recordKeyWordIdMapping);
        // 设置keyword map
        Map<Long, SearchKeyWord> keywordMap = new HashMap<>(mappingList.size());
        if (!CollectionUtils.isEmpty(mappingList)) {
            Set<Long> keywordIdSet = mappingList.stream().map(
                searchRecordKeyWordMapping -> searchRecordKeyWordMapping.getKeywordId()).collect(Collectors.toSet());
            keyWordList = searchKeyWordService.findByIdList(newArrayList(keywordIdSet));
            keyWordList.stream().forEach(keyword -> keywordMap.put(keyword.getId(), keyword));
        }
        List<Long> favoriteRecordIds = Collections.emptyList();
        if (!user.isAnonymousUser()) {
            // todo:  这里从 findFavoriteByUser(psncode); 改成了 findFavoriteByUser(user.getEmail());
            UserAction userAction = userActionService.findFavoriteByUser(user);
            if (null != userAction && !CollectionUtils.isEmpty(userAction.getFavoriteRecordIds())) {
                favoriteRecordIds = userAction.getFavoriteRecordIds();
            }
        }
        final Set<Long> favoriteRecordIdSet = Sets.newHashSet(favoriteRecordIds);

        // 设置图片地址和关键字
        searchRecordList.stream().forEach(searchRecord -> {
            //// 设置图片下载地址
            //searchRecord.setIcon(fileIdUrlMapping.get(searchRecord.getFileRecordId()));
            // 设置关键字
            List<Long> keywordIdList = recordKeyWordIdMapping.get(searchRecord.getId());
            if (!CollectionUtils.isEmpty(keywordIdList)) {
                searchRecord.setKeywordList(
                    keywordIdList.stream().map(keywordId -> keywordMap.get(keywordId)).collect(Collectors.toList()));
            }
            if (favoriteRecordIdSet.contains(searchRecord.getId())) {
                searchRecord.setStar(Boolean.TRUE);
            }
        });
    }

    public void findExtendIcon(List<SearchRecord> recordList) {
        if (CollectionUtils.isEmpty(recordList)) {
            return;
        }
        //Set<Long> fileIdSet = recordList.stream().filter(
        //    searchRecord -> searchRecord.getFileRecordId() != null && 0 != searchRecord.getFileRecordId()).map(
        //    searchRecord -> searchRecord.getFileRecordId()).collect(Collectors.toSet());
        //List<OssFileRecord> ossFileRecords = ossFileUploadService.findByIdList(newArrayList(fileIdSet));
        //HashMap<Long, String> fileIdUrlMapping = new HashMap<>(ossFileRecords.size());
        //ossFileRecords.stream().forEach(
        //    ossFileRecord -> fileIdUrlMapping.put(ossFileRecord.getId(), ossFileRecord.getUrl()));
        //recordList.stream().forEach(
        //    searchRecord -> searchRecord.setIcon(fileIdUrlMapping.get(searchRecord.getFileRecordId())));
    }

}
