package wingchaincase.chaindiaryapi.service.remoting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RemoteCacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    public Object getCache(String key) {

        return redisTemplate.opsForValue().get(key);

    }

    public void saveCache(String key, Object value, Long lifeInSecond) {

        redisTemplate.opsForValue().set(key, value, lifeInSecond, TimeUnit.SECONDS);

    }

}
