package cl.duocuc.blackout.rabbitadmin.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException error) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revisa los campos enviados");
        Map<String, String> fields = new LinkedHashMap<>();
        error.getBindingResult().getFieldErrors()
            .forEach(field -> fields.put(field.getField(), field.getDefaultMessage()));
        result.setProperty("errors", fields);
        return result;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail constraint(ConstraintViolationException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Nombre de recurso inválido");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail malformed(HttpMessageNotReadableException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "JSON o tipo de recurso inválido");
    }

    @ExceptionHandler(AmqpException.class)
    ProblemDetail broker(AmqpException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "RabbitMQ no pudo completar la operación");
    }
}

