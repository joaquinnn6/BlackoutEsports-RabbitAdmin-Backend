package cl.duocuc.blackout.rabbitadmin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duocuc.blackout.rabbitadmin.dto.BindingRequest;
import cl.duocuc.blackout.rabbitadmin.dto.DestinationType;
import cl.duocuc.blackout.rabbitadmin.dto.ExchangeRequest;
import cl.duocuc.blackout.rabbitadmin.dto.ExchangeType;
import cl.duocuc.blackout.rabbitadmin.dto.QueueRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

@ExtendWith(MockitoExtension.class)
class RabbitAdministrationServiceTest {
    @Mock
    private AmqpAdmin admin;

    @InjectMocks
    private RabbitAdministrationService service;

    @Test
    void createsDurableQueueWithDeadLetterConfiguration() {
        service.createQueue(new QueueRequest("fan.notifications.queue", null, null, null,
            "blackout.dead-letter.exchange", "fan.notifications.failed"));

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(admin).declareQueue(captor.capture());
        Queue queue = captor.getValue();
        assertThat(queue.getName()).isEqualTo("fan.notifications.queue");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
            .containsEntry("x-dead-letter-exchange", "blackout.dead-letter.exchange")
            .containsEntry("x-dead-letter-routing-key", "fan.notifications.failed");
    }

    @Test
    void createsTopicExchange() {
        service.createExchange(new ExchangeRequest("blackout.events.exchange", ExchangeType.TOPIC,
            true, false, Map.of("alternate-exchange", "fallback")));

        ArgumentCaptor<TopicExchange> captor = ArgumentCaptor.forClass(TopicExchange.class);
        verify(admin).declareExchange(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("blackout.events.exchange");
        assertThat(captor.getValue().getArguments()).containsEntry("alternate-exchange", "fallback");
    }

    @Test
    void createsAndRemovesQueueBinding() {
        BindingRequest request = new BindingRequest("fan.notifications.queue", DestinationType.QUEUE,
            "blackout.events.exchange", "tournament.#", Map.of());

        service.createBinding(request);
        service.deleteBinding(request);

        ArgumentCaptor<Binding> created = ArgumentCaptor.forClass(Binding.class);
        ArgumentCaptor<Binding> removed = ArgumentCaptor.forClass(Binding.class);
        verify(admin).declareBinding(created.capture());
        verify(admin).removeBinding(removed.capture());
        assertThat(created.getValue().getRoutingKey()).isEqualTo("tournament.#");
        assertThat(removed.getValue().getDestination()).isEqualTo(created.getValue().getDestination());
        assertThat(removed.getValue().getExchange()).isEqualTo(created.getValue().getExchange());
        assertThat(removed.getValue().getRoutingKey()).isEqualTo(created.getValue().getRoutingKey());
    }

    @Test
    void reportsWhetherResourcesWereDeleted() {
        when(admin.deleteQueue("missing.queue")).thenReturn(false);
        when(admin.deleteExchange("blackout.events.exchange")).thenReturn(true);

        assertThat(service.deleteQueue("missing.queue").status()).isEqualTo("not-found");
        assertThat(service.deleteExchange("blackout.events.exchange").status()).isEqualTo("deleted");
    }
}

