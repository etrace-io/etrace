package io.etrace.api.service.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchListRecordMapping;
import io.etrace.api.repository.yellowpage.SearchListRecordMappingMapper;
import io.etrace.common.constant.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.google.common.collect.Sets.newHashSet;

@Service
public class SearchListRecordMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchListRecordMappingService.class);

    @Autowired
    private SearchListRecordMappingMapper searchListRecordMappingMapper;

    public void createOrUpdateMapping(Long listId, List<Long> recordIdList) {
        if (null == recordIdList) {
            recordIdList = Collections.emptyList();
        }
        HashSet<Long> recordIdSet = newHashSet(recordIdList);
        List<SearchListRecordMapping> createList = new ArrayList<>();
        List<Long> deleteIdList = new ArrayList<>();
        List<Long> recoveryIdList = new ArrayList<>();
        List<SearchListRecordMapping> searchListRecordMappingList = searchListRecordMappingMapper
            .findByListIdAndRecordIdInAndStatus(listId, null,
                null, Pageable.unpaged());
        Set<Long> dbRecordIdSet = new HashSet<>(searchListRecordMappingList.size());
        for (SearchListRecordMapping searchListRecordMapping : searchListRecordMappingList) {
            dbRecordIdSet.add(searchListRecordMapping.getRecordId());
            if (recordIdSet.contains(searchListRecordMapping.getRecordId())) {
                // 恢复所有已删除的关系
                if (!Status.Active.name().equals(searchListRecordMapping.getStatus())) {
                    recoveryIdList.add(searchListRecordMapping.getId());
                }
            } else {
                // 删除新的关系中不存在的
                deleteIdList.add(searchListRecordMapping.getId());
            }
        }
        recordIdSet.removeAll(dbRecordIdSet);
        if (!CollectionUtils.isEmpty(recordIdSet)) {
            for (Long recordId : recordIdSet) {
                SearchListRecordMapping searchListRecordMapping = new SearchListRecordMapping();
                searchListRecordMapping.setListId(listId);
                searchListRecordMapping.setRecordId(recordId);
                searchListRecordMapping.setStatus(Status.Active.name());
                createList.add(searchListRecordMapping);
            }
        }
        if (!CollectionUtils.isEmpty(createList)) {
            searchListRecordMappingMapper.saveAll(createList);
        }
        if (!CollectionUtils.isEmpty(deleteIdList)) {

            searchListRecordMappingMapper.batchUpdateStatus(deleteIdList, Status.Inactive.name());
        }
        if (!CollectionUtils.isEmpty(recoveryIdList)) {
            searchListRecordMappingMapper.batchUpdateStatus(recoveryIdList, Status.Active.name());
        }
    }
}
