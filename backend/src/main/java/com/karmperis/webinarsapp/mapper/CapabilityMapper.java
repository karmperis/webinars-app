package com.karmperis.webinarsapp.mapper;

import com.karmperis.webinarsapp.dto.CapabilityEditDTO;
import com.karmperis.webinarsapp.dto.CapabilityInsertDTO;
import com.karmperis.webinarsapp.dto.CapabilityReadOnlyDTO;
import com.karmperis.webinarsapp.model.Capability;
import org.springframework.stereotype.Component;

/**
 * Mapper component responsible for converting between {@link Capability} entities and Capability-related DTOs.
 */
@Component
public class CapabilityMapper {

    /**
     * Maps a {@link CapabilityInsertDTO} to a new {@link Capability} entity. (Insert)
     *
     * @param dto the DTO containing values for creating a capability
     * @return a new Capability entity populated from the DTO
     */
    public Capability mapToCapabilityEntity(CapabilityInsertDTO dto) {
        if (dto == null) return null;

        Capability capability = new Capability();
        capability.setName(dto.name());
        capability.setDescription(dto.description());

        return capability;
    }

    /**
     * Maps a {@link Capability} entity to a {@link CapabilityReadOnlyDTO}. (ReadOnly)
     *
     * @param capability the Capability entity to map
     * @return a read-only DTO representation of the given capability
     */
    public CapabilityReadOnlyDTO mapToCapabilityReadOnlyDTO(Capability capability) {
        if (capability == null) return null;

        return new CapabilityReadOnlyDTO(
                capability.getUuid(),
                capability.getName(),
                capability.getDescription()
        );
    }

    /**
     * Applies values from a {@link CapabilityEditDTO} to an existing {@link Capability} entity. (Edit)
     *
     * @param capability the Capability entity to update
     * @param dto        the DTO containing the updated values
     */
    public void mapToCapabilityEditDTO(Capability capability, CapabilityEditDTO dto) {
        if (capability == null || dto == null) return;

        capability.setName(dto.name());
        capability.setDescription(dto.description());
    }
}