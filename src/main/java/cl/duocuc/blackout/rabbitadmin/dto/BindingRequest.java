package cl.duocuc.blackout.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record BindingRequest(
    @NotBlank @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._-]+") String destination,
    @NotNull DestinationType destinationType,
    @NotBlank @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._-]+") String sourceExchange,
    @NotNull @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._#*-]*") String routingKey,
    Map<String, Object> arguments
) { }

