package wo1261931780.stspringCloud2.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wo1261931780.stspringCloud2.mapper.HotelMapper;
import wo1261931780.stspringCloud2.pojo.Hotel;
import wo1261931780.stspringCloud2.pojo.HotelDoc;
import wo1261931780.stspringCloud2.pojo.PageResult;
import wo1261931780.stspringCloud2.pojo.RequestParams;
import wo1261931780.stspringCloud2.service.IHotelService;

import java.io.IOException;
import java.util.List;

/**
 * @author junw
 */
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {


	@Autowired
	private RestHighLevelClient restHighLevelClient;

	/**
	 * 根据条件搜索酒店信息
	 * 这里其实先放到抽象类，然后重写了一次。我觉得没必要
	 *
	 * @param requestParams 条件参数
	 * @return PageResult<Hotel>
	 */
	@Override
	public PageResult searchHotel(RequestParams requestParams) throws IOException {
		// 1.准备request
		SearchRequest searchRequest = new SearchRequest("hotel");

		// 2.使用dsl
		// 需要结合前端
		String requestParamsKey = requestParams.getKey();
		// 健壮性分析
		if (requestParamsKey.isEmpty()) {
			searchRequest.source().query(QueryBuilders.matchAllQuery());
		} else {
			searchRequest.source().query(QueryBuilders.matchQuery("all", requestParamsKey));
		}
		Integer size = requestParams.getSize();
		searchRequest.source().from((requestParams.getPage() - 1) * size).size(size);

		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		log.debug(String.valueOf(searchResponse));
		// 这里得到的是一个大的json字符串
		// 所以我们是需要进行逐层解析的
		SearchHits hits = searchResponse.getHits();
		long value = hits.getTotalHits().value;
		// 总数据量
		log.debug("总数据量:" + value);
		// 根据结构来逐层解析出东西
		return handleResponse(hits);
		// 2024年4月17日18:15:10，这里碰到个es没在容器中启动的问题，回家等修好了再试试项目
	}

	@Override
	public PageResult searchBooleanHotel(RequestParams requestParams) {
		SearchRequest searchRequest = new SearchRequest("hotel");
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		String requestParamsKey = requestParams.getKey();
		if (requestParamsKey.isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.matchAllQuery());
		} else {
			boolQueryBuilder.must(QueryBuilders.matchQuery("all", requestParamsKey));
		}
		// 然后分别根据城市，品牌，星级来判断一遍
		if (!requestParams.getCity().isEmpty()) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
		}
		if (!requestParams.getBrand().isEmpty()) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
		}

		if (requestParams.getStarName().isEmpty()) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("star", requestParams.getStarName()));
		}

		if (requestParams.getMinPrice() > 0) {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(requestParams.getMinPrice()).gte(requestParams.getMaxPrice()));
		}

		return null;
	}

	/**
	 * 处理搜索结果的方法
	 * 将方法抽取以后，代码更加简洁
	 *
	 * @param hits 搜索结果
	 */
	private PageResult handleResponse(SearchHits hits) {
		// 为了符合前后端交互的逻辑，这里使用统一的返回格式包装一遍
		PageResult pageResult = new PageResult();
		List<HotelDoc> hotels = pageResult.getHotels();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			String asString = hit.getSourceAsString();
			HotelDoc hotelDoc = JSON.parseObject(asString, HotelDoc.class);
			log.debug("结果为：" + hotelDoc);
			hotels.add(hotelDoc);
		}
		pageResult.setTotal(hotels.size());
		return pageResult;
	}
}
