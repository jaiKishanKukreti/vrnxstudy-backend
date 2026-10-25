package com.vrnxstudy.vrnxstudy_api.dto.auth;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String email,
        String fullName,
        String status
) {}
