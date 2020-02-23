package br.com.armange.commons.object.api.beanconverter;

public interface StrategicBeanConverter<S, T> extends StrategicBeanConverterReader<S, T>, StrategicBeanConverterWriter<S, T> {
    
//    static <S, T> StrategicBeanConverterReader<S,T> from(final BeanConverterStrategy strategy) {
//        switch (Optional.ofNullable(strategy).orElse(BeanConverterStrategy.SAME_NAME)) {
//        case ANNOTATED:
//            break;
//        case HYBRID:
//            break;
//        default:
//            //find by SPI
//            //new BeanConverterBySameFieldStrategy();
//            break;
//        }
//    }
}
