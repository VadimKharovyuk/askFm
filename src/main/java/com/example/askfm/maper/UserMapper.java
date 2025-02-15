package com.example.askfm.maper;

import com.example.askfm.dto.*;
import com.example.askfm.model.BlockInfo;
import com.example.askfm.model.User;
import com.example.askfm.service.ImageService;
import com.example.askfm.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final ImageService imageService;
    private final SubscriptionService subscriptionService;

    public UserSearchDTO toSearchDTO(User user, String currentUsername) {
        return UserSearchDTO.builder()
                .username(user.getUsername())
                .avatar(user.getAvatar() != null ?
                        "data:image/jpeg;base64," + imageService.getBase64Avatar(user.getAvatar()) :
                        null)
                .bio(user.getProfile() != null ? user.getProfile().getBio() : null)
                .followersCount(subscriptionService.getSubscribersCount(user.getUsername()))
                .isFollowing(currentUsername != null &&
                        subscriptionService.isFollowing(currentUsername, user.getUsername()))
                .build();
    }

    public User toEntity(UserRegistrationDTO dto) {
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword()) //注意: пароль должен быть зашифрован в сервисе
                .build();
    }

    public BlockInfoDTO toBlockInfoDTO(BlockInfo blockInfo) {
        return BlockInfoDTO.builder()
                .id(blockInfo.getId())
                .blockerUsername(blockInfo.getBlocker().getUsername())
                .blockedUsername(blockInfo.getBlocked().getUsername())
                .blockedAt(blockInfo.getBlockedAt())
                .build();
    }

    public BlockedUserDTO toBlockedUserDTO(User blockedUser, BlockInfo blockInfo) {
        return BlockedUserDTO.builder()
                .username(blockedUser.getUsername())
                .avatar(blockedUser.getAvatar() != null ?
                        "data:image/jpeg;base64," + imageService.getBase64Avatar(blockedUser.getAvatar()) :
                        null)
                .blockedAt(blockInfo.getBlockedAt())
                .build();
    }

    public BlockInfo toBlockInfo(User blocker, User blocked) {
        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setBlocker(blocker);
        blockInfo.setBlocked(blocked);
        blockInfo.setBlockedAt(LocalDateTime.now());
        return blockInfo;
    }
    public AdminUserSearchDTO toAdminSearchDTO(User user) {
        return AdminUserSearchDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar() != null ?
                        "data:image/jpeg;base64," + imageService.getBase64Avatar(user.getAvatar()) :
                        null)
                .bio(user.getProfile() != null ? user.getProfile().getBio() : null)
                .followersCount(subscriptionService.getSubscribersCount(user.getUsername()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<AdminUserSearchDTO> toAdminSearchDTOList(List<User> users) {
        return users.stream()
                .map(this::toAdminSearchDTO)
                .collect(Collectors.toList());
    }


}
