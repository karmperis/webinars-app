package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.dto.WebinarEditDTO;
import com.karmperis.webinarsapp.dto.WebinarInsertDTO;
import com.karmperis.webinarsapp.dto.WebinarReadOnlyDTO;
import com.karmperis.webinarsapp.mapper.WebinarMapper;
import com.karmperis.webinarsapp.model.User;
import com.karmperis.webinarsapp.model.Webinar;
import com.karmperis.webinarsapp.repository.UserRepository;
import com.karmperis.webinarsapp.repository.WebinarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebinarServiceImplTest {

    @Mock
    private WebinarRepository webinarRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebinarMapper webinarMapper;

    @InjectMocks
    private WebinarServiceImpl webinarService;

    private Webinar webinar;
    private User organizer;
    private User participant;
    private UUID webinarUuid;
    private UUID organizerUuid;
    private UUID participantUuid;

    @BeforeEach
    void setUp() {
        webinarUuid = UUID.randomUUID();
        organizerUuid = UUID.randomUUID();
        participantUuid = UUID.randomUUID();

        organizer = new User();
        organizer.setUuid(organizerUuid);
        organizer.setUsername("organizerUser");

        participant = new User();
        participant.setUuid(participantUuid);
        participant.setUsername("participantUser");

        webinar = new Webinar();
        webinar.setUuid(webinarUuid);
        webinar.setTitle("Spring Boot Advanced");
        webinar.setDuration(120);
        webinar.setUser(organizer);
    }

    // ==========================================
    // TESTS-SAVE
    // ==========================================

    @Test
    @DisplayName("saveWebinar: Should save and return WebinarReadOnlyDTO successfully")
    void saveWebinar_Success() throws Exception {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("Spring Boot Advanced");
        when(dto.duration()).thenReturn(120);

        WebinarReadOnlyDTO readOnlyDTO = mock(WebinarReadOnlyDTO.class);
        when(readOnlyDTO.title()).thenReturn("Spring Boot Advanced");

        when(webinarRepository.findByTitleAndDeletedAtIsNull("Spring Boot Advanced")).thenReturn(Optional.empty());
        when(userRepository.findByUuidAndDeletedAtIsNull(organizerUuid)).thenReturn(Optional.of(organizer));
        when(webinarMapper.mapToWebinarEntity(dto)).thenReturn(webinar);
        when(webinarRepository.save(any(Webinar.class))).thenReturn(webinar);
        when(webinarMapper.mapToWebinarReadOnlyDTO(webinar)).thenReturn(readOnlyDTO);

        WebinarReadOnlyDTO result = webinarService.saveWebinar(dto, organizerUuid);

        assertNotNull(result);
        assertEquals("Spring Boot Advanced", result.title());
        verify(webinarRepository, times(1)).save(any(Webinar.class));
        assertEquals(organizer, webinar.getUser()); // Verify organizer was set
    }

    @Test
    @DisplayName("saveWebinar: Should throw Exception when DTO is null")
    void saveWebinar_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.saveWebinar(null, organizerUuid));

        verifyNoInteractions(webinarRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("saveWebinar: Should throw Exception when title already exists")
    void saveWebinar_ThrowsEntityAlreadyExistsException_WhenTitleAlreadyExists() {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("Spring Boot Advanced");
        when(dto.duration()).thenReturn(120);

        when(webinarRepository.findByTitleAndDeletedAtIsNull("Spring Boot Advanced"))
                .thenReturn(Optional.of(webinar));

        assertThrows(EntityAlreadyExistsException.class,
                () -> webinarService.saveWebinar(dto, organizerUuid));

        verify(webinarRepository).findByTitleAndDeletedAtIsNull("Spring Boot Advanced");
        verifyNoInteractions(userRepository);
        verify(webinarRepository, never()).save(any());
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("saveWebinar: Should throw Exception when title is blank")
    void saveWebinar_ThrowsEntityInvalidArgumentException_WhenTitleIsBlank() {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("   ");

        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.saveWebinar(dto, organizerUuid));

        verifyNoInteractions(webinarRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("saveWebinar: Should throw Exception when duration is invalid")
    void saveWebinar_ThrowsEntityInvalidArgumentException_WhenDurationIsInvalid() {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("Spring Boot Advanced");
        when(dto.duration()).thenReturn(10);

        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.saveWebinar(dto, organizerUuid));

        verifyNoInteractions(webinarRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("saveWebinar: Should throw Exception when title is invalid")
    void saveWebinar_ThrowsEntityInvalidArgumentException_WhenTitleIsTooShort() {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("abc"); // Invalid length (< 5)
        when(dto.duration()).thenReturn(120);

        assertThrows(EntityInvalidArgumentException.class, () -> webinarService.saveWebinar(dto, organizerUuid));
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveWebinar: Should throw Exception when title is too long")
    void saveWebinar_ThrowsEntityInvalidArgumentException_WhenTitleIsTooLong() {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("A".repeat(101));
        when(dto.duration()).thenReturn(120);

        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.saveWebinar(dto, organizerUuid));

        verifyNoInteractions(webinarRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("saveWebinar: Should throw Exception when organizer does not exist")
    void saveWebinar_ThrowsEntityNotFoundException_WhenOrganizerNotFound() {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("Spring Boot Advanced");
        when(dto.duration()).thenReturn(120);

        when(webinarRepository.findByTitleAndDeletedAtIsNull("Spring Boot Advanced")).thenReturn(Optional.empty());
        when(userRepository.findByUuidAndDeletedAtIsNull(organizerUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> webinarService.saveWebinar(dto, organizerUuid));
        verify(webinarRepository, never()).save(any());
    }

    // ==========================================
    // TESTS-FIND METHODS
    // ==========================================

    @Test
    @DisplayName("findAllWebinars: Should return a Page of Webinars")
    void findAllWebinars_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Webinar> webinarPage = new PageImpl<>(List.of(webinar));

        WebinarReadOnlyDTO readOnlyDTO = mock(WebinarReadOnlyDTO.class);
        when(readOnlyDTO.title()).thenReturn("Spring Boot Advanced");

        when(webinarRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(webinarPage);
        when(webinarMapper.mapToWebinarReadOnlyDTO(webinar)).thenReturn(readOnlyDTO);

        Page<WebinarReadOnlyDTO> result = webinarService.findAllWebinars(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot Advanced", result.getContent().getFirst().title());
    }

    @Test
    @DisplayName("findAllWebinarsByOrganizer: Should return a Page of Webinars for a specific organizer")
    void findAllWebinarsByOrganizer_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Webinar> webinarPage = new PageImpl<>(List.of(webinar));

        WebinarReadOnlyDTO readOnlyDTO = mock(WebinarReadOnlyDTO.class);
        when(readOnlyDTO.title()).thenReturn("Spring Boot Advanced");

        when(userRepository.findByUuidAndDeletedAtIsNull(organizerUuid)).thenReturn(Optional.of(organizer));
        when(webinarRepository.findAllByUserAndDeletedAtIsNull(organizer, pageable)).thenReturn(webinarPage);
        when(webinarMapper.mapToWebinarReadOnlyDTO(webinar)).thenReturn(readOnlyDTO);

        Page<WebinarReadOnlyDTO> result = webinarService.findAllWebinarsByOrganizer(organizerUuid, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot Advanced", result.getContent().getFirst().title());
    }

    @Test
    @DisplayName("findAllWebinarsByOrganizer: Should throw Exception when organizer is not found")
    void findAllWebinarsByOrganizer_ThrowsEntityNotFoundException_WhenOrganizerNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUuidAndDeletedAtIsNull(organizerUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.findAllWebinarsByOrganizer(organizerUuid, pageable));

        verify(userRepository).findByUuidAndDeletedAtIsNull(organizerUuid);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("findAllWebinarsByParticipant: Should return a Page of Webinars for a participant")
    void findAllWebinarsByParticipant_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Webinar> webinarPage = new PageImpl<>(List.of(webinar));

        WebinarReadOnlyDTO readOnlyDTO = mock(WebinarReadOnlyDTO.class);
        when(readOnlyDTO.title()).thenReturn("Spring Boot Advanced");

        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid))
                .thenReturn(Optional.of(participant));

        when(webinarRepository.findAllByParticipantsContainingAndDeletedAtIsNull(participant, pageable))
                .thenReturn(webinarPage);

        when(webinarMapper.mapToWebinarReadOnlyDTO(webinar))
                .thenReturn(readOnlyDTO);

        Page<WebinarReadOnlyDTO> result =
                webinarService.findAllWebinarsByParticipant(participantUuid, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot Advanced", result.getContent().getFirst().title());

        verify(userRepository).findByUuidAndDeletedAtIsNull(participantUuid);
        verify(webinarRepository).findAllByParticipantsContainingAndDeletedAtIsNull(participant, pageable);
        verify(webinarMapper).mapToWebinarReadOnlyDTO(webinar);
    }

    @Test
    @DisplayName("findAllWebinarsByParticipant: Should throw Exception when participant is not found")
    void findAllWebinarsByParticipant_ThrowsEntityNotFoundException_WhenParticipantNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.findAllWebinarsByParticipant(participantUuid, pageable));

        verify(userRepository).findByUuidAndDeletedAtIsNull(participantUuid);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("findWebinarByUuid: Should return Webinar when found")
    void findWebinarByUuid_Success() throws Exception {
        WebinarReadOnlyDTO readOnlyDTO = mock(WebinarReadOnlyDTO.class);
        when(readOnlyDTO.uuid()).thenReturn(webinarUuid);

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid)).thenReturn(Optional.of(webinar));
        when(webinarMapper.mapToWebinarReadOnlyDTO(webinar)).thenReturn(readOnlyDTO);

        WebinarReadOnlyDTO result = webinarService.findWebinarByUuid(webinarUuid);

        assertNotNull(result);
        assertEquals(webinarUuid, result.uuid());
    }

    @Test
    @DisplayName("findWebinarByUuid: Should throw Exception when webinar is not found")
    void findWebinarByUuid_ThrowsEntityNotFoundException_WhenWebinarNotFound() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.findWebinarByUuid(webinarUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verifyNoInteractions(webinarMapper);
    }

    // ==========================================
    // TESTS-UPDATE/DELETE/ENROLL
    // ==========================================

    @Test
    @DisplayName("updateWebinar: Should throw Exception when DTO is null")
    void updateWebinar_ThrowsEntityInvalidArgumentException_WhenDtoIsNull() {
        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.updateWebinar(webinarUuid, null));

        verifyNoInteractions(webinarRepository);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("updateWebinar: Should throw Exception when title is blank")
    void updateWebinar_ThrowsEntityInvalidArgumentException_WhenTitleIsBlank() {
        WebinarEditDTO dto = mock(WebinarEditDTO.class);
        when(dto.title()).thenReturn("   ");
        when(dto.duration()).thenReturn(150);

        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.updateWebinar(webinarUuid, dto));

        verifyNoInteractions(webinarRepository);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("updateWebinar: Should throw Exception when duration is invalid")
    void updateWebinar_ThrowsEntityInvalidArgumentException_WhenDurationIsInvalid() {
        WebinarEditDTO dto = mock(WebinarEditDTO.class);
        when(dto.title()).thenReturn("Updated Webinar Title");
        when(dto.duration()).thenReturn(10);

        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.updateWebinar(webinarUuid, dto));

        verifyNoInteractions(webinarRepository);
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("updateWebinar: Should throw Exception when webinar is not found")
    void updateWebinar_ThrowsEntityNotFoundException_WhenWebinarNotFound() {
        WebinarEditDTO dto = mock(WebinarEditDTO.class);
        when(dto.title()).thenReturn("Updated Webinar Title");
        when(dto.duration()).thenReturn(150);

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.updateWebinar(webinarUuid, dto));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(webinarRepository, never()).save(any());
        verifyNoInteractions(webinarMapper);
    }

    @Test
    @DisplayName("updateWebinar: Should throw Exception when new title already exists")
    void updateWebinar_ThrowsEntityAlreadyExistsException_WhenNewTitleAlreadyExists() {
        WebinarEditDTO dto = mock(WebinarEditDTO.class);
        when(dto.title()).thenReturn("Updated Webinar Title");
        when(dto.duration()).thenReturn(150);

        Webinar existingWebinar = new Webinar();
        existingWebinar.setUuid(UUID.randomUUID());
        existingWebinar.setTitle("Updated Webinar Title");

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(webinarRepository.findByTitleAndDeletedAtIsNull("Updated Webinar Title"))
                .thenReturn(Optional.of(existingWebinar));

        assertThrows(EntityAlreadyExistsException.class,
                () -> webinarService.updateWebinar(webinarUuid, dto));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(webinarRepository).findByTitleAndDeletedAtIsNull("Updated Webinar Title");
        verify(webinarMapper, never()).mapToWebinarEditDTO(any(), any());
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateWebinar: Should not check duplicate title when title is same ignoring case")
    void updateWebinar_ShouldNotCheckDuplicateTitle_WhenTitleIsSameIgnoringCase() throws Exception {
        WebinarEditDTO dto = mock(WebinarEditDTO.class);
        when(dto.title()).thenReturn("spring boot advanced");
        when(dto.duration()).thenReturn(150);

        WebinarReadOnlyDTO readOnlyDTO = mock(WebinarReadOnlyDTO.class);
        when(readOnlyDTO.title()).thenReturn("spring boot advanced");

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(webinarRepository.save(webinar))
                .thenReturn(webinar);

        when(webinarMapper.mapToWebinarReadOnlyDTO(webinar))
                .thenReturn(readOnlyDTO);

        WebinarReadOnlyDTO result = webinarService.updateWebinar(webinarUuid, dto);

        assertNotNull(result);
        assertEquals("spring boot advanced", result.title());

        verify(webinarRepository, never()).findByTitleAndDeletedAtIsNull(anyString());
        verify(webinarMapper).mapToWebinarEditDTO(webinar, dto);
        verify(webinarRepository).save(webinar);
    }

    @Test
    @DisplayName("updateWebinar: Should update Webinar successfully")
    void updateWebinar_Success() throws Exception {
        WebinarEditDTO dto = mock(WebinarEditDTO.class);
        when(dto.title()).thenReturn("Updated Webinar Title");
        when(dto.duration()).thenReturn(150);

        WebinarReadOnlyDTO readOnlyDTO = mock(WebinarReadOnlyDTO.class);
        when(readOnlyDTO.title()).thenReturn("Updated Webinar Title");

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid)).thenReturn(Optional.of(webinar));
        when(webinarRepository.findByTitleAndDeletedAtIsNull("Updated Webinar Title")).thenReturn(Optional.empty());
        when(webinarRepository.save(webinar)).thenReturn(webinar);
        when(webinarMapper.mapToWebinarReadOnlyDTO(webinar)).thenReturn(readOnlyDTO);

        WebinarReadOnlyDTO result = webinarService.updateWebinar(webinarUuid, dto);

        assertNotNull(result);
        assertEquals("Updated Webinar Title", result.title());

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(webinarRepository).findByTitleAndDeletedAtIsNull("Updated Webinar Title");
        verify(webinarMapper).mapToWebinarEditDTO(webinar, dto);
        verify(webinarRepository).save(webinar);
        verify(webinarMapper).mapToWebinarReadOnlyDTO(webinar);
    }

    @Test
    @DisplayName("softDeleteWebinarByUuid: Should perform soft delete successfully")
    void softDeleteWebinarByUuid_Success() throws Exception {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid)).thenReturn(Optional.of(webinar));

        webinarService.softDeleteWebinarByUuid(webinarUuid);

        verify(webinarRepository, times(1)).save(webinar);
        assertNotNull(webinar.getDeletedAt());

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
    }

    @Test
    @DisplayName("softDeleteWebinarByUuid: Should throw Exception when webinar is not found")
    void softDeleteWebinarByUuid_ThrowsEntityNotFoundException_WhenWebinarNotFound() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.softDeleteWebinarByUuid(webinarUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("enrollUserInWebinar: Should successfully enroll a user to a webinar")
    void enrollUserInWebinar_Success() throws Exception {
        Webinar spyWebinar = spy(webinar);

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid)).thenReturn(Optional.of(spyWebinar));
        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid)).thenReturn(Optional.of(participant));

        webinarService.enrollUserInWebinar(webinarUuid, participantUuid);

        verify(spyWebinar, times(1)).addParticipant(participant);
    }

    @Test
    @DisplayName("enrollUserInWebinar: Should throw Exception when webinar is not found")
    void enrollUserInWebinar_ThrowsEntityNotFoundException_WhenWebinarNotFound() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.enrollUserInWebinar(webinarUuid, participantUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verifyNoInteractions(userRepository);
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("enrollUserInWebinar: Should throw Exception when user is not found")
    void enrollUserInWebinar_ThrowsEntityNotFoundException_WhenUserNotFound() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.enrollUserInWebinar(webinarUuid, participantUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(userRepository).findByUuidAndDeletedAtIsNull(participantUuid);
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("enrollUserInWebinar: Should throw Exception when organizer tries to enroll in own webinar")
    void enrollUserInWebinar_ThrowsEntityInvalidArgumentException_WhenOrganizerEnrollsInOwnWebinar() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(userRepository.findByUuidAndDeletedAtIsNull(organizerUuid))
                .thenReturn(Optional.of(organizer));

        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.enrollUserInWebinar(webinarUuid, organizerUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(userRepository).findByUuidAndDeletedAtIsNull(organizerUuid);
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("enrollUserInWebinar: Should throw Exception when user is already enrolled")
    void enrollUserInWebinar_ThrowsEntityAlreadyExistsException_WhenUserAlreadyEnrolled() {
        webinar.addParticipant(participant);

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid))
                .thenReturn(Optional.of(participant));

        assertThrows(EntityAlreadyExistsException.class,
                () -> webinarService.enrollUserInWebinar(webinarUuid, participantUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(userRepository).findByUuidAndDeletedAtIsNull(participantUuid);
        verify(webinarRepository, never()).save(any());
    }

    // ==========================================
    // TESTS-UNENROLL
    // ==========================================

    @Test
    @DisplayName("unenrollUserFromWebinar: Should unenroll user successfully")
    void unenrollUserFromWebinar_Success() throws Exception {
        webinar.addParticipant(participant);

        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid))
                .thenReturn(Optional.of(participant));

        webinarService.unenrollUserFromWebinar(webinarUuid, participantUuid);

        assertFalse(webinar.hasParticipant(participant));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(userRepository).findByUuidAndDeletedAtIsNull(participantUuid);
        verify(webinarRepository).save(webinar);
    }

    @Test
    @DisplayName("unenrollUserFromWebinar: Should throw Exception when webinar is not found")
    void unenrollUserFromWebinar_ThrowsEntityNotFoundException_WhenWebinarNotFound() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.unenrollUserFromWebinar(webinarUuid, participantUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verifyNoInteractions(userRepository);
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("unenrollUserFromWebinar: Should throw Exception when user is not found")
    void unenrollUserFromWebinar_ThrowsEntityNotFoundException_WhenUserNotFound() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> webinarService.unenrollUserFromWebinar(webinarUuid, participantUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(userRepository).findByUuidAndDeletedAtIsNull(participantUuid);
        verify(webinarRepository, never()).save(any());
    }

    @Test
    @DisplayName("unenrollUserFromWebinar: Should throw Exception when user is not enrolled")
    void unenrollUserFromWebinar_ThrowsEntityInvalidArgumentException_WhenUserIsNotEnrolled() {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid))
                .thenReturn(Optional.of(webinar));

        when(userRepository.findByUuidAndDeletedAtIsNull(participantUuid))
                .thenReturn(Optional.of(participant));

        assertThrows(EntityInvalidArgumentException.class,
                () -> webinarService.unenrollUserFromWebinar(webinarUuid, participantUuid));

        verify(webinarRepository).findByUuidAndDeletedAtIsNull(webinarUuid);
        verify(userRepository).findByUuidAndDeletedAtIsNull(participantUuid);
        verify(webinarRepository, never()).save(any());
    }
}