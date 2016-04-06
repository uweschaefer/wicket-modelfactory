package org.wicketeer.modelfactory;

import java.io.Serializable;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;

import junit.framework.TestCase;

public class Issue5_LDM_Type extends TestCase {

    public void testFindTypeFromAnonLDM() throws Exception {
        // Empty model
        final Foo myFoo = new Foo();

        IModel<Foo> mdl = new LoadableDetachableModel<Foo>() {

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
        TestPanel tp = tester.startComponentInPage(panel);
        tester.assertLabel("panel:label", "baz");
    }

    public void testFindTypeFromHierarchyLDM() throws Exception {
        IModel<Foo> mdl = new ComplexLDM();
        WicketTester tester = new WicketTester();
        IModel<String> model = ModelFactory
                .model(ModelFactory.from(mdl).getBar());
        Label panel = new Label("label", model);
        tester.startComponentInPage(panel);
        tester.assertLabel("label", "pc");
    }

    private static class Foo implements Serializable {
        private String bar;

        public String getBar() {
            return this.bar;
        }

        public void setBar(final String bar) {
            this.bar = bar;
        }
    }

    private static class TestPanel extends GenericPanel<Foo>
            implements IMarkupResourceStreamProvider {

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

    static class ComplexLDM extends ParameterizedClass<Integer, Long> {

        private static final long serialVersionUID = 1L;

    }

    static class ParameterizedClass<X, Y> extends LoadableDetachableModel<Foo> {

        private static final long serialVersionUID = 1L;

        @Override
        protected Foo load() {
            Foo foo = new Foo();
            foo.setBar("pc");
            return foo;
        }

    }
}
