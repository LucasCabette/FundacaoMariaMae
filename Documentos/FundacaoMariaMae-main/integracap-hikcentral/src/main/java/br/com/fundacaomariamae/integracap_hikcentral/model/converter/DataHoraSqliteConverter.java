package br.com.fundacaomariamae.integracap_hikcentral.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;

@Converter
public class DataHoraSqliteConverter implements AttributeConverter<LocalDateTime, String> {
    @Override
    public String convertToDatabaseColumn(LocalDateTime valor) {
        return valor == null ? null : valor.toString();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String valor) {
        return DatasSqlite.lerDataHora(valor);
    }
}
