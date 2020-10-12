package io.etrace.api.controller.yellowpage;

import io.etrace.api.consts.SearchListTypeEnum;
import io.etrace.api.controller.CurrentUser;
import io.etrace.api.model.bo.yellowpage.SearchRequest;
import io.etrace.api.model.bo.yellowpage.SearchType;
import io.etrace.api.model.bo.yellowpage.SuggestSearchResult;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.yellowpage.SearchKeyWord;
import io.etrace.api.model.po.yellowpage.SearchList;
import io.etrace.api.model.po.yellowpage.SearchRecord;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.yellowpage.SearchKeyWordService;
import io.etrace.api.service.yellowpage.SearchListService;
import io.etrace.api.service.yellowpage.SearchRecordService;
import io.etrace.common.constant.Status;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;
import static io.etrace.api.config.SwaggerConfig.YELLOW_PAGE_TAG;

@RestController
@RequestMapping("/yellowpage/search")
@Api(tags = {YELLOW_PAGE_TAG, MYSQL_DATA})
public class YellowPageSearchController {

    @Autowired
    private SearchListService searchListService;

    @Autowired
    private SearchRecordService searchRecordService;

    @Autowired
    private SearchKeyWordService searchKeyWordService;

    @PostMapping("/suggest")
    @ApiOperation("搜索下拉关键字列表提示")
    public ResponseEntity<Map<String, List<SuggestSearchResult>>> findSuggest(@RequestBody SearchRequest searchRequest,
                                                                              @CurrentUser ETraceUser user)
        throws Exception {
        Map<String, List<SuggestSearchResult>> result = new LinkedHashMap<>(3);
        String keyword = searchRequest.getKeyword();
        if (searchRequest.isSearchList()) {
            List<SuggestSearchResult> res = new ArrayList<>(searchRequest.getPageSize());
            SearchResult<SearchList> listSearchResult = searchListService.findByParams(keyword, Status.Active.name(),
                searchRequest.getPageNum(), searchRequest.getPageSize(), user);
            convertSuggestResult(res, listSearchResult.getResults(), SearchType.LIST);
            result.put(SearchType.LIST.name(), res);
        }
        if (searchRequest.isSearchRecord()) {
            List<SuggestSearchResult> res = new ArrayList<>(searchRequest.getPageSize());
            SearchResult<SearchRecord> recordSearchResult = searchRecordService.findByParams(null, keyword,
                Status.Active.name(), searchRequest.getPageNum(), searchRequest.getPageSize(), user);
            convertSuggestResult(res, recordSearchResult.getResults(), SearchType.RECORD);
            result.put(SearchType.RECORD.name(), res);
        }
        if (searchRequest.isSearchKeyWord()) {
            List<SuggestSearchResult> res = new ArrayList<>(searchRequest.getPageSize());
            SearchResult<SearchKeyWord> keyWordSearchResult = searchKeyWordService.findKeywordPage(keyword,
                Status.Active.name(), searchRequest.getPageNum(), searchRequest.getPageSize());
            convertSuggestResult(res, keyWordSearchResult.getResults(), SearchType.KEYWORD);
            result.put(SearchType.KEYWORD.name(), res);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/data")
    @ApiOperation("搜索关键字结果")
    public ResponseEntity<Map<String, List>> findData(@RequestBody SearchRequest searchRequest,
                                                      @CurrentUser ETraceUser user) {
        Map<String, List> result = new LinkedHashMap<>(3);
        String keyword = searchRequest.getKeyword();
        if (searchRequest.isSearchList()) {
            SearchResult<SearchList> listSearchResult = searchListService.findByParams(keyword, Status.Active.name(),
                searchRequest.getPageNum(), searchRequest.getPageSize(), user);
            result.put(SearchType.LIST.name(), listSearchResult.getResults());
        }
        if (searchRequest.isSearchRecord()) {
            SearchResult<SearchRecord> recordSearchResult = searchRecordService.findByParams(null, keyword,
                Status.Active.name(), searchRequest.getPageNum(), searchRequest.getPageSize(), user);
            result.put(SearchType.RECORD.name(), recordSearchResult.getResults());
        }
        if (searchRequest.isSearchKeyWord()) {
            SearchResult<SearchRecord> keywordRecordResult = searchRecordService.findByKeyword(keyword,
                searchRequest.getPageNum(), searchRequest.getPageSize(), user);
            result.put(SearchType.KEYWORD.name(), keywordRecordResult.getResults());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/home")
    @ApiOperation("最热最新与置顶")
    public ResponseEntity<Map<String, List>> findHomeData(@CurrentUser ETraceUser user) {
        Map<String, List> result = new LinkedHashMap<>(4);
        // 查询最热，最新，置顶
        result.put("LIST", searchListService.findByParams(null, Status.Active.name(), 1, 10, user).getResults());
        result.put(SearchListTypeEnum.HOTEST.name(), searchRecordService.findTopNFavorite(10, user));
        result.put(SearchListTypeEnum.RECOMMEND.name(), searchRecordService.findTopNRecommond(10, user));
        result.put(SearchListTypeEnum.NEWEST.name(),
            searchRecordService.findByParams(null, null, Status.Active.name(), 1, 10, user).getResults());
        return ResponseEntity.ok(result);
    }

    private void convertSuggestResult(List<SuggestSearchResult> resList, List obList, SearchType searchType)
        throws Exception {
        if (CollectionUtils.isEmpty(obList)) {
            return;
        }
        for (Object o : obList) {
            SuggestSearchResult suggestSearchResult = new SuggestSearchResult();
            suggestSearchResult.setType(searchType);
            resList.add(suggestSearchResult);
            switch (searchType) {
                case LIST:
                    SearchList searchList = (SearchList)o;
                    suggestSearchResult.setId(searchList.getId());
                    suggestSearchResult.setName(searchList.getName());
                    suggestSearchResult.setIcon(searchList.getIcon());
                    suggestSearchResult.setDescription(searchList.getDescription());
                    break;
                case RECORD:
                    SearchRecord searchRecord = (SearchRecord)o;
                    suggestSearchResult.setId(searchRecord.getId());
                    suggestSearchResult.setName(searchRecord.getName());
                    suggestSearchResult.setIcon(searchRecord.getIcon());
                    suggestSearchResult.setDescription(searchRecord.getDescription());
                    break;
                case KEYWORD:
                    SearchKeyWord searchKeyWord = (SearchKeyWord)o;
                    suggestSearchResult.setId(searchKeyWord.getId());
                    suggestSearchResult.setName(searchKeyWord.getName());
                    break;
                default:
                    throw new Exception("unknow type");
            }

        }
    }
}
