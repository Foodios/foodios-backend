package vn.com.orchestration.foodios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final Map<String, String> HEADER_EXAMPLES = Map.of(
            "X-Request-Id", "123e4567-e89b-12d3-a456-426614174000",
            "X-Request-DateTime", "2026-04-08T10:30:00.000+07:00",
            "X-Channel", "ONL"
    );

    @Bean
    public OpenAPI foodiosOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Foodios API").version("v1"))
                .components(new Components().addSecuritySchemes(
                        BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            ensureEnvelopeHeaders(operation);
            if (requiresAuthorization(handlerMethod)) {
                operation.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
            }
            return operation;
        };
    }

    private void ensureEnvelopeHeaders(io.swagger.v3.oas.models.Operation operation) {
        List<Parameter> parameters = operation.getParameters() == null
                ? new ArrayList<>()
                : new ArrayList<>(operation.getParameters());

        addHeaderIfMissing(parameters, "X-Request-Id", true, "Request correlation id");
        addHeaderIfMissing(parameters, "X-Request-DateTime", true, "Request timestamp in ISO-8601 format");
        addHeaderIfMissing(parameters, "X-Channel", true, "Request channel, for example ONL or OFF");

        operation.setParameters(parameters);
    }

    private void addHeaderIfMissing(List<Parameter> parameters, String name, boolean required, String description) {
        boolean exists = parameters.stream().anyMatch(parameter -> name.equalsIgnoreCase(parameter.getName()));
        if (exists) {
            return;
        }

        parameters.add(new Parameter()
                .in("header")
                .name(name)
                .required(required)
                .description(description)
                .example(HEADER_EXAMPLES.get(name)));
    }

    private boolean requiresAuthorization(HandlerMethod handlerMethod) {
        String[] publicPrefixes = {
                "/api/v1/authentication",
                "/api/v1/public",
                "/actuator",
                "/error"
        };

        for (String pattern : handlerMethod.getMethodAnnotation(org.springframework.web.bind.annotation.RequestMapping.class) != null
                ? handlerMethod.getMethodAnnotation(org.springframework.web.bind.annotation.RequestMapping.class).value()
                : new String[0]) {
            if (startsWithAny(pattern, publicPrefixes)) {
                return false;
            }
        }

        String classLevelPath = "";
        org.springframework.web.bind.annotation.RequestMapping classMapping =
                handlerMethod.getBeanType().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            classLevelPath = classMapping.value()[0];
        }

        String methodPath = resolveMethodPath(handlerMethod);
        return !startsWithAny(classLevelPath + methodPath, publicPrefixes);
    }

    private String resolveMethodPath(HandlerMethod handlerMethod) {
        GetMappingData mapping = GetMappingData.from(handlerMethod);
        return mapping.path();
    }

    private boolean startsWithAny(String path, String[] prefixes) {
        for (String prefix : prefixes) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private record GetMappingData(String path) {
        private static GetMappingData from(HandlerMethod handlerMethod) {
            if (handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.GetMapping.class)) {
                return new GetMappingData(resolvePath(handlerMethod, org.springframework.web.bind.annotation.GetMapping.class));
            }
            if (handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.PostMapping.class)) {
                return new GetMappingData(resolvePath(handlerMethod, org.springframework.web.bind.annotation.PostMapping.class));
            }
            if (handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.PutMapping.class)) {
                return new GetMappingData(resolvePath(handlerMethod, org.springframework.web.bind.annotation.PutMapping.class));
            }
            if (handlerMethod.hasMethodAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class)) {
                return new GetMappingData(resolvePath(handlerMethod, org.springframework.web.bind.annotation.DeleteMapping.class));
            }
            org.springframework.web.bind.annotation.RequestMapping mapping =
                    handlerMethod.getMethodAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
            if (mapping != null && mapping.value().length > 0) {
                return new GetMappingData(mapping.value()[0]);
            }
            return new GetMappingData("");
        }

        private static <A extends java.lang.annotation.Annotation> String resolvePath(HandlerMethod handlerMethod, Class<A> annotationType) {
            A annotation = handlerMethod.getMethodAnnotation(annotationType);
            if (annotation instanceof org.springframework.web.bind.annotation.GetMapping getMapping) {
                return firstPath(getMapping.value(), getMapping.path());
            }
            if (annotation instanceof org.springframework.web.bind.annotation.PostMapping postMapping) {
                return firstPath(postMapping.value(), postMapping.path());
            }
            if (annotation instanceof org.springframework.web.bind.annotation.PutMapping putMapping) {
                return firstPath(putMapping.value(), putMapping.path());
            }
            if (annotation instanceof org.springframework.web.bind.annotation.DeleteMapping deleteMapping) {
                return firstPath(deleteMapping.value(), deleteMapping.path());
            }
            return "";
        }

        private static String firstPath(String[] values, String[] paths) {
            if (values.length > 0) {
                return values[0];
            }
            if (paths.length > 0) {
                return paths[0];
            }
            return "";
        }
    }
}
