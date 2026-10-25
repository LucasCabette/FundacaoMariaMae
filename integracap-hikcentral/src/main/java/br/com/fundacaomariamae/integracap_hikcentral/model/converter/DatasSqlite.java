package br.com.fundacaomariamae.integracap_hikcentral.model.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Compatibilidade com texto ISO e milissegundos existentes no SQLite. */
final class DatasSqlite {
    private DatasSqlite() {}

    static LocalDateTime lerDataHora(String valor) {
        if (valor == null) return null;
        String texto = valor.strip();
        if (texto.matches("-?\\d+")) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(texto)), ZoneOffset.UTC);
        }
        if (texto.length() == 10) return LocalDate.parse(texto).atStartOfDay();
        return LocalDateTime.parse(texto.replace(' ', 'T'));
    }
}
