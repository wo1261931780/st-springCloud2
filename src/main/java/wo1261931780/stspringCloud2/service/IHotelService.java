package wo1261931780.stspringCloud2.service;


import com.baomidou.mybatisplus.extension.service.IService;
import wo1261931780.stspringCloud2.pojo.Hotel;
import wo1261931780.stspringCloud2.pojo.PageResult;
import wo1261931780.stspringCloud2.pojo.RequestParams;

import java.io.IOException;

/**
 * @author junw
 */
public interface IHotelService extends IService<Hotel> {
	PageResult searchHotel(RequestParams requestParams) throws IOException;

	PageResult searchBooleanHotel(RequestParams requestParams);

	PageResult searchSortedHotel(RequestParams requestParams
	);
}
