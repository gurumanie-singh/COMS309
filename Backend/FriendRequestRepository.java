package onetoone.FriendRequests;

import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    // Find a pending friend request from receiver to sender
    FriendRequest findByReceiverAndSenderAndStatus(User receiver, User sender, FriendRequestStatus status);

    // Find pending requests for a specific user (receiver)
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);

    @Transactional
    void deleteBySenderAndReceiver(User sender, User receiver); // To delete request after acceptance/rejection

}
