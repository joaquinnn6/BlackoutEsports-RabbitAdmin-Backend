package cl.duocuc.blackout.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record QueueRequest(
    @NotBlank @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._-]+") String name,
    Boolean durable,
    Boolean exclusive,
    Boolean autoDelete,
    @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._-]*") String deadLetterExchange,
    @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._#*-]*") String deadLetterRoutingKey
) { }

