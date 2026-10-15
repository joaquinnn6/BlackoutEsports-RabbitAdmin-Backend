package cl.duocuc.blackout.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record ExchangeRequest(
    @NotBlank @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._-]+") String name,
    @NotNull ExchangeType type,
    Boolean durable,
    Boolean autoDelete,
    Map<String, Object> arguments
) { }

