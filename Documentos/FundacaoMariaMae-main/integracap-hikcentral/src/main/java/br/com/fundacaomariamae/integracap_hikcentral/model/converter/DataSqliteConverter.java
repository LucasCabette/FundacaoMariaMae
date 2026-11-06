package br.com.fundacaomariamae.integracap_hikcentral.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

@Converter
public class DataSqliteConverter implements AttributeConverter<LocalDate, String> {
    @Override
    public String convertToDatabaseColumn(LocalDate valor) {
        return valor == null ? null : valor.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String valor) {
        var dataHora = DatasSqlite.lerDataHora(valor);
        return dataHora == null ? null : dataHora.toLocalDate();
    }
}
