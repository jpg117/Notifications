package modak.challenge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class NotificationServiceImpl implements NotificationService {
    private final Gateway gateway;
    private final Cache<String, Cache<String, Integer>> rateLimits;
    private final Logger logger;

    public NotificationServiceImpl(Gateway gateway) {
        this.gateway = gateway;
        rateLimits = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS) // Expire limits after 1 day
                .build();
        logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void send(String type, String userId, String message) {
        Cache<String, Integer> recipientCache = rateLimits.getIfPresent(type);
        if (recipientCache == null) {
            recipientCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(getExpirationTime(type), TimeUnit.MINUTES)
                    .build();
            rateLimits.put(type, recipientCache);
        }
        synchronized (recipientCache) {
            Integer sentMessages = recipientCache.getIfPresent(userId);
            if (sentMessages == null) {
                sentMessages = 0;
            }
            if (sentMessages < getMaxFrequency(type)) {
                recipientCache.put(userId, sentMessages + 1);
                gateway.send(userId, message);
            } else {
                logger.warning ("Rate limit exceeded for user " + userId + ", and type " + type);
            }
        }
    }

    // Helper method to determine expiration time based on notification type
    private long getExpirationTime(String notificationType) {
        switch (notificationType) {
            case "Status":
                return 1; // 1 minute
            case "Marketing":
                return 60; // 1 hour
            case "News":
                return 60 * 24; // 1 day
            default:
                return 60; // Default to 1 hour
        }
    }

    private int getMaxFrequency(String notificationType) {
        switch (notificationType) {
            case "News":
                return 1; // 1 per day
            case "Status":
                return 2; // 2 per minute
            case "Marketing":
                return 3; // 3 per hour
            default:
                return 1;
        }
    }
}
