package br.com.armange.commons.object.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.armange.commons.object.impl.artifact.ConvertibleBeanArtifact;

public class BeanConverterImplTest {

    @Test
    public void anyConvertibleBeanIsMatchingWithItsConverter() {
        final BeanConverterImpl<ConvertibleBeanArtifact, ConvertibleBeanArtifact> converter = new BeanConverterImpl<>();
        
        assertTrue(converter.matches(new ConvertibleBeanArtifact()));
    }
}
