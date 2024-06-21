package org.bdware.sw.nodemanager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.nodemanager.cache.LRU.LRUCache;
import org.bdware.sw.nodemanager.cache.SLRU.SLRUCache;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SwitchNodeManager {
    public LRUCache<String,AddressOfSwitch> cacheForAddress;
    public static ConcurrentHashMap<String,Boolean> cacheLock=new ConcurrentHashMap<>();
    public static Cache<String, DoipMessage> cacheForDoipMessage = Caffeine.newBuilder()
            .expireAfterWrite(10,TimeUnit.MINUTES).maximumSize(1000000).build();
    static Logger logger = LogManager.getLogger(SwitchNodeManager.class);

    public SwitchNodeManager(){
        this.cacheForAddress=new LRUCache(600000);
    }


    public void addAddressCache(String k, AddressOfSwitch v){
        //logger.info("Has added address.");
        this.cacheForAddress.put(k,v);
//        logger.info("Has added cache.");
    }

    public AddressOfSwitch getAddressCache(String k){
        return this.cacheForAddress.get(k);
    }

    public void deleteCache(String k){
        synchronized (this.cacheForAddress){
            if (this.cacheForAddress.get(k)!=null){
                this.cacheForAddress.remove(k);
//                logger.info("Has deleted cache.");
            }else{
//                logger.info("Cache not contains the key");
            }
        }

    }

    public AddressOfSwitch ifContainsAddress(String target){
        if (this.cacheForAddress.get(target)!=null){
//            logger.info("Contains Address");
//            logger.info(this.cacheForAddress.get(target));
            return this.cacheForAddress.get(target);
        }
        return null;
    }

    public Object ifInCache(DoipMessage msg) throws Exception {
        String target = msg.header.parameters.id;
        //后续进来的
//        if (msg.header.parameters.operation.equals(BasicOperations.Retrieve.getName())){
//            DoipMessage doipMessage = this.cacheForDoipMessage.getIfPresent(target);
//            if (doipMessage != null) {
//                return doipMessage;
//            }
//
//        if (!cacheLock.containsKey(target)){
//            doipMessage=cacheForDoipMessage.getIfPresent(target);
//                if (doipMessage==null){
//                    cacheLock.put(target, true );//只有第一个会触发
//                    logger.info("触发");
//                }
//        }else{
//            synchronized (cacheLock.get(target)){
//                if (cacheLock.containsKey(target))
//                cacheLock.get(target).wait(4000);
//            }
//            //唤醒是因为有数据来了
//            doipMessage = cacheForDoipMessage.getIfPresent(target);
//            if (doipMessage!=null){
//                return doipMessage;
//            }else{
//                cacheLock.remove(target);
//                throw new Exception();
//            }
//        }
//        }

        if (this.cacheForAddress.get(target)!=null){
//            logger.info("Is in cache");
            return this.cacheForAddress.get(target);
        }else{
//            logger.info("Not in cache");
            return null;
        }

    }


    
}
