package modak.challenge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class NotificationServiceImpl implements NotificationService {
    private Gateway gateway;
    private Cache<String, Cache<String, Integer>> rateLimits;

    public NotificationServiceImpl(Gateway gateway) {
        this.gateway = gateway;
        rateLimits = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS) // Expire limits after 1 day
                .build();
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
                recipientCache.put(userId, 1);
                gateway.send(userId, message);
            } else if (sentMessages < getMaxFrequency(type)) {
                recipientCache.put(userId, sentMessages + 1);
                gateway.send(userId, message);
            } else {
                throw new RuntimeException("Rate limit exceeded");
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
