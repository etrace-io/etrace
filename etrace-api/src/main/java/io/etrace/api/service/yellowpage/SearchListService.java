package io.etrace.api.service.yellowpage;

import com.google.common.collect.Sets;
import io.etrace.api.consts.SearchListTypeEnum;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.po.yellowpage.SearchList;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.yellowpage.SearchListMapper;
import io.etrace.api.service.UserActionService;
import io.etrace.api.service.base.FavoriteAndViewInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class SearchListService implements FavoriteAndViewInterface {

    @Autowired
    private SearchListMapper searchListMapper;

    @Autowired
    private SearchListRecordMappingService searchListRecordMappingService;

    @Autowired
    private UserActionService userActionService;

    public SearchList create(SearchList searchList) {
        return searchListMapper.save(searchList);
    }

    public SearchList update(SearchList searchList) {
        return searchListMapper.save(searchList);
    }

    public void updateStatus(Long id, String status) {
        searchListMapper.findById(id).ifPresent(result -> {
            result.setStatus(status);
            searchListMapper.save(result);
        });
    }

    public SearchResult<SearchList> findByParams(String name, String status, int pageNum, int pageSize,
                                                 ETraceUser user) {

        int total = searchListMapper.countByNameAndStatus(name, status);
        SearchResult<SearchList> result = new SearchResult<>();
        result.setTotal(total);
        int start = (pageNum - 1) * pageSize;
        if (start > total) {
            return result;
        }
        result.setResults(searchListMapper.findAllByNameAndStatus(name, status, PageRequest.of(pageNum, pageSize)));

        if (!user.isAnonymousUser()) {
            //todo: UserAction userAction = userActionService.findFavoriteByUser(psncode);
            UserAction userAction = userActionService.findFavoriteByUser(user);
            if (null != userAction && !CollectionUtils.isEmpty(userAction.getFavoriteListIds())) {
                List<Long> favoriteListIds = userAction.getFavoriteListIds();
                HashSet<Long> listIdSet = Sets.newHashSet(favoriteListIds);
                result.getResults().stream().forEach(searchList -> {
                    if (listIdSet.contains(searchList.getId())) {
                        searchList.setStar(Boolean.TRUE);
                    }
                });
            }
        }
        return result;
    }

    public List<SearchList> findByType(int listType) {
        if (SearchListTypeEnum.ofCode(listType) == null) {
            throw new IllegalArgumentException("listType" + listType);
        }
        return searchListMapper.findByListType(listType);
    }

    public void editListRecord(Long listId, List<Long> recordIdList) {
        searchListRecordMappingService.createOrUpdateMapping(listId, recordIdList);
    }

    public List<SearchList> findByIdList(List<Long> idList) {
        return searchListMapper.findByIdIn(idList);
    }

    public Optional<SearchList> findById(Long id) {
        return searchListMapper.findById(id);
    }

    // todo: 这几个就是没有implement
    @Override
    public void updateUserFavorite(long id) {

    }

    @Override
    public void updateUserView(long id) {

    }

    @Override
    public void deleteUserFavorite(long id) {

    }
}
