package cl.duocuc.blackout.rabbitadmin.config;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.util.Arrays;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;

public class TrustedEntraJwtDecoder implements JwtDecoder {
    private final SecurityProperties properties;
    private final JwtDecoder delegate;

    public TrustedEntraJwtDecoder(SecurityProperties properties) {
        this.properties = properties;
        this.delegate = new SupplierJwtDecoder(
            () -> NimbusJwtDecoder.withIssuerLocation(properties.entraIssuer()).build());
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        requireConfiguration();
        try {
            String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
            if (!properties.entraIssuer().equals(issuer)) throw new BadJwtException("Emisor no autorizado");
            Jwt jwt = delegate.decode(token);
            var result = JwtValidators.createDefaultWithIssuer(issuer).validate(jwt);
            String scope = jwt.getClaimAsString("scp");
            if (result.hasErrors() || jwt.getExpiresAt() == null || jwt.getSubject() == null ||
                jwt.getAudience() == null || !jwt.getAudience().contains(properties.entraAudience()) ||
                scope == null || !Arrays.asList(scope.split(" ")).contains(properties.entraScope())) {
                throw new BadJwtException("Token Entra ID inválido para esta API");
            }
            return jwt;
        } catch (ParseException | IllegalArgumentException exception) {
            throw new BadJwtException("Token inválido", exception);
        }
    }

    private void requireConfiguration() {
        if (!configured(properties.entraIssuer()) || !configured(properties.entraAudience()) ||
            !configured(properties.entraScope())) {
            throw new BadJwtException("Configuración de Entra ID incompleta");
        }
    }

    private static boolean configured(String value) {
        return value != null && !value.isBlank();
    }
}

