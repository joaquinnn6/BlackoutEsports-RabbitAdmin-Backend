package cl.duocuc.blackout.rabbitadmin.controller;

import cl.duocuc.blackout.rabbitadmin.dto.BindingRequest;
import cl.duocuc.blackout.rabbitadmin.dto.ExchangeRequest;
import cl.duocuc.blackout.rabbitadmin.dto.OperationResponse;
import cl.duocuc.blackout.rabbitadmin.dto.QueueRequest;
import cl.duocuc.blackout.rabbitadmin.service.RabbitAdministrationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/rabbit")
public class RabbitAdministrationController {
    private final RabbitAdministrationService service;

    public RabbitAdministrationController(RabbitAdministrationService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una cola")
    OperationResponse createQueue(@Valid @RequestBody QueueRequest request) {
        return service.createQueue(request);
    }

    @DeleteMapping("/queues/{name}")
    @Operation(summary = "Eliminar una cola")
    OperationResponse deleteQueue(
        @PathVariable @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._-]+") String name) {
        return service.deleteQueue(name);
    }

    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un exchange")
    OperationResponse createExchange(@Valid @RequestBody ExchangeRequest request) {
        return service.createExchange(request);
    }

    @DeleteMapping("/exchanges/{name}")
    @Operation(summary = "Eliminar un exchange")
    OperationResponse deleteExchange(
        @PathVariable @Size(max = 255) @Pattern(regexp = "[A-Za-z0-9._-]+") String name) {
        return service.deleteExchange(name);
    }

    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un binding")
    OperationResponse createBinding(@Valid @RequestBody BindingRequest request) {
        return service.createBinding(request);
    }

    @DeleteMapping("/bindings")
    @Operation(summary = "Eliminar un binding")
    OperationResponse deleteBinding(@Valid @RequestBody BindingRequest request) {
        return service.deleteBinding(request);
    }
}

