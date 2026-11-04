package application.support;

import application.domain.ports.out.NotificationPort;
import application.domain.valueobjects.UserId;
import java.util.ArrayList;
import java.util.List;

public class RecordingNotifier implements NotificationPort {
    public record Sent(UserId recipient, String subject, String message) { }
    public final List<Sent> sent = new ArrayList<>();

    @Override public void notify(UserId recipient, String subject, String message) {
        sent.add(new Sent(recipient, subject, message));
    }
}
