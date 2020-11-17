package io.etrace.api.service.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchKeyWord;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.yellowpage.SearchKeyWordMapper;
import io.etrace.common.constant.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchKeyWordService {

    @Autowired
    private SearchKeyWordMapper searchKeyWordMapper;

    //@Autowired
    //private SearchKeywordCorrelationService searchKeywordCorrelationService;

    public Long create(SearchKeyWord searchKeyWord) {

        searchKeyWordMapper.save(searchKeyWord);
        return searchKeyWord.getId();
    }

    public void update(SearchKeyWord searchKeyWord) {
        searchKeyWordMapper.save(searchKeyWord);
    }

    public List<SearchKeyWord> findByIdList(List<Long> idList) {
        return searchKeyWordMapper.findByIdIn(idList);
    }

    public void updateStatus(Long id, String status) {
        searchKeyWordMapper.findById(id).ifPresent(result -> {
            result.setStatus(status);
            searchKeyWordMapper.save(result);
        });
    }

    public List<SearchKeyWord> findKeywordList(String keyword) {
        PageRequest pageRequest = PageRequest.of(0, 99);
        return searchKeyWordMapper.findByStatusAndNameContaining(Status.Active.name(), keyword, pageRequest);
    }

    public SearchResult<SearchKeyWord> findKeywordPage(String keyword, String status, int pageNum, int pageSize) {
        SearchResult<SearchKeyWord> result = new SearchResult<>();
        int total = searchKeyWordMapper.countByStatusAndNameContaining(status, keyword);
        result.setTotal(total);
        int start = (pageNum - 1) * pageSize;
        if (start > total) {
            return result;
        }
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        result.setResults(searchKeyWordMapper.findByStatusAndNameContaining(status, keyword, pageRequest));
        return result;
    }

    //public List<SearchKeyWord> findCorrelationKeywords(List<Long> keywordIdList,Integer correlationCefficient) {
    //    List<Long> correlationKeywordIdList = searchKeywordCorrelationService
    //    .findCorrelationKeywordIdByKeywordIdList(keywordIdList,correlationCefficient);
    //    if (CollectionUtils.isEmpty(correlationKeywordIdList)) {
    //        return Collections.emptyList();
    //    }
    //    return searchKeyWordMapper.findByIdList(correlationKeywordIdList);
    //}
}
