package com.karmperis.webinarsapp.service;

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
    @DisplayName("saveWebinar: Should throw Exception when title is invalid")
    void saveWebinar_ThrowsEntityInvalidArgumentException_WhenTitleIsTooShort() {
        WebinarInsertDTO dto = mock(WebinarInsertDTO.class);
        when(dto.title()).thenReturn("abc"); // Invalid length (< 5)
        when(dto.duration()).thenReturn(120);

        assertThrows(EntityInvalidArgumentException.class, () -> webinarService.saveWebinar(dto, organizerUuid));
        verify(webinarRepository, never()).save(any());
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

    // ==========================================
    // TESTS-UPDATE/DELETE/ENROLL
    // ==========================================

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
    }

    @Test
    @DisplayName("softDeleteWebinarByUuid: Should perform soft delete successfully")
    void softDeleteWebinarByUuid_Success() throws Exception {
        when(webinarRepository.findByUuidAndDeletedAtIsNull(webinarUuid)).thenReturn(Optional.of(webinar));

        webinarService.softDeleteWebinarByUuid(webinarUuid);

        verify(webinarRepository, times(1)).save(webinar);
        assertNotNull(webinar.getDeletedAt());
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
}