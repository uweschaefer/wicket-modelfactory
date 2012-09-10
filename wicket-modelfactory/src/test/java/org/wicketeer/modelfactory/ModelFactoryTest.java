package org.wicketeer.modelfactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.tester.WicketTester;

public class ModelFactoryTest extends TestCase
{

    private A a;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        new WicketTester();
        a = new A();

    }

    public void testSimpleCallChain() throws Exception
    {
        IModel<String> pm = ModelFactory.model(ModelFactory.from(a).getB().getV().getStringProperty());
        pm = testSer(pm);
        assertEquals("bar", pm.getObject());
    }

    public void testDoubleWrap() throws Exception
    {
        IModel<String> pm = ModelFactory.model(ModelFactory.from(ModelFactory.model(ModelFactory.from(a).getB()))
                .getV().getStringProperty());
        pm = testSer(pm);
        assertEquals("bar", pm.getObject());
        testSer(pm);
    }

    public void testPrimitive() throws Exception
    {
        IModel<Integer> pm = ModelFactory.model(ModelFactory.from(ModelFactory.model(ModelFactory.from(a).getB()))
                .getV().getPrimitiveProperty());
        pm = testSer(pm);
        assertEquals(Integer.valueOf(5), pm.getObject());
        testSer(pm);
    }

    public void testBoolean() throws Exception
    {
        IModel<Boolean> pm = ModelFactory.model(ModelFactory.from(ModelFactory.model(ModelFactory.from(a).getB()))
                .getV().isBooleanProperty());
        pm = testSer(pm);
        assertEquals(Boolean.FALSE, pm.getObject());
        testSer(pm);
    }

    public void testNonPropertyCall() throws Exception
    {
        IModel<String> pm = ModelFactory.model(ModelFactory.from(ModelFactory.model(ModelFactory.from(a).getB()))
                .getV().nonPropertyCall());
        pm = testSer(pm);
        assertEquals("ok", pm.getObject());
        testSer(pm);
    }

    private <X> IModel<X> testSer(final IModel<X> pm) throws IOException, ClassNotFoundException
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(pm);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        try
        {
            return (IModel<X>) ois.readObject();
        }
        finally
        {
            ois.close();
        }

    }

    public static class A implements Serializable
    {

        public B getB()
        {
            return b;
        }

        public void setB(final B b)
        {
            this.b = b;
        }

        B b = new B();

    }
    public static class B implements Serializable
    {
        V c = new V();

        public V getV()
        {
            return c;
        }

    }

    static class V implements Serializable
    {
        private String stringProperty = "bar";
        private int primitiveProperty = 5;
        private boolean booleanProperty = false;

        public String getStringProperty()
        {
            return stringProperty;
        }

        public void setStringProperty(final String stringProperty)
        {
            this.stringProperty = stringProperty;
        }

        public int getPrimitiveProperty()
        {
            return primitiveProperty;
        }

        public void setPrimitiveProperty(final int primitiveProperty)
        {
            this.primitiveProperty = primitiveProperty;
        }

        public boolean isBooleanProperty()
        {
            return booleanProperty;
        }

        public void setBooleanProperty(final boolean booleanProperty)
        {
            this.booleanProperty = booleanProperty;
        }

        public String nonPropertyCall()
        {
            return "ok";
        }
    }

}
