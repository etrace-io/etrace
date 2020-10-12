package io.etrace.api.controller.yellowpage;

import io.etrace.api.model.po.yellowpage.SearchKeyWord;
import io.etrace.api.service.yellowpage.SearchKeyWordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;
import static io.etrace.api.config.SwaggerConfig.YELLOW_PAGE_TAG;

@RestController
@RequestMapping("/yellowpage/search/keyword")
@Api(tags = {YELLOW_PAGE_TAG, MYSQL_DATA})
public class SearchKeyWordController {

    @Autowired
    private SearchKeyWordService searchKeyWordService;

    @PostMapping
    @ApiOperation("创建关键字")
    public Long create(@RequestBody SearchKeyWord searchKeyWord) {
        return searchKeyWordService.create(searchKeyWord);
    }

    @PutMapping
    @ApiOperation("更新关键字")
    public void update(@RequestBody SearchKeyWord searchKeyWord) {
        searchKeyWordService.update(searchKeyWord);
    }

    @DeleteMapping
    @ApiOperation("删除关键字")
    public void delete(@RequestParam("id") Long id,
                       @RequestParam(value = "status", defaultValue = "Inactive") String status) {
        searchKeyWordService.updateStatus(id, status);
    }

    @GetMapping("/findByKeyword")
    @ApiOperation("查询关键字关键字")
    public List<SearchKeyWord> findByParams(@RequestParam("keyword") String keyword) {
        return searchKeyWordService.findKeywordList(keyword);
    }

    //@GetMapping("/findSuggestKeyword")
    //@ApiOperation("查询可能相关联的关键字")
    //public List<SearchKeyWord> findSuggestKeyword(@RequestParam("keywordList") List<Long> keywordList,
    //                                              @RequestParam(value = "correlationCefficient", required = false,
    //                                                  defaultValue = "50") Integer correlationCefficient) {
    //    return searchKeyWordService.findCorrelationKeywords(keywordList, correlationCefficient);
    //}
}
