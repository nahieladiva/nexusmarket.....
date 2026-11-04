package application.support;

import application.domain.ports.out.PasswordEncoderPort;

public class FakePasswordEncoder implements PasswordEncoderPort {
    @Override public String encode(String raw) { return "hashed:" + raw; }
    @Override public boolean matches(String raw, String encoded) { return encoded.equals("hashed:" + raw); }
}
