package br.com.armange.commons.object.impl;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import br.com.armange.commons.object.impl.artifact.BeanArtifact;
import br.com.armange.commons.object.impl.artifact.ConvertibleBeanArtifact;
import br.com.armange.commons.object.impl.typeconverter.bean.BeanConverterImpl;

public class BeanConverterImplTest {

    private static final String LONG_CODE = "longCode";
    private static final String NAME = "name";
    private static final String CODE = "code";
    private static final String BIG_CODE = "bigCode";
    private static final String BIG_DECIMAL = "bigDecimal";

    @Test
    public void anyConvertibleBeanIsMatchingWithItsConverter() {
        final BeanConverterImpl<ConvertibleBeanArtifact, ConvertibleBeanArtifact> converter = new BeanConverterImpl<>();
        
//        assertTrue(converter.matches(new ConvertibleBeanArtifact()));
        fail();
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
        
        assertThat(targetObject, hasProperty(NAME, is(name)));
    }
    
    @Test
    public void beanConversionWithoutConvertibleAnnotation() {
        final BeanConverterImpl<BeanArtifact, ConvertibleBeanArtifact> converter = new BeanConverterImpl<>();
        final BeanArtifact sourceObject = new BeanArtifact();
        final String name = "Teste";
        
        sourceObject.setName(name);
        
        final ConvertibleBeanArtifact targetObject = converter
            .from(sourceObject)
            .to(ConvertibleBeanArtifact.class);
        
        assertThat(targetObject, hasProperty(NAME, is(name)));
    }
    
    @Test
    public void nestedBeanConversionInSourceObject() {
        final BeanConverterImpl<BeanArtifact, ConvertibleBeanArtifact> converter = new BeanConverterImpl<>();
        final BeanArtifact sourceObject = new BeanArtifact();
        final Integer code = 1;
        
        sourceObject.setCode(code);
        
        final ConvertibleBeanArtifact targetObject = converter
            .from(sourceObject)
            .to(ConvertibleBeanArtifact.class);
        
        assertThat(targetObject, hasProperty(CODE, is(code)));
    }
    
    @Test
    public void nestedBeanConversionInTargetObject() {
        final BeanConverterImpl<ConvertibleBeanArtifact, BeanArtifact> converter = new BeanConverterImpl<>();
        final ConvertibleBeanArtifact sourceObject = new ConvertibleBeanArtifact();
        final Integer code = 1;
        
        sourceObject.setCode(code);
        
        final BeanArtifact targetObject = converter
            .from(sourceObject)
            .to(BeanArtifact.class);
        
        assertThat(targetObject, hasProperty(CODE, is(code)));
    }
    
    @Test
    public void primitiveFieldConversion() {
        final BeanConverterImpl<ConvertibleBeanArtifact, BeanArtifact> converter = new BeanConverterImpl<>();
        final ConvertibleBeanArtifact sourceObject = new ConvertibleBeanArtifact();
        final long longCode = 1;
        
        sourceObject.setLongCode(longCode);
        
        final BeanArtifact targetObject = converter
            .from(sourceObject)
            .to(BeanArtifact.class);
        
        assertThat(targetObject, hasProperty(LONG_CODE, is(longCode)));
    }
    
    @Test
    public void beanConversionWithManyFields() {
        final BeanConverterImpl<ConvertibleBeanArtifact, BeanArtifact> converter = new BeanConverterImpl<>();
        final ConvertibleBeanArtifact sourceObject = new ConvertibleBeanArtifact();
        final String name = "Teste";
        final Integer code = 1;
        
        sourceObject.setName(name);
        sourceObject.setCode(code);
        sourceObject.setBigCode(new BigDecimal(1));
        
        final BeanArtifact targetObject = converter
            .from(sourceObject)
            .to(BeanArtifact.class);
        
        assertThat(targetObject, 
                allOf(
                        hasProperty(NAME, is(name)),
                        hasProperty(CODE, is(code)),
                        hasProperty(BIG_DECIMAL, nullValue()),
                        not(hasProperty(BIG_CODE))));
    }
}
