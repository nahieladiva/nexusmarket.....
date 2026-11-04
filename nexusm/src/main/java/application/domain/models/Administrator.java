package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Administrador del marketplace: aprueba vendedores, gestiona bodegas y despacha pedidos.
 * No se registra por la API; se crea al arrancar la aplicación con las credenciales
 * configuradas.
 */
public class Administrator extends User {

    public Administrator(UserId id, String fullName, Email email, PhoneNumber phone,
                         String passwordHash, List<Address> addresses, LocalDateTime createdAt) {
        super(id, fullName, email, phone, UserRole.ADMIN, passwordHash, addresses, createdAt);
    }
}
