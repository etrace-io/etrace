package io.etrace.api.service.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchRecordKeyWordMapping;
import io.etrace.api.repository.yellowpage.SearchRecordKeyWordMappingMapper;
import io.etrace.common.constant.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

@Service
public class SearchRecordKeyWordMappingService {

    @Autowired
    private SearchRecordKeyWordMappingMapper searchRecordKeyWordMappingMapper;

    public void updateMapping(Long recordId, List<Long> keywordIdList) {
        if (null == keywordIdList) {
            keywordIdList = Collections.emptyList();
        }
        HashSet<Long> keywordIdSet = newHashSet(keywordIdList);
        List<SearchRecordKeyWordMapping> resList = searchRecordKeyWordMappingMapper
            .findByRecordIdAndKeywordIdInAndStatus(
                recordId, null, null, Pageable.unpaged());
        Map<Long, String> resStatusMap = new HashMap<>(resList.size());
        for (SearchRecordKeyWordMapping mapping : resList) {
            resStatusMap.put(mapping.getKeywordId(), mapping.getStatus());
        }
        Set<Long> needToRecovery = new HashSet<>();
        Set<Long> needToDelete = new HashSet<>();
        for (Map.Entry<Long, String> entry : resStatusMap.entrySet()) {
            Long keyWordId = entry.getKey();
            String status = entry.getValue();
            if (keywordIdSet.contains(keyWordId)) {
                if (!Status.Active.name().equals(status)) {
                    // 状态不为Active的 需要恢复为Active
                    needToRecovery.add(keyWordId);
                }
            } else {
                needToDelete.add(keyWordId);
            }
        }

        keywordIdSet.removeAll(resStatusMap.keySet());

        List<SearchRecordKeyWordMapping> createList = new ArrayList<>(keywordIdSet.size());
        keywordIdSet.forEach(keywordId -> {
            SearchRecordKeyWordMapping keyWordMapping = new SearchRecordKeyWordMapping();
            keyWordMapping.setKeywordId(keywordId);
            keyWordMapping.setRecordId(recordId);
            keyWordMapping.setStatus(Status.Active.name());
            createList.add(keyWordMapping);
        });
        // 新增关系
        if (!CollectionUtils.isEmpty(createList)) {
            searchRecordKeyWordMappingMapper.saveAll(createList);
        }
        // 恢复关系
        if (!CollectionUtils.isEmpty(needToRecovery)) {
            searchRecordKeyWordMappingMapper.batchUpdateStatus(newArrayList(needToRecovery), Status.Active.name());
        }
        // 删除关系
        if (!CollectionUtils.isEmpty(needToDelete)) {
            searchRecordKeyWordMappingMapper.batchUpdateStatus(newArrayList(needToDelete), Status.Inactive.name());
        }
    }

    public void createMapping(Long recordId, List<Long> keywordIdList) {
        if (null == recordId || CollectionUtils.isEmpty(keywordIdList)) {
            return;
        }
        List<SearchRecordKeyWordMapping> createList = new ArrayList<>(keywordIdList.size());
        for (Long keywordId : keywordIdList) {
            SearchRecordKeyWordMapping mapping = new SearchRecordKeyWordMapping();
            mapping.setStatus(Status.Active.name());
            mapping.setKeywordId(keywordId);
            mapping.setRecordId(recordId);
            createList.add(mapping);
        }
        searchRecordKeyWordMappingMapper.saveAll(createList);
    }

    public List<SearchRecordKeyWordMapping> findKeyWordIdListByRecordId(Long recordId) {
        return searchRecordKeyWordMappingMapper.findByRecordIdAndKeywordIdInAndStatus(recordId, null,
            Status.Active.name(), Pageable.unpaged());
    }

    public List<SearchRecordKeyWordMapping> findRecordIdListByKeyWordId(List<Long> keyWordIdList) {
        return searchRecordKeyWordMappingMapper.findByRecordIdAndKeywordIdInAndStatus(null, keyWordIdList,
            Status.Active.name(), Pageable.unpaged());
    }

    public List<SearchRecordKeyWordMapping> findAll(String status, int pageNum, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        return searchRecordKeyWordMappingMapper.findByRecordIdAndKeywordIdInAndStatus(null, null, status,
            pageRequest);
    }
}
