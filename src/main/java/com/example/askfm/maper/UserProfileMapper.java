package com.example.askfm.maper;

import com.example.askfm.dto.ProfileEditDTO;
import com.example.askfm.model.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfile toEntity(ProfileEditDTO dto, UserProfile profile) {
        if (profile == null) {
            profile = new UserProfile();
        }

        profile.setBio(dto.getBio());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setLocation(dto.getLocation());
        profile.setEducation(dto.getEducation());
        profile.setWebsite(dto.getWebsite());
        profile.setOccupation(dto.getOccupation());
        profile.setInterests(dto.getInterests());

        return profile;
    }

    public ProfileEditDTO toDto(UserProfile profile) {
        if (profile == null) {
            return new ProfileEditDTO();
        }

        return ProfileEditDTO.builder()
                .bio(profile.getBio())
                .dateOfBirth(profile.getDateOfBirth())
                .location(profile.getLocation())
                .education(profile.getEducation())
                .website(profile.getWebsite())
                .occupation(profile.getOccupation())
                .interests(profile.getInterests())
                .build();
    }
}