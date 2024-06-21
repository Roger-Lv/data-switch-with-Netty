package org.bdware.sw.nodemanager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.irp.stateinfo.StateInfoBase;
import org.bdware.sw.client.FixedAuditIrpClient;
import org.bdware.sw.nodemanager.cache.LRU.LRUCache;

import java.util.concurrent.TimeUnit;

//DigitalSpaceNodeManager: 包含自身拥有的管理List[DigitalSapce-List[repo-ip:port]]的管理信息
public class DigitalSpaceNodeManager {
    public LRUCache<String, EndpointConfig> endpointCache;
    public static Cache<String, Boolean> banCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(1000000).build();
    public FixedAuditIrpClient auditIrpClient;
    public EndpointConfig endpointConfig;
    static Logger logger = LogManager.getLogger(DigitalSpaceNodeManager.class);

    public DigitalSpaceNodeManager(EndpointConfig config) {
        if (this.auditIrpClient == null && this.endpointConfig == null) {
            this.auditIrpClient = new FixedAuditIrpClient(config);
            this.endpointConfig = config;
        }
        this.endpointCache = new LRUCache<>(600000);
    }

    public Object ifInCache(DoipMessage msg) {
        String target = msg.header.parameters.id;
        EndpointConfig endpointConfig = null;
        try {
            //只有Retrieve请求的数据会存到缓存中
//            DoipMessage message = cacheForDoipMessage.getIfPresent(target);
//            if (message!=null){
//                return message;
//            }
            //如果在banCache中，说明暂时（这种是本机连不上的情况）/永久不属于该空间
            Boolean b = banCache.getIfPresent(target);
            if (b != null && b) {
                logger.info("Is in DigitalSpaceNodeManager's banCache! ");
                return null;
            }
            //如果能直接拿到对应的endPointConfig，直接返回
            if (this.endpointCache.get(target) != null) {
                logger.info("Is in DigitalSpaceNodeManager's cache");
                banCache.put(target, false);
                return endpointCache.get(target);
            } else {
                StateInfoBase result = null;
                result = this.auditIrpClient.resolve(target);
                //属于该空间
                if (result != null && result.handleValues != null) {
                    synchronized (this.endpointCache) {
                        this.endpointCache.put(target, this.endpointConfig);
                    }
                    banCache.put(target, false);
                    logger.info("put endpoint cache");
                    return this.endpointConfig;
                }
                //不属于该空间
                banCache.put(target, true);
            }
        } catch (Exception e) {
            logger.error("无法连接到本机管理空间irpRouter");
            e.printStackTrace();
            banCache.put(target, true);
            return null;
        }
        logger.debug("Not in DigitalSpaceNodeManager's cache");
        return null;
    }
}
