package configs.cloud.client.factory;

import configs.cloud.client.enums.CacheProvider;
import configs.cloud.client.service.CacheService;
import configs.cloud.client.service.impl.EhcacheService;
import configs.cloud.client.service.impl.HazelCastService;


public class CacheFactory {
	
	public static CacheService getCacheService(CacheProvider cacheProvider){
		
		if(cacheProvider == CacheProvider.EHCACHE){
			return  new EhcacheService();
			
		} else if(cacheProvider == CacheProvider.HAZELCAST){
			return  new HazelCastService();
		} else {
			throw new RuntimeException("Unknown Cache Provider");
		}
		
	}

}
