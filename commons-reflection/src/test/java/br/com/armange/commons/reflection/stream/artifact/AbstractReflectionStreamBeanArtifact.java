package br.com.armange.commons.reflection.stream.artifact;

public class AbstractReflectionStreamBeanArtifact {
    @SuppressWarnings("unused")
    private String nestedField1;
    protected Integer nestedField2;
    private long nestedField3;
    public long nestedField4;
    
    public long getNestedField3() {
        return nestedField3;
    }
    
    public void setNestedField3(final long nestedField3) {
        this.nestedField3 = nestedField3;
    }
    
    @SuppressWarnings("unused")
    private void nestedMethod() {}
}
