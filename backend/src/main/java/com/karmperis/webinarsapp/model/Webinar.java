package com.karmperis.webinarsapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a scheduled webinar session.
 * Contains basic metadata (title, description, schedule, duration) and
 * associations to the organizing {@link User} and enrolled {@link User} participants.
 */
@Entity
@Table(name = "webinars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Webinar extends AbstractUuidEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "scheduled_date", nullable = false, columnDefinition = "DATETIME2(6)")
    private LocalDateTime scheduledDate;

    @Column(nullable = false)
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_webinars",
            joinColumns = @JoinColumn(name = "webinar_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    /**
     * Return an immutable snapshot of the users enrolled in this webinar.
     * The returned set is an unmodifiable copy to protect the internal collection.
     *
     * @return an immutable set with the webinar participants
     */
    public Set<User> getAllParticipants(){
        return Set.copyOf(participants);
    }

    /**
     * Enroll a user as a participant of this webinar. This method updates both
     * sides of the bidirectional relationship: the webinar's participants set and
     * the user's enrolled webinars.
     *
     * @param participant the user to enroll; callers should ensure the argument is not null
     */
    public void addParticipant(User participant){
        participants.add(participant);
        participant.getAllEnrolledWebinars().add(this);
    }

    /**
     * Remove a user from this webinar's participants. The bidirectional relationship
     * is updated on both sides (webinar participants and user's enrolled webinars).
     *
     * @param participant the user to remove from participants
     */
    public void removeParticipant(User participant) {
        participants.remove(participant);
        participant.getAllEnrolledWebinars().remove(this);
    }
}