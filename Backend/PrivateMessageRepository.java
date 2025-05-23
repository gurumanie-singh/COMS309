package onetoone.Messages;

import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findBySender(User sender);
    List<PrivateMessage> findByReceiver(User receiver);
    List<PrivateMessage> findBySenderAndReceiver(User sender, User receiver);
}
