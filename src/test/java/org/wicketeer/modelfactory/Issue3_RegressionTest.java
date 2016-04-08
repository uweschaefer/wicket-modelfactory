package org.wicketeer.modelfactory;

import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;

public class Issue3_RegressionTest extends TestCase {

    public void testShouldThrowIllegalArg() throws Exception {
        // Empty model
        try {
            IModel<Foo> mdl = new Model<Foo>();
            new WicketTester()
                    .startComponentInPage(new TestPanel("panel", mdl));
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
    }

    public void testFindTypeFromAnonClass() throws Exception {
        // Empty model
        IModel<Foo> mdl = new Model<Foo>() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;
        };
        WicketTester tester = new WicketTester();
        TestPanel panel = new TestPanel("panel", mdl);

        tester.startComponentInPage(panel);
        Foo foo = new Foo();
        foo.setBar("baz");
        mdl.setObject(foo);
        tester.assertLabel("panel:label", "baz");
    }

    public void testFindTypeFromLDM() throws Exception {
        // Empty model
        final Foo myFoo = new Foo();

        IModel<Foo> mdl = new LoadableDetachableModel<Foo>() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected Foo load() {
                String bar = myFoo.getBar();
                if (bar == null) {
                    fail("too early");
                    return null;
                }
                else {
                    return myFoo;
                }
            }
        };

        WicketTester tester = new WicketTester();
        TestPanel panel = new TestPanel("panel", mdl);

        myFoo.setBar("baz");
        tester.startComponentInPage(panel);
        tester.assertLabel("panel:label", "baz");
    }

    static class MyModel extends Model<Foo> {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Foo getObject() {
            Foo f = super.getObject();
            if (f == null) {
                fail("too early");
                return null;
            }
            else {
                return f;
            }
        }
    }

    public void testFindTypeFromGetObject() throws Exception {
        final Foo myFoo = new Foo();
        IModel<Foo> mdl = new Model<Foo>(myFoo);
        WicketTester tester = new WicketTester();
        TestPanel panel = new TestPanel("panel", mdl);
        myFoo.setBar("baz");
        tester.startComponentInPage(panel);
        tester.assertLabel("panel:label", "baz");
    }

    public void testFindTypeFromTypedModel() throws Exception {
        // Empty model

        IModel<Foo> mdl = new MyModel();
        WicketTester tester = new WicketTester();
        TestPanel panel = new TestPanel("panel", mdl);

        final Foo myFoo = new Foo();
        myFoo.setBar("baz");
        mdl.setObject(myFoo);
        tester.startComponentInPage(panel);
        tester.assertLabel("panel:label", "baz");
    }

    private static class Foo implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private String bar;

        public String getBar() {
            return bar;
        }

        public void setBar(final String bar) {
            this.bar = bar;
        }
    }

    private static class TestPanel extends GenericPanel<Foo>
            implements IMarkupResourceStreamProvider {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private TestPanel(final String id, final IModel<Foo> model) {
            super(id, model);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            // Throws class cast excpetion
            // from method works, but returns a proxy of type Object
            // stepping next would call getBar throws ClassCastException
            add(new Label("label", ModelFactory
                    .model(ModelFactory.from(getModel()).getBar())));
        }

        @Override
        public IResourceStream getMarkupResourceStream(
                final MarkupContainer container,
                final Class<?> containerClass) {
            return new StringResourceStream(
                    "<wicket:panel><span wicket:id=\"label\"></span></wicket:panel>");
        }
    }
}
