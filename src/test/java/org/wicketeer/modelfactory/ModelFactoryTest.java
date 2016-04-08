package org.wicketeer.modelfactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.apache.wicket.util.tester.WicketTester;

public class ModelFactoryTest extends TestCase implements Serializable {

    private static final long serialVersionUID = 1L;
    private A a;
    
    public IModel<A> nullModel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        new WicketTester();
        a = new A();
        
    }

    public void testSimpleCallChain() throws Exception {
        IModel<String> pm = ModelFactory
                .model(ModelFactory.from(a).getB().getV().getStringProperty());
        pm = testSer(pm);
        assertEquals("bar", pm.getObject());
    }

    public void testInFinal() throws Exception {
        try {
            ModelFactory
                    .model(ModelFactory.from(new R3()).getC());
        }
        catch (IllegalArgumentException e) {
            fail("IllegalArgumentException that breaks backward comp. interesting for next major release");
        }
    }

    public void testDoubleWrap() throws Exception {
        IModel<B> model1 = ModelFactory.model(ModelFactory.from(a).getB());
        B from = ModelFactory.from(model1);
        String path = from.getV().getStringProperty();
        IModel<String> pm = ModelFactory.model(path);
        pm = testSer(pm);
        assertEquals("bar", pm.getObject());
        testSer(pm);
    }

    public void testPrimitive() throws Exception {
        IModel<B> model1 = ModelFactory.model(ModelFactory.from(a).getB());
        int model2 = ModelFactory.from(model1).getV().getPrimitiveProperty();
        IModel<Integer> pm = ModelFactory.model(model2);
        pm = testSer(pm);
        assertEquals(Integer.valueOf(5), pm.getObject());

        IModel<Double> pm2 = ModelFactory
                .model(ModelFactory.from(a).getPrimitiveProperty());
        pm2 = testSer(pm2);
        assertEquals(5.0, pm2.getObject());
    }

    public void testBoolean() throws Exception {
        IModel<Boolean> pm = ModelFactory.model(ModelFactory
                .from(ModelFactory.model(ModelFactory.from(a).getB())).getV()
                .isBooleanProperty());
        pm = testSer(pm);
        assertEquals(Boolean.FALSE, pm.getObject());

    }

    public void testNonPropertyCall() throws Exception {
        B bProxy = ModelFactory.from(a).getB();
        IModel<B> bModel = ModelFactory.model(bProxy);
        V vProxy = ModelFactory.from(bModel).getV();
        IModel<String> pm = ModelFactory.model(vProxy.nonPropertyCall());
        pm = testSer(pm);
        assertEquals("ok", pm.getObject());

    }

    @SuppressWarnings("unchecked")
    private <X> IModel<X> testSer(final IModel<X> pm)
            throws IOException, ClassNotFoundException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(pm);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(
                baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        try {
            return (IModel<X>) ois.readObject();
        }
        finally {
            ois.close();
        }

    }

    public static class A implements Serializable {

        public A() {
            this(new B());
        }

        public A(B b) {
            this.b = b;
        }

        private static final long serialVersionUID = 1L;

        public B getB() {
            return b;
        }

        private B b;

        private double primitiveProperty = 5;

        public double getPrimitiveProperty() {
            return primitiveProperty;
        }

        public void setPrimitiveProperty(final double primitiveProperty) {
            this.primitiveProperty = primitiveProperty;
        }
    }

    public static class B implements Serializable {
        private V v;

        public B(V v) {
            this.v = v;
        }

        public B() {
            this(new V());
        }

        private static final long serialVersionUID = 1L;

        public V getV() {
            return v;
        }

    }

    static class V implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private String stringProperty = "bar";

        private int primitiveProperty = 5;

        private boolean booleanProperty = false;

        public String getStringProperty() {
            return stringProperty;
        }

        public void setStringProperty(final String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public int getPrimitiveProperty() {
            return primitiveProperty;
        }

        public void setPrimitiveProperty(final int primitiveProperty) {
            this.primitiveProperty = primitiveProperty;
        }

        public boolean isBooleanProperty() {
            return booleanProperty;
        }

        public void setBooleanProperty(final boolean booleanProperty) {
            this.booleanProperty = booleanProperty;
        }

        public String nonPropertyCall() {
            return "ok";
        }
    }

    /**
     * final class
     */
    public static final class C implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        String foo;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    public static class R3 implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        C c;

        public C getC() {
            return c;
        }

        public void setC(C c) {
            this.c = c;
        }
    }

    public static class R1 {
        public R2 getR2() {
            return null;
        }

        public void setR2(final R2 r2) {
            this.r2 = r2;
        }

        R2 r2;
    }

    public static class R2 {
        String foo;
    }

    public void testNullCompatible() throws Exception {
        try {
            ModelFactory.model(ModelFactory.from(null));
            fail("Nullpointer did not happen");
        }
        catch (NullPointerException e) {
            // fine
        }
        catch (Exception e) {
            fail("Different Exception to Nullpointer! "
                    + e.getClass().getName());
        }
    }

    public static class CallingSetterWhileCreating implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public CallingSetterWhileCreating() {
            setX(2);
            getX();
            getB().getV();
        }

        public int getX() {
            return x;
        }

        public void setX(final int x) {
            this.x = x;
        }

        private int x = 1;

        B b = new B();

        public B getB() {
            return b;
        }

        public void setB(final B b) {
            this.b = b;
        }

    }

    public void testStateCheck() throws Exception {
        assertEquals("b.v", ModelFactory.path(ModelFactory
                .from(new CallingSetterWhileCreating()).getB().getV()));
    }

    public void testFromClass() throws Exception {
        A a = ModelFactory.fromClass(A.class);
        try {
            ModelFactory.model(a);
            fail();
        }
        catch (IllegalStateException ignore) {
        }
        catch (Throwable fallthrough) {
            fail("unexpected " + fallthrough.toString());
        }

        assertNotNull(a);
        assertTrue(A.class.isAssignableFrom(a.getClass()));
        assertNotSame(A.class, a.getClass());
    }

    public void testFromClassRoundTrip() throws Exception {
        A a = ModelFactory.fromClass(A.class);
        try {
            ModelFactory.model(a);
            fail();
        }
        catch (IllegalStateException ignore) {
        }
        catch (Throwable fallthrough) {
            fail("unexpected " + fallthrough.toString());
        }

        V v = a.getB().getV();
        String p = ModelFactory.path(v);
        assertEquals(p, "b.v");
    }

    public void testFromClassNPE() throws Exception {
        try {
            @SuppressWarnings("unused")
            A a = ModelFactory.fromClass(null);
        }
        catch (NullPointerException ignore) {
        }
        catch (Throwable fallthrough) {
            fail("unexpected " + fallthrough.toString());
        }
    }

    public void testFromTyped() throws Exception {
        V v = new V();
        String stringProperty = "testFromTyped";
        v.setStringProperty(stringProperty);
        Model<A> a = Model.of(new A(new B(v)));
        IModel<String> pm = ModelFactory.model(ModelFactory.from(a, A.class)
                .getB().getV().getStringProperty());
        assertSame(stringProperty, pm.getObject());
        testSer(pm);
    }

    public void testFromLDM() throws Exception {

        LDM1 ldm1 = new LDM1();
        IModel<String> pm = ModelFactory.model(
                ModelFactory.from(ldm1).getB().getV().getStringProperty());
        testSer(pm);
    }

    public void testFromAROM() throws Exception {
        IModel<String> pm = ModelFactory.model(ModelFactory.from(new AROM1())
                .getB().getV().getStringProperty());
        testSer(pm);
    }

    public void testFromAnonModelWithNull() throws Exception {
        Model<A> a = new Model<A>(null) {

            private static final long serialVersionUID = 1L;
        };
        IModel<B> pm = ModelFactory.model(ModelFactory.from(a).getB());
        testSer(pm);
    }

    public void testFailForNullNonAnon() throws Exception {
        Model<A> a = Model.of((A) null);
        try {
            ModelFactory.from(a);
            fail("Missing IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    public void testTypeWithNull() throws Exception {
        Model<A> a = Model.of((A) null);
        IModel<B> m = ModelFactory.model(ModelFactory.from(a, A.class).getB());
        testSer(m);

        assertNull(m.getObject());
        a.setObject(new A());
        assertNotNull(m.getObject());
    }

    public void testFromAnonLDM() throws Exception {
        IModel<A> a = new LoadableDetachableModel<A>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected A load() {
                throw new RuntimeException("load was called, but shouldn't be");
            }
        };
        IModel<B> pm = ModelFactory.model(ModelFactory.from(a).getB());
        testSer(pm);
    }

    static class LDM1 extends LoadableDetachableModel<A> {
        private static final long serialVersionUID = 1L;

        @Override
        protected A load() {
            throw new RuntimeException("load was called, but shouldn't be");
        }
    }

    static class AROM1 extends AbstractReadOnlyModel<A> {
        private static final long serialVersionUID = 1L;

        @Override
        public A getObject() {
            throw new RuntimeException(
                    "getObject was called, but shouldn't be");
        }
    }

}
