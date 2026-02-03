package com.fepbox.utility.model;

import java.util.UUID;

public record TpaRequest(UUID sender, UUID target, long expiresAt, boolean here) {}
