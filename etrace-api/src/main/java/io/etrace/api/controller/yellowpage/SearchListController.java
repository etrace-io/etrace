package io.etrace.api.controller.yellowpage;

import com.google.common.base.Splitter;
import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.yellowpage.SearchList;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.yellowpage.SearchListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;
import static io.etrace.api.config.SwaggerConfig.YELLOW_PAGE_TAG;

@RestController
@RequestMapping("/yellowpage/search/list")
@Api(tags = {YELLOW_PAGE_TAG, MYSQL_DATA})
public class SearchListController {
    @Autowired
    private SearchListService searchListService;

    @PostMapping
    @ApiOperation("创建列表集")
    public SearchList create(@RequestBody SearchList searchList, @CurrentUser ETraceUser user) {
        searchList.setCreatedBy(user.getUsername());
        searchList.setUpdatedBy(user.getUsername());
        searchList.setMaintainerEmail(user.getEmail());
        return searchListService.create(searchList);
    }

    @PutMapping
    @ApiOperation("更新列表集")
    public SearchList update(@RequestBody SearchList searchList, @CurrentUser ETraceUser user)
        throws Exception {
        if (!checkModifyPermission(searchList.getId(), user)) {
            throw new UserForbiddenException("only admin and maintainer could modify");
        }
        searchList.setUpdatedBy(user.getUsername());
        return searchListService.update(searchList);
    }

    @DeleteMapping
    @ApiOperation("删除列表集")
    public void delete(@RequestParam("id") Long id,
                       @RequestParam(value = "status", defaultValue = "Inactive") String status,
                       @CurrentUser ETraceUser user) throws Exception {
        if (!checkModifyPermission(id, user)) {
            throw new UserForbiddenException("only admin and maintainer could modify");
        }
        searchListService.updateStatus(id, status);
    }

    @GetMapping(value = {"/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("根据id查询列表集")
    public SearchList findById(@PathVariable("id") Long id) {
        Optional<SearchList> op = searchListService.findById(id);
        return op.orElse(null);
    }

    @GetMapping("/findByParams")
    @ApiOperation("根据条件搜索列表集")
    public SearchResult<SearchList> findByParams(
        @RequestParam(value = "name") String name,
        @RequestParam(value = "status", defaultValue = "Active") String status,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
        @CurrentUser ETraceUser user) {
        return searchListService.findByParams(name, status, pageNum, pageSize, user);
    }

    @PostMapping("/editListRecord")
    public void editListRecord(
        @RequestParam("listId") Long listId,
        @RequestParam(value = "recordIdList", required = false) List<Long> recordIdList) {
        searchListService.editListRecord(listId, recordIdList);
    }

    private boolean checkModifyPermission(Long id, @CurrentUser ETraceUser user) throws BadRequestException {
        if (user.isAdmin()) {
            return true;
        }
        Optional<SearchList> op = searchListService.findById(id);
        if (!op.isPresent()) {
            throw new BadRequestException("could not find searchList");
        } else {
            SearchList searchList = op.get();
            return Splitter.on(",").splitToList(searchList.getMaintainerEmail()).contains(user.getEmail());
        }
    }
}
