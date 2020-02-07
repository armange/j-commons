package br.com.armange.commons.object.impl;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.armange.commons.object.impl.artifact.BeanArtifact;
import br.com.armange.commons.object.impl.artifact.ConvertibleBeanArtifact;

public class BeanConverterImplTest {

    @Test
    public void anyConvertibleBeanIsMatchingWithItsConverter() {
        final BeanConverterImpl<ConvertibleBeanArtifact, ConvertibleBeanArtifact> converter = new BeanConverterImpl<>();
        
        assertTrue(converter.matches(new ConvertibleBeanArtifact()));
    }
    
    @Test
    public void beanConversion() {
        final BeanConverterImpl<ConvertibleBeanArtifact, BeanArtifact> converter = new BeanConverterImpl<>();
        final ConvertibleBeanArtifact sourceObject = new ConvertibleBeanArtifact();
        final String name = "Teste";
        
        sourceObject.setName(name);
        
        final BeanArtifact targetObject = converter
            .from(sourceObject)
            .to(BeanArtifact.class);
        
        assertThat(targetObject, hasProperty("name", is(name)));
    }
}
