package br.com.armange.commons.example;

import br.com.armange.commons.object.api.typeconverter.TypeConverter;
import br.com.armange.commons.object.impl.typeconverter.string.StringToLongConverterImpl;

public final class ConsumerSmoke {
    private ConsumerSmoke() {
    }

    public static void main(final String[] args) {
        final TypeConverter<String, Long> converter = new StringToLongConverterImpl().from("42");

        if (!Long.valueOf(42L).equals(converter.to(Long.class))) {
            throw new AssertionError("StringToLongConverterImpl retornou um valor inesperado");
        }

        System.out.println("Consumo de commons-object-impl no JDK 25: OK");
    }
}
