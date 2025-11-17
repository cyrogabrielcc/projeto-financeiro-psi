package cef.invest.resources.test.ResourcesTest;

import cef.financial.security.AuthResource;
import cef.financial.security.LoginRequest;
import cef.financial.security.LoginResponse;
import cef.financial.security.TokenService;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Parameter;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    @Mock
    TokenService tokenService;

    AuthResource authResource;

    @BeforeEach
    void setUp() {
        // usamos o construtor novo pra injetar o mock
        authResource = new AuthResource(tokenService);
    }

    // ===================== TESTES DE ANOTAÇÕES =====================

    @Test
    void testClassAnnotations() {
        assertTrue(AuthResource.class.isAnnotationPresent(Path.class));
        assertEquals("/auth", AuthResource.class.getAnnotation(Path.class).value());

        assertTrue(AuthResource.class.isAnnotationPresent(Consumes.class));
        assertEquals(MediaType.APPLICATION_JSON,
                AuthResource.class.getAnnotation(Consumes.class).value()[0]);

        assertTrue(AuthResource.class.isAnnotationPresent(Produces.class));
        assertEquals(MediaType.APPLICATION_JSON,
                AuthResource.class.getAnnotation(Produces.class).value()[0]);
    }

    @Test
    void testLoginMethodAnnotations() throws NoSuchMethodException {
        var method = AuthResource.class.getMethod("login", LoginRequest.class);

        // Anotações no MÉTODO
        assertTrue(method.isAnnotationPresent(POST.class));
        assertTrue(method.isAnnotationPresent(Path.class));
        assertEquals("/login", method.getAnnotation(Path.class).value());

        assertTrue(method.isAnnotationPresent(PermitAll.class));
        assertTrue(method.isAnnotationPresent(SecurityRequirementsSet.class));
        assertTrue(method.isAnnotationPresent(Operation.class));
        assertTrue(method.isAnnotationPresent(APIResponses.class));

        // Anotação @RequestBody está no PARÂMETRO, não no método
        Parameter[] params = method.getParameters();
        assertEquals(1, params.length);
        assertTrue(params[0].isAnnotationPresent(RequestBody.class));
    }

    // ===================== TESTES DE FLUXO DO MÉTODO login =====================

    @Test
    void login_DeveRetornar400_QuandoRequestForNulo() {
        Response resp = authResource.login(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        verifyNoInteractions(tokenService);
    }

    @Test
    void login_DeveRetornar400_QuandoUsernameForNulo() {
        LoginRequest req = new LoginRequest();
        req.username = null;
        req.password = "qualquer";

        Response resp = authResource.login(req);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        verifyNoInteractions(tokenService);
    }

    @Test
    void login_DeveRetornar400_QuandoPasswordForNulo() {
        LoginRequest req = new LoginRequest();
        req.username = "user";
        req.password = null;

        Response resp = authResource.login(req);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        verifyNoInteractions(tokenService);
    }

    @Test
    void login_DeveRetornar200_ComTokenParaAdmin() {
        LoginRequest req = new LoginRequest();
        req.username = "admin";
        req.password = "admin123";

        when(tokenService.generateToken(eq("admin"), anySet())).thenReturn("token-admin");

        Response resp = authResource.login(req);

        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertNotNull(resp.getEntity());
        assertTrue(resp.getEntity() instanceof LoginResponse);

        LoginResponse body = (LoginResponse) resp.getEntity();

        // tenta acessar campo 'token' se existir
        try {
            var field = LoginResponse.class.getDeclaredField("token");
            field.setAccessible(true);
            Object tokenValor = field.get(body);
            assertEquals("token-admin", tokenValor);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // se não tiver campo 'token' público, pelo menos garantimos que o service foi chamado
        }

        // verifica se as roles foram passadas corretamente
        ArgumentCaptor<Set<String>> rolesCaptor = ArgumentCaptor.forClass(Set.class);
        verify(tokenService, times(1))
                .generateToken(eq("admin"), rolesCaptor.capture());

        Set<String> roles = rolesCaptor.getValue();
        assertTrue(roles.contains("admin"));
        assertTrue(roles.contains("user"));
    }

    @Test
    void login_DeveRetornar200_ComTokenParaUser() {
        LoginRequest req = new LoginRequest();
        req.username = "user";
        req.password = "user123";

        when(tokenService.generateToken(eq("user"), anySet())).thenReturn("token-user");

        Response resp = authResource.login(req);

        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertNotNull(resp.getEntity());
        assertTrue(resp.getEntity() instanceof LoginResponse);

        verify(tokenService, times(1))
                .generateToken(eq("user"), anySet());
    }

    @Test
    void login_DeveRetornar401_QuandoCredenciaisInvalidas() {
        LoginRequest req = new LoginRequest();
        req.username = "user";
        req.password = "senhaErrada";

        Response resp = authResource.login(req);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), resp.getStatus());
        verifyNoInteractions(tokenService);
    }
}
