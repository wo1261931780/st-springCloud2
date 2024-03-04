package wo1261931780.stspringCloud2.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import wo1261931780.stspringCloud2.mapper.HotelMapper;
import wo1261931780.stspringCloud2.pojo.Hotel;
import wo1261931780.stspringCloud2.service.IHotelService;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
