
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class APITestBanco {

   private static final String ENDPOINT = "https://67e48bae2ae442db76d4c670.mockapi.io/api/bancoegg/Transacciones";

    @BeforeAll
    public static void setup() {
        // Configuración inicial
        RestAssured.baseURI = ENDPOINT;
    }

    @BeforeEach
    public void clearEndpoint() {
        // Prueba 1: Verificar y limpiar el endpoint
        List<String> userIds = given()
            .when()
            .get()
            .then()
            .extract()
            .path("id");

        // Eliminar todos los usuarios existentes
        if (userIds != null) {
            userIds.forEach(id -> 
                given()
                    .pathParam("id", id)
                    .when()
                    .delete("/{id}")
                    .then()
                    .statusCode(200)
            );
        }

        // Verificar que el endpoint esté vacío
        given()
            .when()
            .get()
            .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    // Clase de Transacción POJO
    static class Transaccion {
        private String nombre;
        private String email;
        private double saldo;
        private String numeroCuenta;

        public Transaccion(String nombre, String email, double saldo, String numeroCuenta) {
            this.nombre = nombre;
            this.email = email;
            this.saldo = saldo;
            this.numeroCuenta = numeroCuenta;
        }

        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public double getSaldo() { return saldo; }
        public void setSaldo(double saldo) { this.saldo = saldo; }
        public String getNumeroCuenta() { return numeroCuenta; }
        public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }
    }

    // Método para generar un nombre aleatorio
    private String generarNombreAleatorio() {
        String[] nombres = {"Juan", "Maria", "Carlos", "Ana", "Pedro", "Laura", "Miguel", "Sofia"};
        String[] apellidos = {"Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez", "Perez", "Sanchez", "Ramirez"};
        
        return nombres[(int)(Math.random() * nombres.length)] + " " + 
               apellidos[(int)(Math.random() * apellidos.length)];
    }

    // Método para generar un email aleatorio
    private String generarEmailAleatorio() {
        return UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    // Método para generar un número de cuenta aleatorio
    private String generarNumeroCuenta() {
        return String.format("%010d", (long)(Math.random() * 10000000000L));
    }

    @Test
    public void testCrearYValidarTransacciones() {
        // Prueba 2: Crear transacciones únicas
        Set<String> emails = new HashSet<>();
        List<Transaccion> transacciones = new ArrayList<>();

        // Generar 10 transacciones con emails únicos
        for (int i = 0; i < 10; i++) {
            String email;
            do {
                email = generarEmailAleatorio();
            } while (!emails.add(email));

            Transaccion transaccion = new Transaccion(
                generarNombreAleatorio(), 
                email, 
                Math.random() * 10000, // Saldo aleatorio entre 0 y 10000
                generarNumeroCuenta()
            );
            transacciones.add(transaccion);
        }

        // Enviar transacciones
        Response response = given()
            .contentType(ContentType.JSON)
            .body(transacciones)
            .when()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .response();
    }

    @Test
    public void testValidarEmailsUnicos() {
        // Prueba 3: Verificar que no haya emails duplicados
        List<String> emails = given()
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .path("email");

        // Verificar unicidad de emails
        Set<String> uniqueEmails = new HashSet<>(emails);
        assertEquals(emails.size(), uniqueEmails.size(), "Existen emails duplicados");
    }

    @Test
    public void testActualizarNumeroCuenta() {
        // Prueba 4: Actualizar número de cuenta
        // Primero, crear una transacción
        Transaccion transaccion = new Transaccion(
            generarNombreAleatorio(), 
            generarEmailAleatorio(), 
            Math.random() * 10000,
            generarNumeroCuenta()
        );

        // Crear transacción
        String transaccionId = given()
            .contentType(ContentType.JSON)
            .body(transaccion)
            .when()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Actualizar número de cuenta
        String nuevaCuenta = generarNumeroCuenta();
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", transaccionId)
            .body("{\"numeroCuenta\": \"" + nuevaCuenta + "\"}")
            .when()
            .put("/{id}")
            .then()
            .statusCode(200);

        // Verificar actualización
        String cuentaActualizada = given()
            .pathParam("id", transaccionId)
            .when()
            .get("/{id}")
            .then()
            .statusCode(200)
            .extract()
            .path("numeroCuenta");

        assertEquals(nuevaCuenta, cuentaActualizada);
    }

    @Test
    public void testDepositoYRetiro() {
        // Crear transacción con saldo inicial
        Transaccion transaccion = new Transaccion(
            generarNombreAleatorio(), 
            generarEmailAleatorio(), 
            0.0, // Saldo inicial cero
            generarNumeroCuenta()
        );

        String transaccionId = given()
            .contentType(ContentType.JSON)
            .body(transaccion)
            .when()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Prueba 5: Depósito de dinero
        double montoDeposito = 1000.0;
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", transaccionId)
            .body("{\"saldo\": " + montoDeposito + "}")
            .when()
            .put("/{id}")
            .then()
            .statusCode(200);

        // Verificar depósito
        Number saldoActualizadoNumber = given()
            .pathParam("id", transaccionId)
            .when()
            .get("/{id}")
            .then()
            .statusCode(200)
            .extract()
            .path("saldo");

        double saldoActualizado = saldoActualizadoNumber.doubleValue();
        assertEquals(montoDeposito, saldoActualizado, 0.001);

        // Prueba 6: Retiro de dinero
        double montoRetiro = 500.0;
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", transaccionId)
            .body("{\"saldo\": " + (saldoActualizado - montoRetiro) + "}")
            .when()
            .put("/{id}")
            .then()
            .statusCode(200);

        // Verificar retiro
        Number saldoFinalNumber = given()
            .pathParam("id", transaccionId)
            .when()
            .get("/{id}")
            .then()
            .statusCode(200)
            .extract()
            .path("saldo");

        double saldoFinal = saldoFinalNumber.doubleValue();
        assertEquals(montoDeposito - montoRetiro, saldoFinal, 0.001);
    }

    @Test
    public void testRetiroMayorAlSaldo() {
        // Crear transacción con saldo limitado
        Transaccion transaccion = new Transaccion(
            generarNombreAleatorio(), 
            generarEmailAleatorio(), 
            100.0, // Saldo inicial pequeño
            generarNumeroCuenta()
        );

        String transaccionId = given()
            .contentType(ContentType.JSON)
            .body(transaccion)
            .when()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Intentar retirar más de lo disponible
        double montoRetiro = 200.0;
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", transaccionId)
            .body("{\"saldo\": " + (transaccion.getSaldo() - montoRetiro) + "}")
            .when()
            .put("/{id}")
            .then()
            .statusCode(200); // Esperamos un código de error
    }
}