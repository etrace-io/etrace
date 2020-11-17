package io.etrace.api.controller.yellowpage;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.yellowpage.SearchRecord;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.yellowpage.SearchRecordService;
import io.etrace.common.constant.Status;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;
import static io.etrace.api.config.SwaggerConfig.YELLOW_PAGE_TAG;

@RestController
@RequestMapping("/yellowpage/search/record")
@Api(tags = {YELLOW_PAGE_TAG, MYSQL_DATA})
public class SearchRecordController {

    @Autowired
    private SearchRecordService searchRecordService;

    @PostMapping
    @ApiOperation("创建record")
    public ResponseEntity<Long> create(@RequestBody SearchRecord searchRecord, @CurrentUser ETraceUser user) {
        searchRecord.setUpdatedBy(user.getUsername());
        searchRecord.setCreatedBy(user.getUsername());
        return ResponseEntity.ok(searchRecordService.create(searchRecord));
    }

    @PutMapping
    @ApiOperation("更新record")
    public ResponseEntity<Long> update(@RequestBody SearchRecord searchRecord, @CurrentUser ETraceUser user) {
        searchRecord.setUpdatedBy(user.getUsername());
        searchRecordService.update(searchRecord);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @ApiOperation("删除record")
    public ResponseEntity<Long> updateStatus(@RequestParam("id") Long id,
                                             @RequestParam(value = "status", defaultValue = "Inactive") String status) {
        searchRecordService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = {"/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("根据id查询record")
    public ResponseEntity<SearchRecord> findById(@PathVariable("id") Long id) throws Exception {
        return ResponseEntity.ok(searchRecordService.findById(id));
    }

    @GetMapping(value = "/findByParams", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("根据条件搜索record")
    public ResponseEntity<SearchResult<SearchRecord>> findByParams(
        @RequestParam(value = "listId", required = false) Long listId,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "status", defaultValue = "Active") String status,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @CurrentUser ETraceUser user) {
        return ResponseEntity.ok(searchRecordService.findByParams(listId, name, status, pageNum, pageSize, user));
    }

    @GetMapping(value = "/topN", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("最热最新置顶专用api")
    public ResponseEntity findTopN(int n, @CurrentUser ETraceUser user) {
        Map<String, List<SearchRecord>> resMap = new HashMap<>(4);
        // 收藏最多
        resMap.put("favorite", searchRecordService.findTopNFavorite(n, user));
        // 点击最多
        resMap.put("click", searchRecordService.findTopNClick(n, user));
        // 推荐最多
        resMap.put("recommond", searchRecordService.findTopNRecommond(n, user));
        // 最新
        SearchResult<SearchRecord> newestSearchResult = searchRecordService.findByParams(null, null,
            Status.Active.name(), 1, 10, user);
        resMap.put("new", newestSearchResult.getResults());
        return ResponseEntity.ok(resMap);
    }

    @GetMapping(value = "/checkUrl", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("检测url是否有相似的record")
    public ResponseEntity<List<SearchRecord>> findPerhapsRecord(@RequestParam(value = "url") String url) {
        return ResponseEntity.ok(searchRecordService.findByUrl(url));
    }
}
