package cl.duocuc.blackout.rabbitadmin.service;

import cl.duocuc.blackout.rabbitadmin.dto.BindingRequest;
import cl.duocuc.blackout.rabbitadmin.dto.ExchangeRequest;
import cl.duocuc.blackout.rabbitadmin.dto.OperationResponse;
import cl.duocuc.blackout.rabbitadmin.dto.QueueRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdministrationService {
    private final AmqpAdmin admin;

    public RabbitAdministrationService(AmqpAdmin admin) {
        this.admin = admin;
    }

    public OperationResponse createQueue(QueueRequest request) {
        Map<String, Object> arguments = new HashMap<>();
        putIfPresent(arguments, "x-dead-letter-exchange", request.deadLetterExchange());
        putIfPresent(arguments, "x-dead-letter-routing-key", request.deadLetterRoutingKey());
        Queue queue = new Queue(request.name(), valueOr(request.durable(), true),
            valueOr(request.exclusive(), false), valueOr(request.autoDelete(), false), arguments);
        admin.declareQueue(queue);
        return new OperationResponse("queue", request.name(), "created");
    }

    public OperationResponse deleteQueue(String name) {
        boolean deleted = admin.deleteQueue(name);
        return new OperationResponse("queue", name, deleted ? "deleted" : "not-found");
    }

    public OperationResponse createExchange(ExchangeRequest request) {
        boolean durable = valueOr(request.durable(), true);
        boolean autoDelete = valueOr(request.autoDelete(), false);
        Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        AbstractExchange exchange = switch (request.type()) {
            case DIRECT -> new DirectExchange(request.name(), durable, autoDelete, arguments);
            case TOPIC -> new TopicExchange(request.name(), durable, autoDelete, arguments);
            case FANOUT -> new FanoutExchange(request.name(), durable, autoDelete, arguments);
        };
        admin.declareExchange(exchange);
        return new OperationResponse("exchange", request.name(), "created");
    }

    public OperationResponse deleteExchange(String name) {
        boolean deleted = admin.deleteExchange(name);
        return new OperationResponse("exchange", name, deleted ? "deleted" : "not-found");
    }

    public OperationResponse createBinding(BindingRequest request) {
        admin.declareBinding(toBinding(request));
        return new OperationResponse("binding", request.destination(), "created");
    }

    public OperationResponse deleteBinding(BindingRequest request) {
        admin.removeBinding(toBinding(request));
        return new OperationResponse("binding", request.destination(), "deleted");
    }

    private Binding toBinding(BindingRequest request) {
        Binding.DestinationType type = request.destinationType() == cl.duocuc.blackout.rabbitadmin.dto.DestinationType.QUEUE
            ? Binding.DestinationType.QUEUE : Binding.DestinationType.EXCHANGE;
        return new Binding(request.destination(), type, request.sourceExchange(), request.routingKey(),
            request.arguments() == null ? Map.of() : request.arguments());
    }

    private static boolean valueOr(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    private static void putIfPresent(Map<String, Object> arguments, String key, String value) {
        if (value != null && !value.isBlank()) arguments.put(key, value);
    }
}

