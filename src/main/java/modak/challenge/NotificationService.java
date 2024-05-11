package modak.challenge;

public interface NotificationService {
    void send(String type, String userId, String message);
}
