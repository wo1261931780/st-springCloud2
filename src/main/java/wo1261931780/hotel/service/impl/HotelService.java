package wo1261931780.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import wo1261931780.hotel.mapper.HotelMapper;
import wo1261931780.hotel.pojo.Hotel;
import wo1261931780.hotel.service.IHotelService;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
