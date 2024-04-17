package wo1261931780.stspringCloud2.Controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import wo1261931780.stspringCloud2.pojo.PageResult;
import wo1261931780.stspringCloud2.pojo.RequestParams;
import wo1261931780.stspringCloud2.service.impl.HotelService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Intellij IDEA.
 * Project:st-springCloud2
 * Package:wo1261931780.hotel.Controller
 *
 * @author liujiajun_junw
 * @Date 2023-04-13-01  星期日
 * @description
 */
@Controller
@Slf4j
public class HotelController {


	@Autowired
	private HotelService hotelService;

	@PostMapping("/hotel")
	public Map<String, List<String>> hotel(@RequestBody Map<String, Object> map) {
		// log.info("hotel");
		// return hotelService.filterHotel(map);

		return null;
	}

	@PostMapping("/hotel/list")
	public PageResult searchHotel(@RequestBody RequestParams requestParams) throws IOException {
		log.info("searchHotel");
		return hotelService.searchHotel(requestParams);
	}

	@PostMapping("/hotel/booleanList")
	public PageResult searchBooleanHotel(@RequestBody RequestParams requestParams) throws IOException {
		log.info("searchHotel");
		return hotelService.searchBooleanHotel(requestParams);
	}
}
