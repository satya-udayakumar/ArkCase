/**
 * SearchBase.Service
 *
 * manages all service call to application server
 *
 * @author jwu
 */
SearchBase.Service = {
    create : function(args) {
        if (args.url) {
            this.API_FACET_SEARCH_ = args.url;
        }
    },
    onInitialized : function() {
    }

    ,
    API_ADVANCED_SEARCH_ : "/api/v1/plugin/search/advancedSearch?q=name:"

    ,
    advancedSearchDeferred : function(searchInfo, postData, jtParams, sortMap, callbackSuccess, callbackError) {
        return AcmEx.Service.JTable.deferredPagingListAction(postData, jtParams, sortMap, function() {
            var url;
            url = App.getContextPath() + SearchBase.Service.API_ADVANCED_SEARCH_;

            var searchTerms = searchInfo.q.trim().replace(/(\*|\")/g, "").split(' ').filter(function(item) {
                return item != "";
            });
            searchTerms.forEach(function(element, index, arr) {
                arr[index] = element + '*';
            });

            url += searchTerms.join(" AND ");

            //for test
            //url = App.getContextPath() + "/resources/facetSearch.json?q=xyz";

            var filterParam = " AND object_type_s:USER AND status_lcs:VALID";
            url += filterParam;

            url += '&acm_ticket=' + document.getElementsByName('acmTicket')[0].value;

            return url;
        }, function(data) {
            var jtData = null
            if (SearchBase.Model.validateFacetSearchData(data)) {
                if (0 == data.responseHeader.status) {
                    //response.start should match to jtParams.jtStartIndex
                    //response.docs.length should be <= jtParams.jtPageSize

                    searchInfo.total = data.response.numFound;

                    var result = data.response;
                    //var page = Acm.goodValue(jtParams.jtStartIndex, 0);
                    //SearchBase.Model.cacheResult.put(page, result);
                    SearchBase.Model.putCachedResult(searchInfo, result);
                    jtData = callbackSuccess(result);
                    SearchBase.Controller.modelChangedResult(Acm.Service.responseWrapper(data, result));

                    if (!SearchBase.Model.isFacetUpToDate()) {
                        var facet = SearchBase.Model.makeFacet(data);
                        SearchBase.Controller.modeChangedFacet(Acm.Service.responseWrapper(data, facet));
                        SearchBase.Model.setFacetUpToDate(true);
                    }
                } else {
                    if (Acm.isNotEmpty(data.error)) {
                        //todo: report error to controller. data.error.msg + "(" + data.error.code + ")";
                    }
                }
            }
            return jtData;
        });
    }

};
