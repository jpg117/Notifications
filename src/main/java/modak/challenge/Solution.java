package modak.challenge;

public class Solution {

    public static void main(String[] args) {
        NotificationServiceImpl service = new NotificationServiceImpl(new Gateway());
        service.send("News", "user1", "news 1");
        service.send("News", "user2", "news 2");
        service.send("Status", "user1", "status 1");
        service.send("Status", "user1", "status 2");
        service.send("Status", "user2", "status 3");
        service.send("Marketing", "user1", "marketing 1");
        service.send("Marketing", "user1", "marketing 2");
        service.send("Marketing", "user1", "marketing 3");
        service.send("Marketing", "user2", "marketing 4");
        service.send("News", "user1", "news 3");
    }
}
